package com.example.bai01

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/* ============================================================
 * Thông số lấy trực tiếp từ Figma "UTH-Mobile / UTH Todo V2"
 * (Copy as code → CSS all layers): Start, 1a, 1b, 1c
 * ============================================================ */

// ---- Font: Be Vietnam Pro (res/font) ----
val BeVietnamPro = FontFamily(
    Font(R.font.bevietnampro_regular, FontWeight.Normal),
    Font(R.font.bevietnampro_medium, FontWeight.Medium),
    Font(R.font.bevietnampro_semibold, FontWeight.SemiBold),
    Font(R.font.bevietnampro_bold, FontWeight.Bold),
    Font(R.font.bevietnampro_extrabold, FontWeight.ExtraBold),
)

// ---- Màu ----
val UthBlue = Color(0xFF2F5BEA)
val UthDark = Color(0xFF1C1C1E)
val UthSkip = Color(0xFF6B6B70)
val UthBody = Color(0xFF5C5C62)
val UthBar = Color(0xFFE4E4EA)
val UthDotOff = Color(0xFFD1D1D6)
val UthCheckBorder = Color(0xFFC7C7CC)
val UthOverlay = Color(0x1A2F5BEA) // rgba(47, 91, 234, 0.1)

internal fun uthText(
    size: TextUnit,
    weight: FontWeight,
    color: Color,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontSize = size,
    fontWeight = weight,
    color = color,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

/** box-shadow: 0 {y} {blur} {color} */
internal fun Modifier.cssShadow(shape: Shape, y: Dp, blur: Dp, color: Color) =
    this.dropShadow(shape, Shadow(radius = blur, color = color, offset = DpOffset(0.dp, y)))

// ============================================================
// Logo (SVG trong Figma, toạ độ theo % kích thước)
// ============================================================
@Composable
fun LogoMark(
    size: Dp,
    corner: Dp,
    background: Color,
    foreground: Color,
    secondCircleAlpha: Float,
    secondLineAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(background)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.width
            // Ô tick: 22%–40% x, 28%–46% y
            drawCircle(foreground, radius = s * 0.09f, center = Offset(s * 0.31f, s * 0.37f))
            // Dấu tick: 26.6%–35.6% x, 34.2%–40.4% y, nét 3.2%
            val check = Path().apply {
                moveTo(s * 0.266f, s * 0.372f)
                lineTo(s * 0.300f, s * 0.404f)
                lineTo(s * 0.356f, s * 0.342f)
            }
            drawPath(
                check, background,
                style = Stroke(width = s * 0.032f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Dòng 1: 48%–73% tại y 37%, nét 8%
            drawLine(
                foreground, Offset(s * 0.48f, s * 0.37f), Offset(s * 0.73f, s * 0.37f),
                strokeWidth = s * 0.08f, cap = StrokeCap.Round
            )
            // Ô trống: 23.8%–38.2% x, 55.8%–70.2% y, nét 3.6%
            drawCircle(
                foreground.copy(alpha = secondCircleAlpha),
                radius = s * 0.072f - s * 0.018f,
                center = Offset(s * 0.31f, s * 0.63f),
                style = Stroke(width = s * 0.036f)
            )
            // Dòng 2: 48%–65% tại y 63%
            drawLine(
                foreground.copy(alpha = secondLineAlpha),
                Offset(s * 0.48f, s * 0.63f), Offset(s * 0.65f, s * 0.63f),
                strokeWidth = s * 0.08f, cap = StrokeCap.Round
            )
        }
    }
}

// ============================================================
// Start (400 × 860)
// ============================================================
@Composable
fun SplashScreen(appInfo: AppInfo, onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = appInfo.splashDurationMillis, easing = LinearEasing))
        onFinished()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(UthBlue)
            .navigationBarsPadding()
            .padding(start = 32.dp, end = 32.dp, bottom = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Container giữa – gap 22
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoMark(
                size = 116.dp,
                corner = 27.dp,
                background = Color.White,
                foreground = UthBlue,
                secondCircleAlpha = 0.55f,
                secondLineAlpha = 0.45f,
                modifier = Modifier.cssShadow(
                    RoundedCornerShape(27.dp), y = 18.dp, blur = 40.dp, color = Color(0x38000000)
                )
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(appInfo.brandName) }
                        withStyle(SpanStyle(fontWeight = FontWeight.Normal)) { append(" " + appInfo.brandSuffix) }
                    },
                    style = uthText(40.sp, FontWeight.ExtraBold, Color.White, 46.sp, (-1).sp)
                )
                Text(
                    appInfo.slogan,
                    style = uthText(15.sp, FontWeight.Normal, Color.White.copy(alpha = 0.9f), 19.sp)
                )
            }
        }

        // Thanh tải + phiên bản – gap 14
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.28f))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White)
                )
            }
            Text(
                appInfo.versionLabel(),
                style = uthText(12.sp, FontWeight.Normal, Color.White.copy(alpha = 0.9f), 15.sp)
            )
        }
    }
}

