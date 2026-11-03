package com.example.bai01

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/* ============================================================
 * Đăng nhập Facebook (Facebook Login SDK)
 *  Facebook SDK mở app Facebook / trình duyệt → trả về access token
 *  → token được gửi cho Firebase (FacebookAuthProvider) trong FirebaseAuthService.
 * ============================================================ */

/**
 * Trả về hàm để bắt đầu đăng nhập Facebook.
 * @param onToken  nhận access token khi người dùng đồng ý
 * @param onCancel người dùng bấm huỷ
 * @param onError  lỗi (đã đổi sang câu tiếng Việt)
 */
@Composable
fun rememberFacebookLogin(
    onToken: (String) -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val callbackManager = remember { CallbackManager.Factory.create() }
    val loginManager = remember { LoginManager.getInstance() }

    val currentOnToken by rememberUpdatedState(onToken)
    val currentOnCancel by rememberUpdatedState(onCancel)
    val currentOnError by rememberUpdatedState(onError)

    DisposableEffect(loginManager, callbackManager) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) = currentOnToken(result.accessToken.token)
            override fun onCancel() = currentOnCancel()
            override fun onError(error: FacebookException) = currentOnError(
                when {
                    error.message.orEmpty().contains("CONNECTION_FAILURE", ignoreCase = true) ->
                        "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."
                    error.message.orEmpty().contains("key hash", ignoreCase = true) ->
                        "Key hash của app chưa được khai báo trên Meta for Developers."
                    else -> "Đăng nhập Facebook thất bại: ${error.message ?: "lỗi không xác định"}"
                }
            )
        })
        onDispose { loginManager.unregisterCallback(callbackManager) }
    }

    val contract = remember { loginManager.createLogInActivityResultContract(callbackManager, null) }
    val launcher = rememberLauncherForActivityResult(contract) { /* kết quả đi qua callback ở trên */ }

    return {
        val appId = context.getString(R.string.facebook_app_id)
        if (appId.isBlank() || !appId.all { it.isDigit() }) {
            currentOnError("Chưa điền Facebook App ID. Hãy mở res/values/strings.xml và dán App ID, Client token từ Meta for Developers.")
        } else {
            loginManager.logOut() // luôn cho chọn lại tài khoản
            launcher.launch(listOf("email", "public_profile"))
        }
    }
}
