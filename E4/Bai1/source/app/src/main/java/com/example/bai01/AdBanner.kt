package com.example.bai01

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/* ============================================================
 * ADMOB – banner quảng cáo ở Trang chủ
 * Đang dùng ID QUẢNG CÁO THỬ của Google (an toàn để bấm, không vi phạm chính sách).
 * Khi có tài khoản AdMob thật: thay AD_UNIT_ID ở đây và APPLICATION_ID trong AndroidManifest.
 * ============================================================ */
object AdIds {
    /** Banner thích ứng – ID thử của Google. */
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    if (failed) return // không có quảng cáo → ẩn luôn, không để khoảng trống

    Column(modifier.fillMaxWidth()) {
        Text(
            "Quảng cáo",
            style = uthText(10.sp, FontWeight.Medium, UthMuted, 12.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (loaded) Color.Transparent else Color.White),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            val widthDp = maxWidth.value.toInt()
            val adView = remember(widthDp) {
                AdView(context).apply {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
                    adUnitId = AdIds.BANNER
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("UTH-ADS", "Banner đã tải")
                            loaded = true
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("UTH-ADS", "Banner lỗi ${error.code}: ${error.message}")
                            failed = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
            DisposableEffect(adView) {
                onDispose { adView.destroy() }
            }
            if (!loaded) {
                Box(Modifier.height(50.dp), contentAlignment = Alignment.Center) {
                    Text("Đang tải quảng cáo…", style = uthText(11.sp, FontWeight.Normal, UthMuted, 14.sp))
                }
            }
            AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(4.dp))
    }
}
