package com.example.bai3.model

/**
 * Dữ liệu đi xuyên suốt 4 màn hình của flow "Nav (Data Flow)".
 * - Màn 1 (Forget Password) điền email
 * - Màn 2 (Verify Code) điền code
 * - Màn 3 (Create new password) điền password
 * - Màn 4 (Confirm) đọc lại cả 3 giá trị
 *
 * Dùng data class vì: tự sinh equals/hashCode/toString và hàm copy()
 * giúp tạo bản sao bất biến khi cập nhật từng trường.
 */
data class ResetPasswordData(
    val email: String = "",
    val code: String = "",
    val password: String = ""
)
