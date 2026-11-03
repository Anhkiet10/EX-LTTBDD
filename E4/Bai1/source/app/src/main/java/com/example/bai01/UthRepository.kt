package com.example.bai01

/**
 * Nguồn dữ liệu của app. Giao diện chỉ đọc dữ liệu qua lớp này,
 * không viết cứng chữ/số trong màn hình.
 */
interface UthRepository {
    fun getAppInfo(): AppInfo
    fun getOnboardingFlow(): OnboardingFlow
    fun getLoginContent(): LoginContent
    fun getTodayTasks(): List<TodoTask>
    fun getWeeklyStats(): WeeklyStats
}

class LocalUthRepository : UthRepository {

    override fun getAppInfo(): AppInfo = AppInfo(
        brandName = "UTH",
        brandSuffix = "Todo",
        slogan = "Việc học, việc nhà — gọn gàng mỗi ngày",
        versionName = "2.1.0",
        splashDurationMillis = 2000,
    )

    override fun getOnboardingFlow(): OnboardingFlow = OnboardingFlow(
        listOf(
            OnboardingPage(
                step = 1,
                title = "Mọi việc, gọn trong một chỗ",
                description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
                illustration = Illustration.TaskList(
                    listOf(
                        TaskPreview(isDone = true, barWidthDp = 110),
                        TaskPreview(isDone = false, barWidthDp = 90),
                        TaskPreview(isDone = false, barWidthDp = 120),
                    )
                ),
            ),
            OnboardingPage(
                step = 2,
                title = "Chia theo Học tập và Cuộc sống",
                description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì.",
                illustration = Illustration.Categories(
                    listOf(
                        CategoryPreview(Category.STUDY, barWidthDp = 140),
                        CategoryPreview(Category.LIFE, barWidthDp = 110),
                    )
                ),
            ),
            FinalOnboardingPage(
                step = 3,
                title = "Nhắc đúng lúc",
                description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
                illustration = Illustration.Notification(
                    Reminder(title = "Ôn bài Toán", time = "19:00")
                ),
            ),
        ),
        skipLabel = "Bỏ qua",
    )

    override fun getLoginContent(): LoginContent = LoginContent(
        title = "Chào mừng đến với\nUTH Todo",
        description = "Đăng nhập để lưu công việc và đồng bộ trên mọi thiết bị của bạn.",
        googleLabel = "Tiếp tục với Google",
        microsoftLabel = "Tiếp tục với Microsoft",
        githubLabel = "Tiếp tục với GitHub",
        facebookLabel = "Tiếp tục với Facebook",
        twitterLabel = "Tiếp tục với X (Twitter)",
        yahooLabel = "Tiếp tục với Yahoo",
        emailLabel = "Tiếp tục với Email",
        phoneLabel = "Tiếp tục với số điện thoại",
        loadingLabel = "Đang đăng nhập…",
        orLabel = "hoặc",
        guestLabel = "Dùng thử không cần tài khoản",
        termsPrefix = "Khi tiếp tục, bạn đồng ý với ",
        termsLink = "Điều khoản sử dụng",
        termsMiddle = " và ",
        privacyLink = "Chính sách quyền riêng tư",
        termsSuffix = " của UTH Todo.",
    )

    override fun getTodayTasks(): List<TodoTask> = listOf(
        TodoTask("1", "Tập thể dục 30 phút", Category.LIFE, "06:30", isDone = true),
        TodoTask("2", "Nộp bài tập Lập trình di động", Category.STUDY, "09:00", isDone = true),
        TodoTask("3", "Ôn chương 3 – Cấu trúc dữ liệu", Category.STUDY, "14:00", isDone = false),
        TodoTask("4", "Đi chợ mua đồ cho tuần", Category.LIFE, "17:30", isDone = false),
    )

    /** Dữ liệu mẫu cho Preview – app thật lấy từ Firestore. */
    override fun getWeeklyStats(): WeeklyStats = WeeklyStats(
        doneByDate = weekDateKeys().zip(listOf(3, 4, 2, 5, 3, 1, 0)).toMap(),
    )
}
