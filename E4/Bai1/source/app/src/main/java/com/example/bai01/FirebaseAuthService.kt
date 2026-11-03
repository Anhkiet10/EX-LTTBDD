package com.example.bai01

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.facebook.login.LoginManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/* ============================================================
 * Đăng nhập thật bằng Firebase Authentication
 *  - Google: Credential Manager → ID token → GoogleAuthProvider
 *  - Email/Mật khẩu: signIn / createUser / sendPasswordResetEmail
 *  - Số điện thoại: PhoneAuthProvider (OTP qua SMS)
 *  - Dùng thử: signInAnonymously()
 * ============================================================ */
class FirebaseAuthService(
    private val appContext: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthService {

    init {
        auth.setLanguageCode("vi") // SMS và email gửi bằng tiếng Việt
    }

    override val currentUser: UthUser?
        get() = auth.currentUser?.toUthUser()

    // ---------------- Google ----------------
    override suspend fun signInWithGoogle(context: Context): AuthResult {
        val webClientId = webClientId(context)
            ?: return AuthResult.Error(
                "Chưa có Web client ID. Hãy bật Google trong Firebase Authentication, " +
                    "thêm SHA-1 rồi tải lại google-services.json vào thư mục app."
            )

        return try {
            val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()
            val credential = CredentialManager.create(context)
                .getCredential(context, request)
                .credential

            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return AuthResult.Error("Không nhận được tài khoản Google hợp lệ.")
            }

            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(firebaseCredential).awaitResult().user
                ?: return AuthResult.Error("Đăng nhập không thành công, vui lòng thử lại.")
            AuthResult.Success(user.toUthUser())
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("Bạn đã huỷ đăng nhập Google.")
        } catch (e: NoCredentialException) {
            AuthResult.Error("Không tìm thấy tài khoản Google trên thiết bị. Hãy thêm tài khoản Google trong Cài đặt máy.")
        } catch (e: GetCredentialException) {
            AuthResult.Error("Đăng nhập Google thất bại: ${e.message ?: e.type}")
        } catch (e: Exception) {
            AuthResult.Error(friendlyMessage(e))
        }
    }

    // ---------------- Microsoft / GitHub (OAuth qua trình duyệt) ----------------
    override suspend fun signInWithMicrosoft(activity: Activity): AuthResult =
        signInWithOAuth(
            activity,
            OAuthProvider.newBuilder(MICROSOFT_PROVIDER_ID)
                .addCustomParameter("prompt", "select_account") // luôn cho chọn tài khoản
                .setScopes(listOf("openid", "email", "profile", "User.Read"))
                .build(),
            providerName = "Microsoft",
        )

    override suspend fun signInWithGitHub(activity: Activity): AuthResult =
        signInWithOAuth(
            activity,
            OAuthProvider.newBuilder(GITHUB_PROVIDER_ID)
                .setScopes(listOf("read:user", "user:email")) // lấy tên + email (kể cả email ẩn)
                .build(),
            providerName = "GitHub",
        )

    override suspend fun signInWithTwitter(activity: Activity): AuthResult =
        signInWithOAuth(
            activity,
            OAuthProvider.newBuilder(TWITTER_PROVIDER_ID)
                .addCustomParameter("lang", "vi")
                .build(),
            providerName = "X (Twitter)",
        )

    override suspend fun signInWithYahoo(activity: Activity): AuthResult =
        signInWithOAuth(
            activity,
            OAuthProvider.newBuilder(YAHOO_PROVIDER_ID)
                .addCustomParameter("prompt", "login") // luôn hỏi đăng nhập / chọn tài khoản
                .addCustomParameter("language", "vi-VN")
                .setScopes(listOf("openid", "email", "profile"))
                .build(),
            providerName = "Yahoo",
        )

    // ---------------- Facebook ----------------
    override suspend fun signInWithFacebook(accessToken: String): AuthResult = try {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val user = auth.signInWithCredential(credential).awaitResult().user
        if (user != null) AuthResult.Success(user.toUthUser())
        else AuthResult.Error("Đăng nhập Facebook không thành công, vui lòng thử lại.")
    } catch (e: Exception) {
        runCatching { LoginManager.getInstance().logOut() }
        AuthResult.Error(friendlyMessage(e, oauthName = "Facebook"))
    }

    private suspend fun signInWithOAuth(
        activity: Activity,
        provider: OAuthProvider,
        providerName: String,
    ): AuthResult = try {
        // Nếu app bị đóng giữa chừng khi đang ở trang đăng nhập thì lấy lại kết quả cũ
        val pending = auth.pendingAuthResult
        val result = pending?.awaitResult()
            ?: auth.startActivityForSignInWithProvider(activity, provider).awaitResult()
        val user = result.user
        if (user != null) AuthResult.Success(user.toUthUser())
        else AuthResult.Error("Đăng nhập $providerName không thành công, vui lòng thử lại.")
    } catch (e: Exception) {
        AuthResult.Error(friendlyMessage(e, oauthName = providerName))
    }

    // ---------------- Email / Mật khẩu ----------------
    override suspend fun signInWithEmail(email: String, password: String): AuthResult = try {
        val user = auth.signInWithEmailAndPassword(email, password).awaitResult().user
        if (user != null) AuthResult.Success(user.toUthUser())
        else AuthResult.Error("Đăng nhập không thành công, vui lòng thử lại.")
    } catch (e: Exception) {
        AuthResult.Error(friendlyMessage(e))
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult = try {
        val user = auth.createUserWithEmailAndPassword(email, password).awaitResult().user
            ?: throw IllegalStateException("Không tạo được tài khoản, vui lòng thử lại.")
        // Lưu họ tên vào hồ sơ Firebase
        val profile = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(profile).awaitResult()
        AuthResult.Success(user.toUthUser().copy(displayName = name))
    } catch (e: Exception) {
        AuthResult.Error(friendlyMessage(e))
    }

    override suspend fun sendPasswordReset(email: String): AuthMessage = try {
        auth.sendPasswordResetEmail(email).awaitResult()
        AuthMessage(true, "Nếu $email đã đăng ký, bạn sẽ nhận được email hướng dẫn đặt lại mật khẩu. Hãy kiểm tra cả mục Spam.")
    } catch (e: Exception) {
        AuthMessage(false, friendlyMessage(e))
    }

    // ---------------- Số điện thoại ----------------
    override suspend fun sendPhoneCode(activity: Activity, phone: String): PhoneCodeResult =
        suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    if (cont.isActive) cont.resume(PhoneCodeResult.CodeSent(verificationId))
                }

                // Một số máy tự đọc được SMS → đăng nhập luôn không cần nhập mã
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { result ->
                            val user = result.user
                            if (cont.isActive) cont.resume(
                                if (user != null) PhoneCodeResult.Verified(user.toUthUser())
                                else PhoneCodeResult.Error("Đăng nhập không thành công, vui lòng thử lại.")
                            )
                        }
                        .addOnFailureListener { e ->
                            if (cont.isActive) cont.resume(PhoneCodeResult.Error(friendlyMessage(e, phone = true)))
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resume(PhoneCodeResult.Error(friendlyMessage(e, phone = true)))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    override suspend fun verifyPhoneCode(verificationId: String, code: String): AuthResult = try {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val user = auth.signInWithCredential(credential).awaitResult().user
        if (user != null) AuthResult.Success(user.toUthUser())
        else AuthResult.Error("Đăng nhập không thành công, vui lòng thử lại.")
    } catch (e: Exception) {
        AuthResult.Error(friendlyMessage(e, phone = true))
    }

    // ---------------- Dùng thử ----------------
    override suspend fun signInAsGuest(): AuthResult = try {
        val user = auth.signInAnonymously().awaitResult().user
        if (user != null) AuthResult.Success(user.toUthUser())
        else AuthResult.Error("Không thể vào chế độ dùng thử.")
    } catch (e: Exception) {
        AuthResult.Error(friendlyMessage(e))
    }

    // ---------------- Đăng xuất ----------------
    override suspend fun signOut() {
        if (auth.currentUser?.isAnonymous == true) {
            // Tài khoản ẩn danh không đăng nhập lại được → xoá luôn cho gọn
            runCatching { auth.currentUser?.delete()?.awaitResult() }
        }
        auth.signOut()
        runCatching { LoginManager.getInstance().logOut() } // thoát cả phiên Facebook
        runCatching {
            CredentialManager.create(appContext).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    /** default_web_client_id do plugin google-services sinh ra từ google-services.json. */
    private fun webClientId(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }
}

/** Đổi lỗi Firebase thành câu thông báo tiếng Việt dễ hiểu. */
private const val MICROSOFT_PROVIDER_ID = "microsoft.com"
private const val GITHUB_PROVIDER_ID = "github.com"
private const val TWITTER_PROVIDER_ID = "twitter.com"
private const val YAHOO_PROVIDER_ID = "yahoo.com"

private fun friendlyMessage(e: Exception, phone: Boolean = false, oauthName: String? = null): String {
    val code = (e as? FirebaseAuthException)?.errorCode.orEmpty()
    val raw = e.message.orEmpty()
    return when {
        oauthName != null && code == "ERROR_WEB_CONTEXT_CANCELED" ->
            "Bạn đã huỷ đăng nhập $oauthName."
        code == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
            "Email của tài khoản này đã được dùng để đăng nhập bằng cách khác (Google hoặc Email). Hãy đăng nhập bằng cách đó."
        oauthName == "Microsoft" && (raw.contains("invalid_client") || raw.contains("unauthorized_client") || raw.contains("AADSTS")) ->
            "Cấu hình Microsoft chưa đúng (Application ID, Client secret hoặc Redirect URI). Hãy kiểm tra lại trên Azure và Firebase."
        oauthName == "GitHub" && (raw.contains("redirect_uri") || raw.contains("client_id") || raw.contains("incorrect_client_credentials")) ->
            "Cấu hình GitHub chưa đúng (Client ID, Client secret hoặc Callback URL). Hãy kiểm tra lại trên GitHub và Firebase."
        oauthName == "X (Twitter)" && (raw.contains("callback", ignoreCase = true) || raw.contains("consumer", ignoreCase = true) || raw.contains("401")) ->
            "Cấu hình X (Twitter) chưa đúng (API Key, API Secret hoặc Callback URI). Hãy kiểm tra lại trên X Developer và Firebase." +
                "\n\nChi tiết: ${raw.take(300)}"
        oauthName == "Yahoo" && (raw.contains("invalid_client") || raw.contains("redirect_uri") || raw.contains("invalid_request") || raw.contains("unauthorized")) ->
            "Cấu hình Yahoo chưa đúng (Client ID, Client Secret hoặc Redirect URI). Hãy kiểm tra lại trên Yahoo Developer và Firebase." +
                "\n\nChi tiết: ${raw.take(300)}"
        code == "ERROR_OPERATION_NOT_ALLOWED" && oauthName != null ->
            "Đăng nhập $oauthName chưa được bật trong Firebase Console."
        phone && raw.contains("BILLING_NOT_ENABLED") ->
            "Số này chưa có trong danh sách số test của Firebase, mà gói miễn phí không gửi được SMS thật. " +
                "Hãy thêm số dạng +84… vào Authentication → Phone → Phone numbers for testing."
        e is FirebaseNetworkException ->
            "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."
        e is FirebaseTooManyRequestsException ->
            "Bạn thử quá nhiều lần. Vui lòng đợi một lúc rồi thử lại."
        e is FirebaseAuthWeakPasswordException ->
            "Mật khẩu quá yếu, cần ít nhất 6 ký tự."
        e is FirebaseAuthUserCollisionException ->
            "Email này đã được đăng ký. Hãy chuyển sang Đăng nhập."
        e is FirebaseAuthInvalidUserException ->
            "Tài khoản không tồn tại hoặc đã bị vô hiệu hoá."
        phone && code == "ERROR_INVALID_VERIFICATION_CODE" ->
            "Mã xác nhận không đúng. Vui lòng kiểm tra lại."
        phone && code == "ERROR_SESSION_EXPIRED" ->
            "Mã xác nhận đã hết hạn. Hãy bấm \"Gửi lại\"."
        phone && code == "ERROR_INVALID_PHONE_NUMBER" ->
            "Số điện thoại không hợp lệ."
        phone && e is FirebaseAuthInvalidCredentialsException ->
            "Số điện thoại hoặc mã xác nhận không đúng."
        code == "ERROR_INVALID_EMAIL" ->
            "Email không hợp lệ."
        e is FirebaseAuthInvalidCredentialsException ->
            "Email hoặc mật khẩu không đúng."
        code == "ERROR_OPERATION_NOT_ALLOWED" ->
            "Cách đăng nhập này chưa được bật trong Firebase Console."
        else -> e.localizedMessage ?: "Đã có lỗi xảy ra, vui lòng thử lại."
    }
}

/** FirebaseUser → model của app. */
private fun FirebaseUser.toUthUser(): UthUser {
    val ids = providerData.map { it.providerId }
    val provider = when {
        isAnonymous -> AuthProvider.GUEST
        GoogleAuthProvider.PROVIDER_ID in ids -> AuthProvider.GOOGLE
        MICROSOFT_PROVIDER_ID in ids -> AuthProvider.MICROSOFT
        GITHUB_PROVIDER_ID in ids -> AuthProvider.GITHUB
        TWITTER_PROVIDER_ID in ids -> AuthProvider.TWITTER
        YAHOO_PROVIDER_ID in ids -> AuthProvider.YAHOO
        FacebookAuthProvider.PROVIDER_ID in ids -> AuthProvider.FACEBOOK
        PhoneAuthProvider.PROVIDER_ID in ids -> AuthProvider.PHONE
        else -> AuthProvider.EMAIL
    }
    val mail = email?.takeIf { it.isNotBlank() }
        ?: providerData.firstNotNullOfOrNull { it.email?.takeIf { e -> e.isNotBlank() } }
        ?: ""
    val tel = phoneNumber.orEmpty()
    val name = displayName?.takeIf { it.isNotBlank() }
        ?: providerData.firstNotNullOfOrNull { it.displayName?.takeIf { n -> n.isNotBlank() } }
    val shown = when {
        isAnonymous -> "Khách"
        !name.isNullOrBlank() -> name
        mail.isNotBlank() -> mail.substringBefore("@")
        tel.isNotBlank() -> localPhone(tel)
        else -> "Bạn"
    }
    return UthUser(uid = uid, displayName = shown, email = mail, provider = provider, phone = tel)
}

/** Chờ Task của Google Play services mà không cần thêm thư viện coroutines-play-services. */
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
