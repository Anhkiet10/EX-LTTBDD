package com.example.bai01

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ============================================================
 * Màn 10 – Hồ sơ (thông tin cá nhân)
 * ============================================================ */

private val UthGreenBg = Color(0xFFE7F7EC)
private val UthGreen = Color(0xFF1F8A43)
private val UthBarPast = Color(0xFFB4C6F8)

@Composable
fun ProfileScreen(
    user: UthUser,
    tasks: List<TodoTask>,
    weekly: WeeklyStats,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    /** Nút kiểm tra Crashlytics (làm app crash thật). null = ẩn. */
    onTestCrash: (() -> Unit)? = null,
    /** Sao chép FCM token để gửi thông báo thử từ Console. null = ẩn. */
    onCopyFcmToken: (() -> Unit)? = null,
) {
    val progress = TodayProgress(tasks)

    Column(
        Modifier
            .fillMaxSize()
            .background(UthScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // ---- Thanh tiêu đề ----
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                Modifier
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onBack() }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LineIcon(UthIcons.ChevronLeft, UthBlue, size = 18.dp)
                Spacer(Modifier.width(2.dp))
                Text("Cài đặt", style = uthText(15.sp, FontWeight.Medium, UthBlue, 19.sp))
            }
            Text(
                "Hồ sơ",
                style = uthText(16.sp, FontWeight.SemiBold, UthDark, 20.sp),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Avatar + tên + email ----
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(user.initials, size = 84, textSize = 28)
                Spacer(Modifier.height(14.dp))
                Text(user.displayName, style = uthText(20.sp, FontWeight.Bold, UthDark, 26.sp, (-0.3).sp))
                if (user.contact.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(user.contact, style = uthText(13.sp, FontWeight.Normal, UthMuted, 17.sp))
                }
                Spacer(Modifier.height(10.dp))
                LinkedChip(user)
            }

            Spacer(Modifier.height(22.dp))

            // ---- 3 ô thống kê ----
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("${progress.done}", "Việc đã xong", Modifier.weight(1f))
                StatCard("${progress.pending}", "Đang chờ", Modifier.weight(1f))
                StatCard("${progress.percent}%", "Hoàn thành", Modifier.weight(1f))
            }

            Spacer(Modifier.height(14.dp))

            WeeklyChartCard(weekly, todayDone = progress.done)

            // Trang web trên Firebase Hosting
            Spacer(Modifier.height(14.dp))
            val uriHandler = LocalUriHandler.current
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                listOf(
                    "Chính sách quyền riêng tư" to HostingUrls.PRIVACY,
                    "Điều khoản sử dụng" to HostingUrls.TERMS,
                ).forEach { (label, url) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri(url) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = uthText(14.sp, FontWeight.Medium, UthDark, 18.sp), modifier = Modifier.weight(1f))
                        LineIcon(UthIcons.ChevronRight, UthChevron, size = 18.dp)
                    }
                }
            }

            if (onCopyFcmToken != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { onCopyFcmToken() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LineIcon(UthIcons.Bell, UthBlue, size = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sao chép FCM token", style = uthText(14.sp, FontWeight.SemiBold, UthDark, 18.sp))
                        Text(
                            "Dán vào Firebase Console → Messaging → Send test message",
                            style = uthText(11.sp, FontWeight.Normal, UthMuted, 14.sp)
                        )
                    }
                    LineIcon(UthIcons.ChevronRight, UthChevron, size = 18.dp)
                }
            }

            if (onTestCrash != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable { onTestCrash() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Gửi lỗi thử (Crashlytics)", style = uthText(14.sp, FontWeight.SemiBold, UthDark, 18.sp))
                        Text(
                            "App sẽ tắt đột ngột – mở lại để báo cáo được gửi lên Firebase",
                            style = uthText(11.sp, FontWeight.Normal, UthMuted, 14.sp)
                        )
                    }
                    LineIcon(UthIcons.ChevronRight, UthChevron, size = 18.dp)
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // ---- Đăng xuất ----
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { onSignOut() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LineIcon(UthIcons.LogOut, UthRed, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất", style = uthText(15.sp, FontWeight.SemiBold, UthRed, 19.sp))
        }
    }
}

@Composable
private fun LinkedChip(user: UthUser) {
    val bg = if (user.isGuest) UthBorder else UthGreenBg
    val fg = if (user.isGuest) UthSkip else UthGreen
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!user.isGuest) {
            LineIcon(UthIcons.Check, fg, size = 12.dp, strokeWidth = 3f)
            Spacer(Modifier.width(4.dp))
        }
        Text(user.linkedLabel(), style = uthText(11.sp, FontWeight.SemiBold, fg, 14.sp))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(value, style = uthText(20.sp, FontWeight.Bold, UthDark, 26.sp, (-0.3).sp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = uthText(11.sp, FontWeight.Normal, UthMuted, 14.sp))
    }
}

// ---- Biểu đồ "Tuần này" ----
@Composable
private fun WeeklyChartCard(weekly: WeeklyStats, todayDone: Int) {
    val counts = weekly.countsWithToday(todayDone)
    val maxCount = (counts.filterNotNull().maxOrNull() ?: 0).coerceAtLeast(1)
    val maxBar = 64f

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tuần này", style = uthText(14.sp, FontWeight.SemiBold, UthDark, 18.sp))
            Text("Việc xong mỗi ngày", style = uthText(11.sp, FontWeight.Normal, UthMuted, 14.sp))
        }
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(maxBar.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            counts.forEachIndexed { i, count ->
                val isToday = i == weekly.todayIndex
                val height = if (count == null || count == 0) 4f else maxBar * count / maxCount
                val color = when {
                    count == null || count == 0 -> UthBorder
                    isToday -> UthBlue
                    else -> UthBarPast
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .width(18.dp)
                            .height(height.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            weekly.dayLabels.forEachIndexed { i, label ->
                val isToday = i == weekly.todayIndex
                Text(
                    label,
                    style = uthText(
                        10.sp,
                        if (isToday) FontWeight.Bold else FontWeight.Normal,
                        if (isToday) UthDark else UthMuted,
                        13.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(name = "10 Hồ sơ", widthDp = 390, heightDp = 844)
@Composable
fun ProfilePreview() {
    val repo = LocalUthRepository()
    ProfileScreen(
        user = UthUser("1", "Minh Anh", "minhanh@example.com", AuthProvider.GOOGLE),
        tasks = repo.getTodayTasks(),
        weekly = repo.getWeeklyStats(),
        onBack = {},
        onSignOut = {}
    )
}