// ============================================================
// Onboarding 1a / 1b / 1c (390 × 844, padding 60 28 40)
// ============================================================
@Composable
fun OnboardingScreen(appInfo: AppInfo, flow: OnboardingFlow, onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { flow.size })
    val scope = rememberCoroutineScope()
    val currentPage = flow.pageAt(pagerState.currentPage)

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 16.dp, bottom = 24.dp)
    ) {
        // Header – cao 44, space-between
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LogoMark(
                    size = 32.dp,
                    corner = 8.dp,
                    background = UthBlue,
                    foreground = Color.White,
                    secondCircleAlpha = 0.7f,
                    secondLineAlpha = 0.55f
                )
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = UthDark, fontWeight = FontWeight.Bold)) { append(appInfo.brandName) }
                        withStyle(SpanStyle(color = UthBlue, fontWeight = FontWeight.Medium)) { append(" " + appInfo.brandSuffix) }
                    },
                    style = uthText(19.sp, FontWeight.Bold, UthDark, 24.sp, (-0.3).sp)
                )
            }
            Text(
                flow.skipLabel,
                style = uthText(15.sp, FontWeight.Medium, UthSkip, 19.sp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onFinish() }
                    .padding(horizontal = 4.dp, vertical = 10.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val item = flow.pageAt(page)
            Column(Modifier.fillMaxSize()) {
                // Vùng minh hoạ – flex-grow, căn giữa
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OnboardingIllustration(item.illustration)
                }
                // Khối chữ – cao 150, gap 12
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        item.title,
                        style = uthText(28.sp, FontWeight.Bold, UthDark, 36.sp, (-0.4).sp)
                    )
                    Text(
                        item.description,
                        style = uthText(16.sp, FontWeight.Normal, UthBody, 24.sp)
                    )
                }
            }
        }

        // Chấm trang – padding 24 0 28
        Box(Modifier.padding(top = 24.dp, bottom = 28.dp)) {
            PageIndicator(count = flow.size, current = pagerState.currentPage)
        }

        Button(
            onClick = {
                if (currentPage.isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(flow.nextIndex(pagerState.currentPage)) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UthBlue, contentColor = Color.White),
            elevation = null
        ) {
            Text(
                currentPage.buttonText,
                style = uthText(17.sp, FontWeight.SemiBold, Color.White, 22.sp)
            )
        }
    }
}

@Composable
fun PageIndicator(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { i ->
            val active = i == current
            val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "dot")
            Box(
                Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) UthBlue else UthDotOff)
            )
        }
    }
}

// ============================================================
// Minh hoạ: khung 280 × 300, overlay 220 × 260 tại (30, 20), bo 28
// ============================================================
@Composable
fun OnboardingIllustration(illustration: Illustration) {
    Box(Modifier.size(width = 280.dp, height = 300.dp)) {
        Box(
            Modifier
                .offset(x = 30.dp, y = 20.dp)
                .size(width = 220.dp, height = 260.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(UthOverlay)
        )
        val contentModifier = Modifier.offset(
            x = illustration.contentOffsetX.dp,
            y = illustration.contentOffsetY.dp
        )
        when (illustration) {
            is Illustration.TaskList -> TaskListIllustration(illustration.tasks, contentModifier)
            is Illustration.Categories -> CategoryIllustration(illustration.items, contentModifier)
            is Illustration.Notification -> ReminderIllustration(illustration.reminder, contentModifier)
        }
    }
}

@Composable
private fun WhiteCard(
    radius: Dp,
    modifier: Modifier = Modifier,
    shadowAlpha: Float = 0.08f,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .cssShadow(shape, y = 6.dp, blur = 20.dp, color = Color(0xFF141428).copy(alpha = shadowAlpha))
            .clip(shape)
            .background(Color.White)
    ) { content() }
}

