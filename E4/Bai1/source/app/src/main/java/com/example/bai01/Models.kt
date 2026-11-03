package com.example.bai01

/* ============================================================
 * MODEL (OOP) – toàn bộ dữ liệu của app được mô tả bằng lớp/đối tượng
 * ============================================================ */

/** Thông tin chung của ứng dụng (màn Start). */
class AppInfo(
    val brandName: String,
    val brandSuffix: String,
    val slogan: String,
    private val versionName: String,
    val splashDurationMillis: Int,
) {
    val fullName: String get() = "$brandName $brandSuffix"
    fun versionLabel(): String = "Phiên bản $versionName"
}

/** Một công việc hiển thị trong hình minh hoạ (1a). */
data class TaskPreview(
    val isDone: Boolean,
    val barWidthDp: Int,
) {
    fun toggled(): TaskPreview = copy(isDone = !isDone)
}

/** Nhóm công việc – mỗi nhóm có nhãn và màu riêng (1b). */
enum class Category(
    val label: String,
    val chipBackground: Long,
    val chipText: Long,
) {
    STUDY("Học tập", 0xFFE6EDFF, 0xFF1F4FD1),
    LIFE("Cuộc sống", 0xFFFFEBD9, 0xFFA24A08),
}

/** Thẻ nhóm trong minh hoạ: một nhóm + độ dài thanh mô tả. */
data class CategoryPreview(
    val category: Category,
    val barWidthDp: Int,
)

/** Một lời nhắc (1c). */
data class Reminder(
    val title: String,
    val time: String,
) {
    fun displayText(): String = "$title lúc $time"
}

/* ---------- Minh hoạ: lớp cha trừu tượng + các lớp con (đa hình) ---------- */
sealed class Illustration {
    /** Toạ độ nội dung bên trong khung 280 × 300 (theo Figma). */
    abstract val contentOffsetX: Int
    abstract val contentOffsetY: Int

    class TaskList(val tasks: List<TaskPreview>) : Illustration() {
        override val contentOffsetX = 40
        override val contentOffsetY = 70
        fun doneCount(): Int = tasks.count { it.isDone }
    }

    class Categories(val items: List<CategoryPreview>) : Illustration() {
        override val contentOffsetX = 40
        override val contentOffsetY = 80
    }

    class Notification(val reminder: Reminder) : Illustration() {
        override val contentOffsetX = 50
        override val contentOffsetY = 90
    }
}

/* ---------- Trang onboarding ---------- */
open class OnboardingPage(
    val step: Int,
    val title: String,
    val description: String,
    val illustration: Illustration,
) {
    open val buttonText: String = "Tiếp tục"
    open val isLast: Boolean = false
}

/** Trang cuối: ghi đè nút và cờ isLast (kế thừa). */
class FinalOnboardingPage(
    step: Int,
    title: String,
    description: String,
    illustration: Illustration,
) : OnboardingPage(step, title, description, illustration) {
    override val buttonText: String = "Bắt đầu ngay"
    override val isLast: Boolean = true
}

/** Quản lý luồng onboarding – đóng gói danh sách trang. */
class OnboardingFlow(
    private val pages: List<OnboardingPage>,
    val skipLabel: String,
) {
    val size: Int get() = pages.size
    fun pageAt(index: Int): OnboardingPage = pages[index.coerceIn(0, pages.lastIndex)]
    fun nextIndex(current: Int): Int = (current + 1).coerceAtMost(pages.lastIndex)
}

/* ---------- Các màn hình (route cho Navigation) ---------- */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object EmailLogin : Screen("login_email")
    data object PhoneLogin : Screen("login_phone")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
}
