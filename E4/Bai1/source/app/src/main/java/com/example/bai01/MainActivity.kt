package com.example.bai01

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.bai01.ui.theme.Bai01Theme
import com.google.android.gms.ads.MobileAds
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * FIREBASE APP CHECK – chỉ cho app thật của mình gọi Firebase.
 *  - Bản debug (chạy từ Android Studio / máy ảo): Debug provider → in "debug secret" ra Logcat,
 *    dán vào Console → App Check → Apps → Manage debug tokens.
 *  - Bản release (cài từ Google Play): Play Integrity.
 */
private fun ComponentActivity.installAppCheck() {
    val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val factory = if (isDebuggable) DebugAppCheckProviderFactory.getInstance()
    else PlayIntegrityAppCheckProviderFactory.getInstance()
    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installAppCheck() // App Check: phải cài trước khi gọi Firestore/Auth
        enableEdgeToEdge()
        Telemetry.init(this) // Firebase Analytics
        RemoteConfigService.init() // Firebase Remote Config
        PushHelper.createChannel(this) // kênh thông báo cho FCM
        // AdMob: khởi tạo ở luồng nền cho app mở nhanh hơn
        Thread { MobileAds.initialize(this) {} }.start()
        // Mỗi lần quay lại app (từ nền) → tải lại Remote Config
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = RemoteConfigService.refresh()
        })
        setContent {
            Bai01Theme {
                UthNavHost()
            }
        }
    }
}
