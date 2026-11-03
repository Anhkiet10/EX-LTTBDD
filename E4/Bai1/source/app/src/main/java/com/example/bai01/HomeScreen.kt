package com.example.bai01

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ============================================================
 * Màn 2 – Trang chủ
 *  Nhấn avatar (thông tin cá nhân) → màn Hồ sơ
 * ============================================================ */

val UthScreenBg = Color(0xFFF2F3F7)
val UthChevron = Color(0xFFC7C7CC)
val UthRed = Color(0xFFE5484D)
val UthAvatarBg = Color(0xFFE6EDFF)

@Composable
fun HomeScreen(
    user: UthUser,
    tasks: List<TodoTask>,
    onToggleTask: (TodoTask) -> Unit,
    onOpenProfile: () -> Unit,
    /** true khi đang tải danh sách việc từ Firestore lần đầu. */
    loading: Boolean = false,
    onAddTask: () -> Unit = {},
    onEditTask: (TodoTask) -> Unit = {},
    /** Chữ / banner lấy từ Remote Config. */
    config: AppConfig = AppConfig(),
) {
    val progress = TodayProgress(tasks)

    Box(
        Modifier
            .fillMaxSize()
            .background(UthScreenBg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 120.dp)
        ) {
            HomeHeader(user, config.greeting, onOpenProfile)
            if (config.bannerEnabled && config.bannerText.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                RemoteBanner(config.bannerText)
            }
            Spacer(Modifier.height(18.dp))
            ProgressCard(progress, config.messageFor(progress))
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategorySummaryCard(
                    category = Category.STUDY,
                    icon = UthIcons.GraduationCap,
                    remaining = TodayProgress.remainingIn(tasks, Category.STUDY),
                    modifier = Modifier.weight(1f)
                )
                CategorySummaryCard(
                    category = Category.LIFE,
                    icon = UthIcons.Home,
                    remaining = TodayProgress.remainingIn(tasks, Category.LIFE),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Việc hôm nay", style = uthText(18.sp, FontWeight.Bold, UthDark, 24.sp, (-0.2).sp))
                Text("Xem tất cả", style = uthText(13.sp, FontWeight.SemiBold, UthBlue, 16.sp))
            }
            Spacer(Modifier.height(12.dp))
            when {
                loading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(28.dp), color = UthBlue, strokeWidth = 3.dp)
                }
                tasks.isEmpty() -> EmptyTasksCard(onAddTask)
                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.forEach { task ->
                        TaskRow(
                            task,
                            onToggle = { onToggleTask(task) },
                            onClick = { onEditTask(task) },
                        )
                    }
                }
            }

            // AdMob – bật/tắt bằng Remote Config "show_ads"
            if (config.showAds) {
                Spacer(Modifier.height(18.dp))
                AdBanner()
            }
        }

        UthBottomBar(
            selected = 0,
            onSelect = { if (it == 3) onOpenProfile() },
            onAdd = onAddTask,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ---------------- Header: avatar + lời chào + chuông ----------------
@Composable
private fun HomeHeader(user: UthUser, greeting: String, onOpenProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nhấn vào thông tin cá nhân → Hồ sơ
        Row(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onOpenProfile() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(user.initials, size = 42, textSize = 15)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(vietnameseToday(), style = uthText(12.sp, FontWeight.Normal, UthMuted, 15.sp))
                Spacer(Modifier.height(2.dp))
                Text(
                    "$greeting ${user.shortName}!",
                    style = uthText(20.sp, FontWeight.Bold, UthDark, 26.sp, (-0.3).sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            LineIcon(UthIcons.Bell, UthDark, size = 20.dp)
            Box(
                Modifier
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(UthRed)
            )
        }
    }
}

@Composable
fun Avatar(initials: String, size: Int, textSize: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(UthAvatarBg),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, style = uthText(textSize.sp, FontWeight.Bold, UthBlue, (textSize + 4).sp))
    }
}

// ---------------- Thẻ tiến độ xanh ----------------
@Composable
private fun ProgressCard(progress: TodayProgress, message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .cssShadow(RoundedCornerShape(20.dp), y = 10.dp, blur = 24.dp, color = Color(0x332F5BEA))
            .clip(RoundedCornerShape(20.dp))
            .background(UthBlue)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Tiến độ hôm nay", style = uthText(13.sp, FontWeight.Medium, Color.White.copy(alpha = 0.85f), 16.sp))
            Spacer(Modifier.height(6.dp))
            Text(progress.label(), style = uthText(28.sp, FontWeight.Bold, Color.White, 34.sp, (-0.5).sp))
            Spacer(Modifier.height(6.dp))
            Text(message, style = uthText(12.sp, FontWeight.Normal, Color.White.copy(alpha = 0.85f), 15.sp))
        }
        Spacer(Modifier.width(12.dp))
        ProgressRing(progress.fraction, "${progress.percent}%")
    }
}

