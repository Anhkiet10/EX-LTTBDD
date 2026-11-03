package com.example.bai3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bai3.model.ResetPasswordData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Nơi giữ dữ liệu dùng chung của cả flow.
 *
 * ĐÓNG GÓI (Encapsulation):
 * - _data là private, chỉ ViewModel được sửa.
 * - Bên ngoài chỉ đọc được qua [data] (StateFlow chỉ đọc)
 *   và chỉ thay đổi được qua các hàm setXxx().
 *
 * ViewModel còn sống qua xoay màn hình nên dữ liệu không bị mất.
 */
class ResetPasswordViewModel : ViewModel() {

    private val _data = MutableStateFlow(ResetPasswordData())
    val data: StateFlow<ResetPasswordData> = _data.asStateFlow()

    // Thông tin đã gửi khi bấm Summit, hiển thị lại ở màn Forget Password.
    // null = chưa submit lần nào.
    private val _submitted = MutableStateFlow<ResetPasswordData?>(null)
    val submitted: StateFlow<ResetPasswordData?> = _submitted.asStateFlow()

    fun setEmail(email: String) = _data.update { it.copy(email = email.trim()) }

    fun setCode(code: String) = _data.update { it.copy(code = code) }

    fun setPassword(password: String) = _data.update { it.copy(password = password) }

    /**
     * Gọi khi bấm Summit:
     * - lưu lại dữ liệu vừa nhập vào [submitted] để màn đầu hiển thị
     * - xoá form để lần nhập sau bắt đầu từ trống
     */
    fun submit() {
        _submitted.value = _data.value
        reset()
    }

    /** Xoá dữ liệu form (email, code, password). */
    fun reset() {
        _data.value = ResetPasswordData()
    }

    companion object {
        /** Figma có 5 ô nhập mã. Đổi số này nếu đề yêu cầu khác. */
        const val CODE_LENGTH = 5
    }
}
