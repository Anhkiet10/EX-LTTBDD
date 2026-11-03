package com.example.bai3.navigation

/**
 * Danh sách các màn hình (route) trong flow.
 *
 * KẾ THỪA (Inheritance): mỗi object là một lớp con của Screen.
 * sealed class đảm bảo chỉ có đúng 4 màn hình này, tránh gõ sai chuỗi route
 * ở nhiều nơi (chỉ cần gọi Screen.VerifyCode.route).
 */
sealed class Screen(val route: String) {
    data object ForgetPassword : Screen("forget_password")
    data object VerifyCode : Screen("verify_code")
    data object NewPassword : Screen("new_password")
    data object Confirm : Screen("confirm")
}
