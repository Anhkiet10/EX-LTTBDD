package com.example.bai01

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/* ============================================================
 * MODEL cho Đăng nhập / Trang chủ / Hồ sơ
 * ============================================================ */

/** Cách người dùng đăng nhập. */
enum class AuthProvider(val label: String) {
    GOOGLE("Google"),
    EMAIL("Email"),
    PHONE("số điện thoại"),
    MICROSOFT("Microsoft"),
    GITHUB("GitHub"),
    FACEBOOK("Facebook"),
    TWITTER("X (Twitter)"),
    YAHOO("Yahoo"),
    GUEST("Khách"),
}

/** Người dùng đã đăng nhập (map từ FirebaseUser). */
data class UthUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val provider: AuthProvider,
    val phone: String = "",
) {
    val isGuest: Boolean get() = provider == AuthProvider.GUEST

    private val nameParts: List<String>
        get() = displayName.trim().split(" ").filter { it.isNotBlank() }

    private val isPhoneName: Boolean
        get() = displayName.none { it.isLetter() }

    /** "Nguyễn Minh Anh" → "Minh Anh" (tên gọi thân mật). Số điện thoại giữ nguyên. */
    val shortName: String
        get() = when {
            displayName.isBlank() -> "bạn"
            isPhoneName -> displayName
            else -> nameParts.takeLast(2).joinToString(" ")
        }

    /** "Minh Anh" → "MA"; số điện thoại → 2 số cuối. */
    val initials: String
        get() = when {
            displayName.isBlank() -> "?"
            isPhoneName -> displayName.filter { it.isDigit() }.takeLast(2)
            else -> nameParts.takeLast(2).joinToString("") { it.first().uppercase() }
        }

    /** Dòng phụ dưới tên trong Hồ sơ: email hoặc số điện thoại. */
    val contact: String
        get() = email.ifBlank { if (displayName != localPhone(phone)) localPhone(phone) else "" }

    fun linkedLabel(): String = when (provider) {
        AuthProvider.GUEST -> "Đang dùng thử – chưa liên kết tài khoản"
        AuthProvider.GOOGLE -> "Đã liên kết tài khoản Google"
        AuthProvider.MICROSOFT -> "Đã liên kết tài khoản Microsoft"
        AuthProvider.GITHUB -> "Đã liên kết tài khoản GitHub"
        AuthProvider.FACEBOOK -> "Đã liên kết tài khoản Facebook"
        AuthProvider.TWITTER -> "Đã liên kết tài khoản X (Twitter)"
        AuthProvider.YAHOO -> "Đã liên kết tài khoản Yahoo"
        AuthProvider.EMAIL -> "Đăng nhập bằng Email"
        AuthProvider.PHONE -> "Đăng nhập bằng số điện thoại"
    }
}

/** "+84912345678" → "0912 345 678" */
fun localPhone(e164: String): String {
    if (e164.isBlank()) return ""
    val digits = if (e164.startsWith("+84")) "0" + e164.removePrefix("+84") else e164
    return if (digits.length == 10) "${digits.take(4)} ${digits.substring(4, 7)} ${digits.substring(7)}" else digits
}

/** "0912 345 678" / "912345678" / "+84912345678" → "+84912345678"; sai định dạng → null */
fun normalizeVietnamPhone(input: String): String? {
    val s = input.filter { it.isDigit() || it == '+' }
    val e164 = when {
        s.startsWith("+") -> s
        s.startsWith("84") && s.length >= 11 -> "+$s"
        s.startsWith("0") -> "+84" + s.drop(1)
        else -> "+84$s"
    }
    val ok = Regex("^\\+\\d{10,14}$").matches(e164) && (!e164.startsWith("+84") || e164.length == 12)
    return if (ok) e164 else null
}

/** Một việc trong ngày (lưu trên Firestore: users/{uid}/tasks/{id}). */
data class TodoTask(
    val id: String,
    val title: String,
    val category: Category,
    val time: String,
    val isDone: Boolean,
    /** Ngày của việc, dạng "yyyy-MM-dd". */
    val date: String = todayKey(),
) {
    fun toggled(): TodoTask = copy(isDone = !isDone)
}

