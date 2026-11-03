package com.example.bai3.model

import android.util.Patterns

/**
 * TRỪU TƯỢNG (Abstraction): chỉ định nghĩa "kiểm tra dữ liệu" là gì,
 * không quan tâm kiểm tra cụ thể ra sao.
 *
 * @return null nếu hợp lệ, ngược lại trả về câu thông báo lỗi.
 */
interface Validator {
    fun validate(input: String): String?
}

/* ĐA HÌNH (Polymorphism): mỗi lớp dưới đây cài đặt validate() theo cách riêng,
   nhưng màn hình chỉ cần gọi validator.validate(...) giống nhau. */

/** Màn 1: email phải đúng định dạng. */
class EmailValidator : Validator {
    override fun validate(input: String): String? =
        if (Patterns.EMAIL_ADDRESS.matcher(input.trim()).matches()) null
        else "Email không hợp lệ"
}

/** Màn 2: mã phải đủ [length] chữ số. */
class CodeValidator(private val length: Int) : Validator {
    override fun validate(input: String): String? =
        if (input.length == length && input.all { it.isDigit() }) null
        else "Mã xác thực phải gồm $length chữ số"
}

/** Màn 3: mật khẩu tối thiểu 6 ký tự và phải khớp với ô Confirm Password. */
class PasswordValidator(private val confirm: String) : Validator {
    override fun validate(input: String): String? = when {
        input.length < MIN_LENGTH -> "Mật khẩu tối thiểu $MIN_LENGTH ký tự"
        input != confirm -> "Mật khẩu xác nhận không khớp"
        else -> null
    }

    companion object {
        const val MIN_LENGTH = 6
    }
}
