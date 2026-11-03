package com.example.bai01

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/* ============================================================
 * CLOUD MESSAGING (FCM) – nhận thông báo đẩy
 *  - Khi app ở nền/đã tắt: Android tự hiện thông báo (kênh mặc định trong Manifest)
 *  - Khi app đang mở: onMessageReceived → tự hiện thông báo bằng PushHelper.show
 *  - Token của máy được lưu vào Firestore: users/{uid}.fcmTokens
 *  - Mọi máy đăng ký chủ đề "all_users" → gửi 1 lần tới tất cả người dùng
 * ============================================================ */
class UthMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(PushHelper.TAG, "Token mới: $token")
        PushHelper.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(PushHelper.TAG, "Nhận tin: ${message.notification?.title} | data=${message.data}")
        val title = message.notification?.title ?: message.data["title"] ?: "UTH Todo"
        val body = message.notification?.body ?: message.data["body"] ?: return
        PushHelper.show(this, title, body)
    }
}

object PushHelper {
    const val TAG = "UTH-FCM"
    const val CHANNEL_ID = "uth_reminders"
    const val TOPIC_ALL = "all_users"

    /** Tạo kênh thông báo (Android 8+). Gọi 1 lần trong MainActivity. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nhắc việc",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Thông báo nhắc việc và tin tức từ UTH Todo" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Hiện 1 thông báo; bấm vào sẽ mở app. */
    fun show(context: Context, title: String, body: String) {
        if (!hasPermission(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF2F5BEA.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Không có quyền hiện thông báo", e)
        }
    }

    /** Sau khi đăng nhập: lấy token, lưu vào Firestore, đăng ký chủ đề chung. */
    fun registerUser(uid: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token: $token")
                saveToken(token, uid)
            }
            .addOnFailureListener { Log.w(TAG, "Không lấy được token: ${it.message}") }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ALL)
            .addOnSuccessListener { Log.d(TAG, "Đã đăng ký chủ đề $TOPIC_ALL") }
        // In-App Messaging dùng Installation ID để "Test on device"
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener { Log.d(TAG, "Installation ID (In-App Messaging): $it") }
    }

    fun saveToken(token: String, uid: String? = FirebaseAuth.getInstance().currentUser?.uid) {
        if (uid == null) return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)), SetOptions.merge())
    }

    /** Sao chép token vào bộ nhớ tạm để dán vào Console → "Send test message". */
    fun copyToken(context: Context, onDone: (String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText("FCM token", token))
                Log.d(TAG, "FCM token: $token")
                onDone("Đã sao chép FCM token")
            }
            .addOnFailureListener { onDone("Không lấy được token: ${it.message}") }
    }
}