/** Tiến độ hôm nay – tính từ danh sách việc. */
class TodayProgress(tasks: List<TodoTask>) {
    val total: Int = tasks.size
    val done: Int = tasks.count { it.isDone }
    val pending: Int get() = total - done
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
    val percent: Int get() = (fraction * 100).toInt()

    fun label(): String = "$done/$total việc"

    fun message(): String = when {
        total == 0 -> "Hôm nay chưa có việc nào."
        done == 0 -> "Bắt đầu từ việc nhỏ nhất nhé."
        done == total -> "Xong hết rồi, tuyệt vời!"
        fraction >= 0.5f -> "Đã hơn nửa chặng, cố lên!"
        else -> "Khởi đầu tốt, tiếp tục nào!"
    }

    companion object {
        fun remainingIn(tasks: List<TodoTask>, category: Category): Int =
            tasks.count { it.category == category && !it.isDone }
    }
}

/** Số việc xong theo từng ngày trong tuần (T2 … CN). */
class WeeklyStats(
    /** Số việc đã xong theo ngày, key "yyyy-MM-dd" (lấy từ Firestore). */
    private val doneByDate: Map<String, Int>,
) {
    /** 7 ngày của tuần này, Thứ Hai → Chủ Nhật. */
    val dates: List<String> = weekDateKeys()

    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    /** 0 = Thứ Hai … 6 = Chủ Nhật. */
    val todayIndex: Int
        get() = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 6
        }

    /** Số việc xong mỗi ngày; ngày tương lai = null. */
    fun countsWithToday(todayDone: Int): List<Int?> = dayLabels.indices.map { i ->
        when {
            i < todayIndex -> doneByDate[dates[i]] ?: 0
            i == todayIndex -> todayDone
            else -> null
        }
    }
}

private fun dateKey(cal: Calendar): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

/** Hôm nay, dạng "yyyy-MM-dd". */
fun todayKey(): String = dateKey(Calendar.getInstance())

/** 7 ngày của tuần hiện tại (Thứ Hai → Chủ Nhật), dạng "yyyy-MM-dd". */
fun weekDateKeys(): List<String> {
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)               // CN=1, T2=2 … T7=7
    val fromMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_MONTH, -fromMonday)
    return List(7) { i ->
        if (i > 0) cal.add(Calendar.DAY_OF_MONTH, 1)
        dateKey(cal)
    }
}

/** Trang web của app – lưu trữ trên Firebase Hosting (thư mục source/hosting). */
object HostingUrls {
    const val HOME = "https://baitap-b5d0b.web.app"
    const val PRIVACY = "$HOME/privacy"
    const val TERMS = "$HOME/terms"
}

/** Chữ trên màn đăng nhập. */
class LoginContent(
    val title: String,
    val description: String,
    val googleLabel: String,
    val microsoftLabel: String,
    val githubLabel: String,
    val facebookLabel: String,
    val twitterLabel: String,
    val yahooLabel: String,
    val emailLabel: String,
    val phoneLabel: String,
    val loadingLabel: String,
    val orLabel: String,
    val guestLabel: String,
    val termsPrefix: String,
    val termsLink: String,
    val termsMiddle: String,
    val privacyLink: String,
    val termsSuffix: String,
)

/** "Thứ Ba, 20 tháng 10" */
fun vietnameseToday(cal: Calendar = Calendar.getInstance()): String {
    val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Thứ Hai"
        Calendar.TUESDAY -> "Thứ Ba"
        Calendar.WEDNESDAY -> "Thứ Tư"
        Calendar.THURSDAY -> "Thứ Năm"
        Calendar.FRIDAY -> "Thứ Sáu"
        Calendar.SATURDAY -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }
    return "$dow, ${cal.get(Calendar.DAY_OF_MONTH)} tháng ${cal.get(Calendar.MONTH) + 1}"
}
