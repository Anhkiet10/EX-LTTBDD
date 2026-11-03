package com.example.bai01

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.delay

/* ============================================================
 * AUTH – lớp trừu tượng cho việc đăng nhập.
 *
 * Bản thật: [FirebaseAuthService] (Google, Email/Mật khẩu, Số điện thoại, Ẩn danh)
 * Bản giả lập: [FakeAuthService] – chạy giao diện không cần mạng/Firebase.
 * ============================================================ */

/** Kết quả một lần đăng nhập. */
sealed class AuthResult {
    data class Success(val user: UthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** Kết quả gửi mã OTP qua SMS. */
sealed class PhoneCodeResult {
    /** Đã gửi mã → chờ người dùng nhập. */
    data class CodeSent(val verificationId: String) : PhoneCodeResult()

    /** Máy tự đọc được mã và đã đăng nhập luôn. */
    data class Verified(val user: UthUser) : PhoneCodeResult()

    data class Error(val message: String) : PhoneCodeResult()
}

/** Kết quả một thao tác không cần trả về người dùng (vd: gửi mail đặt lại mật khẩu). */
data class AuthMessage(val success: Boolean, val message: String)

interface AuthService {
    /** Người dùng đang đăng nhập (null nếu chưa). */
    val currentUser: UthUser?

    /** Google – cần Context của Activity cho Credential Manager. */
    suspend fun signInWithGoogle(context: Context): AuthResult

    /** Microsoft (Outlook/Hotmail/Office 365) – mở trang đăng nhập Microsoft trong Custom Tab. */
    suspend fun signInWithMicrosoft(activity: Activity): AuthResult

    /** GitHub – mở trang đăng nhập GitHub trong Custom Tab. */
    suspend fun signInWithGitHub(activity: Activity): AuthResult

    /** X (Twitter) – mở trang đăng nhập X trong Custom Tab. */
    suspend fun signInWithTwitter(activity: Activity): AuthResult

    /** Yahoo – mở trang đăng nhập Yahoo trong Custom Tab. */
    suspend fun signInWithYahoo(activity: Activity): AuthResult

    /** Facebook – nhận access token từ Facebook Login SDK rồi đăng nhập Firebase. */
    suspend fun signInWithFacebook(accessToken: String): AuthResult

    /** Email + mật khẩu. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult
    suspend fun sendPasswordReset(email: String): AuthMessage

    /** Số điện thoại: bước 1 gửi mã, bước 2 xác nhận mã. [phone] dạng +84xxxxxxxxx */
    suspend fun sendPhoneCode(activity: Activity, phone: String): PhoneCodeResult
    suspend fun verifyPhoneCode(verificationId: String, code: String): AuthResult

    /** "Dùng thử không cần tài khoản". */
    suspend fun signInAsGuest(): AuthResult

    suspend fun signOut()
}

/**
 * Bản giả lập để dựng giao diện khi chưa có Firebase.
 * - Mã OTP đúng: 123456
 * @param googleShouldFail đặt true để xem luồng "đăng nhập lỗi → thông báo lỗi".
 */
class FakeAuthService(
    private val googleShouldFail: Boolean = false,
) : AuthService {

    override var currentUser: UthUser? = null
        private set

    private fun success(user: UthUser): AuthResult {
        currentUser = user
        return AuthResult.Success(user)
    }

    override suspend fun signInWithGoogle(context: Context): AuthResult {
        delay(1200)
        if (googleShouldFail) {
            return AuthResult.Error("Không thể đăng nhập bằng Google. Vui lòng kiểm tra kết nối mạng và thử lại.")
        }
        return success(UthUser("fake-google", "Minh Anh", "minhanh@example.com", AuthProvider.GOOGLE))
    }

    override suspend fun signInWithMicrosoft(activity: Activity): AuthResult {
        delay(1000)
        return success(UthUser("fake-ms", "Minh Anh", "minhanh@outlook.com", AuthProvider.MICROSOFT))
    }

    override suspend fun signInWithGitHub(activity: Activity): AuthResult {
        delay(1000)
        return success(UthUser("fake-gh", "Minh Anh", "minhanh@users.noreply.github.com", AuthProvider.GITHUB))
    }

    override suspend fun signInWithTwitter(activity: Activity): AuthResult {
        delay(1000)
        return success(UthUser("fake-x", "Minh Anh", "", AuthProvider.TWITTER))
    }

    override suspend fun signInWithYahoo(activity: Activity): AuthResult {
        delay(1000)
        return success(UthUser("fake-yahoo", "Minh Anh", "minhanh@yahoo.com", AuthProvider.YAHOO))
    }

    override suspend fun signInWithFacebook(accessToken: String): AuthResult {
        delay(800)
        return success(UthUser("fake-fb", "Minh Anh", "minhanh@example.com", AuthProvider.FACEBOOK))
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        delay(800)
        if (password.length < 6) return AuthResult.Error("Email hoặc mật khẩu không đúng.")
        return success(UthUser("fake-email", email.substringBefore("@"), email, AuthProvider.EMAIL))
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult {
        delay(800)
        return success(UthUser("fake-email", name, email, AuthProvider.EMAIL))
    }

    override suspend fun sendPasswordReset(email: String): AuthMessage {
        delay(600)
        return AuthMessage(true, "Đã gửi email đặt lại mật khẩu tới $email.")
    }

    override suspend fun sendPhoneCode(activity: Activity, phone: String): PhoneCodeResult {
        delay(800)
        return PhoneCodeResult.CodeSent("fake-verification")
    }

    override suspend fun verifyPhoneCode(verificationId: String, code: String): AuthResult {
        delay(600)
        if (code != "123456") return AuthResult.Error("Mã xác nhận không đúng. Vui lòng kiểm tra lại.")
        return success(UthUser("fake-phone", localPhone("+84912345678"), "", AuthProvider.PHONE, phone = "+84912345678"))
    }

    override suspend fun signInAsGuest(): AuthResult {
        delay(500)
        return success(UthUser("fake-guest", "Khách", "", AuthProvider.GUEST))
    }

    override suspend fun signOut() {
        currentUser = null
    }
}
