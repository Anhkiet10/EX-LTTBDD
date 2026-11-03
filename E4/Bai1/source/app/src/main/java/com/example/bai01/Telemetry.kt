package com.example.bai01

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/* ============================================================
 * ANALYTICS + CRASHLYTICS + PERFORMANCE MONITORING
 *  - Analytics: ghi sự kiện (đăng nhập, thêm/xong/xoá việc, mở màn hình)
 *  - Crashlytics: gắn uid vào báo cáo lỗi, ghi lỗi không làm crash app
 *  - Performance: đo thời gian tải danh sách việc (trace tự đặt)
 * Gọi Telemetry.init(context) một lần trong MainActivity.
 * ============================================================ */
object Telemetry {

    private var analytics: FirebaseAnalytics? = null
    private val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics.getInstance()

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    // ---------------- Người dùng ----------------
    /** Gắn người dùng vào Analytics + Crashlytics (null khi đăng xuất). */
    fun setUser(user: UthUser?) {
        analytics?.setUserId(user?.uid)
        analytics?.setUserProperty("login_method", user?.provider?.name?.lowercase())
        crashlytics.setUserId(user?.uid.orEmpty())
        crashlytics.setCustomKey("login_method", user?.provider?.name ?: "none")
    }

    fun login(user: UthUser) = log(
        FirebaseAnalytics.Event.LOGIN,
        FirebaseAnalytics.Param.METHOD to user.provider.name.lowercase(),
    )

    fun logout() = log("logout")

    // ---------------- Màn hình ----------------
    fun screen(name: String) = log(
        FirebaseAnalytics.Event.SCREEN_VIEW,
        FirebaseAnalytics.Param.SCREEN_NAME to name,
        FirebaseAnalytics.Param.SCREEN_CLASS to "MainActivity",
    )

    // ---------------- Công việc ----------------
    fun taskAdded(category: Category) = log("task_add", "category" to category.name.lowercase())
    fun taskEdited() = log("task_edit")
    fun taskDeleted() = log("task_delete")
    fun taskToggled(done: Boolean) = log(if (done) "task_complete" else "task_uncomplete")

    // ---------------- Lỗi ----------------
    /** Ghi lỗi không làm crash app (vd lỗi Firestore) lên Crashlytics → mục "Non-fatals". */
    fun nonFatal(error: Throwable, where: String) {
        crashlytics.setCustomKey("where", where)
        crashlytics.log("Non-fatal at $where: ${error.message}")
        crashlytics.recordException(error)
    }

    /** Nút "Gửi lỗi thử" – làm app crash thật để kiểm tra Crashlytics. */
    fun testCrash(): Nothing {
        crashlytics.log("Người dùng bấm nút Gửi lỗi thử")
        throw RuntimeException("Test Crash từ UTH Todo (Crashlytics)")
    }

    // ---------------- Performance ----------------
    fun startTrace(name: String): Trace = FirebasePerformance.getInstance().newTrace(name).apply { start() }

    // ---------------- Nội bộ ----------------
    private fun log(event: String, vararg params: Pair<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics?.logEvent(event, bundle)
    }
}