@Composable
private fun Bar(width: Dp) {
    Box(
        Modifier
            .size(width = width, height = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(UthBar)
    )
}

// ---- 1a: 3 thẻ việc (200 × 50, padding 14, gap 12, bo 14) ----
@Composable
private fun TaskListIllustration(tasks: List<TaskPreview>, modifier: Modifier = Modifier) {
    Column(
        modifier.width(200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tasks.forEach { task ->
            WhiteCard(14.dp, Modifier.fillMaxWidth().height(50.dp)) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (task.isDone) CheckedBox() else EmptyBox()
                    Bar(task.barWidthDp.dp)
                }
            }
        }
    }
}

@Composable
internal fun CheckedBox() {
    Box(
        Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(UthBlue),
        contentAlignment = Alignment.Center
    ) {
        // Icon 12 × 12: 20.83%–83.33% x, 29.17%–70.83% y, nét 1.5
        Canvas(Modifier.size(12.dp)) {
            val s = size.width
            val p = Path().apply {
                moveTo(s * 0.2083f, s * 0.5f)
                lineTo(s * 0.4167f, s * 0.7083f)
                lineTo(s * 0.8333f, s * 0.2917f)
            }
            drawPath(
                p, Color.White,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
internal fun EmptyBox() {
    Box(
        Modifier
            .size(22.dp)
            .border(2.dp, UthCheckBorder, RoundedCornerShape(10.dp))
    )
}

// ---- 1b: 2 thẻ nhóm (padding 16, gap 8, bo 16; cách nhau 14) ----
@Composable
private fun CategoryIllustration(items: List<CategoryPreview>, modifier: Modifier = Modifier) {
    Column(
        modifier.width(200.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items.forEach { CategoryCard(it) }
    }
}

@Composable
private fun CategoryCard(item: CategoryPreview) {
    val chipBg = Color(item.category.chipBackground)
    val chipText = Color(item.category.chipText)
    WhiteCard(16.dp, Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                item.category.label,
                style = uthText(12.sp, FontWeight.SemiBold, chipText, 15.sp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Bar(item.barWidthDp.dp)
        }
    }
}

// ---- 1c: chuông 84 + thẻ nhắc 180 × 40 (gap 18) ----
private val bellBody = PathParser().parsePathString("M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9").toPath()
private val bellClapper = PathParser().parsePathString("M13.73 21a2 2 0 0 1-3.46 0").toPath()

@Composable
private fun ReminderIllustration(reminder: Reminder, modifier: Modifier = Modifier) {
    Column(
        modifier.width(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        WhiteCard(42.dp, Modifier.size(84.dp), shadowAlpha = 0.1f) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(40.dp)) {
                    val k = size.width / 24f
                    scale(k, pivot = Offset.Zero) {
                        val stroke = Stroke(
                            width = 3.dp.toPx() / k,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                        drawPath(bellBody, UthBlue, style = stroke)
                        drawPath(bellClapper, UthBlue, style = stroke)
                    }
                }
            }
        }
        WhiteCard(14.dp, Modifier.fillMaxWidth().height(40.dp)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    reminder.displayText(),
                    style = uthText(13.sp, FontWeight.Medium, UthDark, 16.sp)
                )
            }
        }
    }
}

// ============================================================
// Preview – cùng kích thước frame Figma, dữ liệu lấy từ repository
// ============================================================
private val previewRepo = LocalUthRepository()

@Preview(name = "Start", widthDp = 400, heightDp = 860)
@Composable
fun SplashPreview() {
    SplashScreen(appInfo = previewRepo.getAppInfo(), onFinished = {})
}

@Preview(name = "1a Onboarding", widthDp = 390, heightDp = 844)
@Composable
fun OnboardingPreview() {
    OnboardingScreen(
        appInfo = previewRepo.getAppInfo(),
        flow = previewRepo.getOnboardingFlow(),
        onFinish = {}
    )
}

@Preview(name = "Minh hoạ 1b", widthDp = 280, heightDp = 300)
@Composable
fun Illustration2Preview() {
    OnboardingIllustration(previewRepo.getOnboardingFlow().pageAt(1).illustration)
}

@Preview(name = "Minh hoạ 1c", widthDp = 280, heightDp = 300)
@Composable
fun Illustration3Preview() {
    OnboardingIllustration(previewRepo.getOnboardingFlow().pageAt(2).illustration)
}
