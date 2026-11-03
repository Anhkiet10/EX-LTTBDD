package com.example.bai01

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/* ============================================================
 * CLOUD FIRESTORE – lưu công việc theo từng tài khoản
 *
 *  users/{uid}                    ← hồ sơ: tên, email, cách đăng nhập
 *  users/{uid}/tasks/{taskId}     ← từng công việc
 *      title, category (STUDY|LIFE), time "HH:mm", date "yyyy-MM-dd",
 *      done, createdAt, updatedAt
 *
 * Firestore tự lưu offline: mất mạng vẫn thêm/sửa được, có mạng lại tự đồng bộ.
 * ============================================================ */

interface TaskRepository {
    /** Theo dõi (realtime) các việc của 1 ngày. */
    fun observeDay(uid: String, date: String): Flow<List<TodoTask>>

    /** Theo dõi số việc đã xong của từng ngày trong [dates] (dùng cho biểu đồ tuần). */
    fun observeDoneByDate(uid: String, dates: List<String>): Flow<Map<String, Int>>

    fun add(uid: String, task: TodoTask, onError: (String) -> Unit)
    fun update(uid: String, task: TodoTask, onError: (String) -> Unit)
    fun setDone(uid: String, taskId: String, done: Boolean, onError: (String) -> Unit)
    fun delete(uid: String, taskId: String, onError: (String) -> Unit)

    /** Lưu/cập nhật hồ sơ người dùng mỗi lần đăng nhập. */
    fun saveProfile(user: UthUser)
}

class FirestoreTaskRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : TaskRepository {

    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun tasks(uid: String) = userDoc(uid).collection("tasks")

    override fun observeDay(uid: String, date: String): Flow<List<TodoTask>> = callbackFlow {
        // Performance Monitoring: đo thời gian từ lúc mở đến khi có danh sách việc đầu tiên
        val trace = Telemetry.startTrace("load_today_tasks")
        var traceStopped = false
        val registration = tasks(uid)
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toTodoTask() }
                    .sortedBy { it.time }
                if (!traceStopped) {
                    traceStopped = true
                    trace.putMetric("task_count", list.size.toLong())
                    trace.putAttribute("from_cache", (snapshot?.metadata?.isFromCache == true).toString())
                    trace.stop()
                }
                trySend(list)
            }
        awaitClose {
            registration.remove()
            if (!traceStopped) trace.stop()
        }
    }

    override fun observeDoneByDate(uid: String, dates: List<String>): Flow<Map<String, Int>> = callbackFlow {
        // Chỉ lọc theo 1 trường (date) → không cần tạo index trong Firestore
        val registration = tasks(uid)
            .whereGreaterThanOrEqualTo("date", dates.first())
            .whereLessThanOrEqualTo("date", dates.last())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val counts = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toTodoTask() }
                    .filter { it.isDone }
                    .groupingBy { it.date }
                    .eachCount()
                trySend(counts)
            }
        awaitClose { registration.remove() }
    }

    override fun add(uid: String, task: TodoTask, onError: (String) -> Unit) {
        tasks(uid)
            .add(task.toMap() + mapOf("createdAt" to FieldValue.serverTimestamp()))
            .addOnFailureListener { onError(firestoreMessage(it)) }
    }

    override fun update(uid: String, task: TodoTask, onError: (String) -> Unit) {
        tasks(uid).document(task.id)
            .set(task.toMap(), SetOptions.merge())
            .addOnFailureListener { onError(firestoreMessage(it)) }
    }

    override fun setDone(uid: String, taskId: String, done: Boolean, onError: (String) -> Unit) {
        tasks(uid).document(taskId)
            .update(mapOf("done" to done, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnFailureListener { onError(firestoreMessage(it)) }
    }

    override fun delete(uid: String, taskId: String, onError: (String) -> Unit) {
        tasks(uid).document(taskId)
            .delete()
            .addOnFailureListener { onError(firestoreMessage(it)) }
    }

    override fun saveProfile(user: UthUser) {
        userDoc(user.uid).set(
            mapOf(
                "displayName" to user.displayName,
                "email" to user.email,
                "phone" to user.phone,
                "provider" to user.provider.name,
                "lastLoginAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge()
        )
    }

    private fun TodoTask.toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "category" to category.name,
        "time" to time,
        "date" to date,
        "done" to isDone,
        "updatedAt" to FieldValue.serverTimestamp(),
    )

    private fun DocumentSnapshot.toTodoTask(): TodoTask? {
        val title = getString("title") ?: return null
        return TodoTask(
            id = id,
            title = title,
            category = runCatching { Category.valueOf(getString("category") ?: "") }
                .getOrDefault(Category.STUDY),
            time = getString("time") ?: "",
            isDone = getBoolean("done") ?: false,
            date = getString("date") ?: "",
        )
    }
}

/** Đổi lỗi Firestore thành câu tiếng Việt. */
fun firestoreMessage(e: Throwable): String {
    val code = (e as? FirebaseFirestoreException)?.code
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Firestore từ chối truy cập. Hãy kiểm tra Rules trong Firebase Console → Firestore Database → Rules."
        FirebaseFirestoreException.Code.NOT_FOUND ->
            "Chưa tạo Firestore Database. Vào Firebase Console → Firestore Database → Create database."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Không kết nối được Firestore. Dữ liệu sẽ tự đồng bộ khi có mạng."
        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại."
        else -> e.localizedMessage ?: "Lỗi Firestore không xác định."
    }
}