@Composable
private fun ProgressRing(fraction: Float, label: String) {
    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 6.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                Color.White.copy(alpha = 0.25f), 0f, 360f, false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke)
            )
            drawArc(
                Color.White, -90f, 360f * fraction, false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text(label, style = uthText(13.sp, FontWeight.Bold, Color.White, 16.sp))
    }
}

// ---------------- Thẻ nhóm Học tập / Cuộc sống ----------------
@Composable
private fun CategorySummaryCard(
    category: Category,
    icon: List<Path>,
    remaining: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(category.chipBackground)),
            contentAlignment = Alignment.Center
        ) {
            LineIcon(icon, Color(category.chipText), size = 16.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(category.label, style = uthText(15.sp, FontWeight.SemiBold, UthDark, 19.sp))
        Spacer(Modifier.height(4.dp))
        Text("$remaining việc còn lại", style = uthText(12.sp, FontWeight.Normal, UthMuted, 15.sp))
    }
}

// ---------------- Một dòng việc ----------------
@Composable
private fun TaskRow(task: TodoTask, onToggle: () -> Unit, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .clickable { onToggle() }
                .padding(2.dp)
        ) {
            if (task.isDone) CheckedBox() else EmptyBox()
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                style = uthText(
                    15.sp,
                    FontWeight.Medium,
                    if (task.isDone) UthMuted else UthDark,
                    19.sp
                ).copy(textDecoration = if (task.isDone) TextDecoration.LineThrough else null),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.category.label,
                    style = uthText(11.sp, FontWeight.SemiBold, Color(task.category.chipText), 14.sp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(task.category.chipBackground))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(task.time, style = uthText(12.sp, FontWeight.Normal, UthMuted, 15.sp))
            }
        }
        LineIcon(UthIcons.ChevronRight, UthChevron, size = 18.dp)
    }
}

// ---------------- Banner từ Remote Config ----------------
@Composable
private fun RemoteBanner(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF4D6))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LineIcon(UthIcons.Bell, Color(0xFFB7791F), size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Text(text, style = uthText(13.sp, FontWeight.Medium, Color(0xFF7A4E0B), 18.sp))
    }
}

// ---------------- Chưa có việc nào ----------------
@Composable
private fun EmptyTasksCard(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onAdd() }
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(UthAvatarBg),
            contentAlignment = Alignment.Center
        ) {
            LineIcon(UthIcons.Plus, UthBlue, size = 22.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Hôm nay chưa có việc nào", style = uthText(15.sp, FontWeight.SemiBold, UthDark, 19.sp))
        Spacer(Modifier.height(4.dp))
        Text("Bấm + để thêm việc đầu tiên", style = uthText(13.sp, FontWeight.Normal, UthMuted, 17.sp))
    }
}

// ---------------- Thanh điều hướng dưới ----------------
private class NavItem(val label: String, val icon: List<Path>)

@Composable
fun UthBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit = {},
) {
    val items = listOf(
        NavItem("Trang chủ", UthIcons.Home),
        NavItem("Danh sách", UthIcons.List),
        NavItem("Thông báo", UthIcons.Bell),
        NavItem("Cài đặt", UthIcons.Sliders),
    )
    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .background(Color.White)
                .navigationBarsPadding()
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(UthBorder.copy(alpha = 0.6f)))
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { i, item ->
                    if (i == 2) Spacer(Modifier.weight(1f)) // chỗ cho nút +
                    val active = i == selected
                    val color = if (active) UthBlue else UthMuted
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(i) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LineIcon(item.icon, color, size = 22.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(item.label, style = uthText(11.sp, FontWeight.Medium, color, 14.sp))
                    }
                }
            }
        }
        // Nút + nổi
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(54.dp)
                .cssShadow(CircleShape, y = 8.dp, blur = 18.dp, color = Color(0x592F5BEA))
                .clip(CircleShape)
                .background(UthBlue)
                .clickable { onAdd() },
            contentAlignment = Alignment.Center
        ) {
            LineIcon(UthIcons.Plus, Color.White, size = 24.dp, strokeWidth = 2.4f)
        }
    }
}

// ---------------- Preview ----------------
@Preview(name = "2 Trang chủ", widthDp = 390, heightDp = 844)
@Composable
fun HomePreview() {
    val repo = remember { LocalUthRepository() }
    val tasks = remember { mutableStateListOf(*repo.getTodayTasks().toTypedArray()) }
    HomeScreen(
        user = UthUser("1", "Minh Anh", "minhanh@example.com", AuthProvider.GOOGLE),
        tasks = tasks,
        onToggleTask = { t -> tasks[tasks.indexOf(t)] = t.toggled() },
        onOpenProfile = {}
    )
}
