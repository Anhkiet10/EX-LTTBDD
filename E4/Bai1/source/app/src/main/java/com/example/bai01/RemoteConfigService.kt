package com.example.bai01

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/* ============================================================
 * REMOTE CONFIG – đổi chữ / bật tắt tính năng từ Firebase Console
 * mà không cần cập nhật app.
 *
 * Tên tham số (tạo đúng tên này trong Console → Remote Config):
 *   home_greeting        lời chào ở Trang chủ               ("Chào")
 *   msg_empty            khi hôm nay chưa có việc
 *   msg_start            khi chưa xong việc nào
 *   msg_progress         khi xong dưới 50%
 *   msg_half             khi xong từ 50%
 *   msg_done             khi xong hết
 *   banner_enabled       true/false – hiện banner thông báo ở Trang chủ
 *   banner_text          nội dung banner
 *   show_crash_button    true/false – hiện nút "Gửi lỗi thử" ở Hồ sơ
 *   show_ads             true/false – hiện banner quảng cáo AdMob ở Trang chủ
 * ============================================================ */

/** Cấu hình hiện tại của app (giá trị mặc định = khi chưa tải được từ Firebase). */
data class AppConfig(
    val greeting: String = "Chào",
    val msgEmpty: String = "Hôm nay chưa có việc nào.",
    val msgStart: String = "Bắt đầu từ việc nhỏ nhất nhé.",
    val msgProgress: String = "Khởi đầu tốt, tiếp tục nào!",
    val msgHalf: String = "Đã hơn nửa chặng, cố lên!",
    val msgDone: String = "Xong hết rồi, tuyệt vời!",
    val bannerEnabled: Boolean = false,
    val bannerText: String = "",
    val showCrashButton: Boolean = true,
    val showAds: Boolean = true,
) {
    /** Câu động viên theo tiến độ hôm nay. */
    fun messageFor(p: TodayProgress): String = when {
        p.total == 0 -> msgEmpty
        p.done == 0 -> msgStart
        p.done == p.total -> msgDone
        p.fraction >= 0.5f -> msgHalf
        else -> msgProgress
    }

    fun toDefaults(): Map<String, Any> = mapOf(
        "home_greeting" to greeting,
        "msg_empty" to msgEmpty,
        "msg_start" to msgStart,
        "msg_progress" to msgProgress,
        "msg_half" to msgHalf,
        "msg_done" to msgDone,
        "banner_enabled" to bannerEnabled,
        "banner_text" to bannerText,
        "show_crash_button" to showCrashButton,
        "show_ads" to showAds,
    )
}

object RemoteConfigService {

    private val remoteConfig: FirebaseRemoteConfig get() = FirebaseRemoteConfig.getInstance()
    private val _config = MutableStateFlow(AppConfig())

    /** Giao diện collect cái này → đổi trên Console là app tự cập nhật. */
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private var started = false

    fun init() {
        if (started) return
        started = true

        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                // Bài tập: cho tải lại sau 60 giây. App thật nên để 3600 (1 giờ) trở lên.
                .setMinimumFetchIntervalInSeconds(60)
                .build()
        )
        remoteConfig.setDefaultsAsync(AppConfig().toDefaults())
            .addOnCompleteListener { publish() }

        // Lần mở app: tải giá trị mới nhất
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { publish() }

        // Realtime: bấm Publish trên Console là app nhận ngay (không cần mở lại app)
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Realtime: có thay đổi ${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener { publish() }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                // Realtime lỗi (vd chưa bật Realtime API) → vẫn còn refresh() định kỳ bên dưới
                Log.w(TAG, "Realtime lỗi: ${error.code} – ${error.message}")
                Telemetry.nonFatal(error, "remoteConfigRealtime")
            }
        })
    }

    /**
     * Tải lại cấu hình (dự phòng khi Realtime không chạy).
     * Gọi khi app quay lại màn hình và định kỳ mỗi 60 giây khi đang dùng app.
     */
    fun refresh() {
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener { changed -> Log.d(TAG, "refresh: changed=$changed") }
            .addOnFailureListener { Log.w(TAG, "refresh lỗi: ${it.message}") }
            .addOnCompleteListener { publish() }
    }

    private const val TAG = "UTH-RC"

    private fun publish() {
        val rc = remoteConfig
        val d = AppConfig()
        fun str(key: String, default: String) = rc.getString(key).ifBlank { default }
        _config.value = AppConfig(
            greeting = str("home_greeting", d.greeting),
            msgEmpty = str("msg_empty", d.msgEmpty),
            msgStart = str("msg_start", d.msgStart),
            msgProgress = str("msg_progress", d.msgProgress),
            msgHalf = str("msg_half", d.msgHalf),
            msgDone = str("msg_done", d.msgDone),
            bannerEnabled = rc.getBoolean("banner_enabled"),
            bannerText = rc.getString("banner_text"),
            showCrashButton = rc.getBoolean("show_crash_button"),
            showAds = rc.getBoolean("show_ads"),
        )
    }
}
