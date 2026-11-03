package com.example.bai01

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/* ============================================================
 * Đăng nhập / Đăng ký bằng Email + Mật khẩu
 * ============================================================ */

@Composable
fun EmailLoginScreen(
    authService: AuthService,
    onBack: () -> Unit,
    onSignedIn: (UthUser) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // ---- Kiểm tra dữ liệu nhập ----
    val nameError = if (submitted && isRegister && name.isBlank()) "Vui lòng nhập họ tên" else null
    val emailError = when {
        !submitted -> null
        email.isBlank() -> "Vui lòng nhập email"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Email không hợp lệ"
        else -> null
    }
    val passwordError = when {
        !submitted -> null
        password.isEmpty() -> "Vui lòng nhập mật khẩu"
        password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
        else -> null
    }

    fun submit() {
        submitted = true
        if (nameError != null || emailError != null || passwordError != null) return
        // Tính lại vì các biến lỗi ở trên được tính trước khi submitted = true
        if ((isRegister && name.isBlank()) || email.isBlank() ||
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() || password.length < 6
        ) return

        scope.launch {
            loading = true
            val result = if (isRegister) {
                authService.registerWithEmail(name.trim(), email.trim(), password)
            } else {
                authService.signInWithEmail(email.trim(), password)
            }
            loading = false
            when (result) {
                is AuthResult.Success -> {
                    showSignedInToast(context, result.user)
                    onSignedIn(result.user)
                }
                is AuthResult.Error -> errorMessage = result.message
            }
        }
    }

    fun forgotPassword() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMessage = "Hãy nhập email của bạn vào ô Email trước, rồi bấm \"Quên mật khẩu?\"."
            return
        }
        scope.launch {
            loading = true
            val r = authService.sendPasswordReset(email.trim())
            loading = false
            if (r.success) infoMessage = r.message else errorMessage = r.message
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthHeader(
                title = if (isRegister) "Tạo tài khoản" else "Đăng nhập bằng Email",
                description = if (isRegister)
                    "Tạo tài khoản UTH Todo bằng email để đồng bộ công việc."
                else
                    "Nhập email và mật khẩu bạn đã đăng ký."
            )
            Spacer(Modifier.height(4.dp))

            if (isRegister) {
                UthTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Họ và tên",
                    placeholder = "Nguyễn Minh Anh",
                    error = nameError,
                    enabled = !loading,
                )
            }
            UthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "ban@example.com",
                keyboardType = KeyboardType.Email,
                error = emailError,
                enabled = !loading,
            )
            UthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                placeholder = "Ít nhất 6 ký tự",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                error = passwordError,
                enabled = !loading,
            )

            if (!isRegister) {
                Text(
                    "Quên mật khẩu?",
                    style = uthText(13.sp, FontWeight.SemiBold, UthBlue, 16.sp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !loading) { forgotPassword() }
                        .padding(6.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
            UthPrimaryButton(
                label = if (isRegister) "Đăng ký" else "Đăng nhập",
                onClick = { submit() },
                loading = loading,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isRegister) "Đã có tài khoản?" else "Chưa có tài khoản?",
                    style = uthText(14.sp, FontWeight.Normal, UthBody, 18.sp)
                )
                Text(
                    if (isRegister) "Đăng nhập" else "Đăng ký",
                    style = uthText(14.sp, FontWeight.SemiBold, UthBlue, 18.sp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !loading) {
                            isRegister = !isRegister
                            submitted = false
                        }
                        .padding(6.dp)
                )
            }
        }
    }

    errorMessage?.let {
        AuthErrorDialog(
            message = it,
            title = if (isRegister) "Đăng ký thất bại" else "Đăng nhập thất bại",
            onDismiss = { errorMessage = null }
        )
    }
    infoMessage?.let {
        AuthErrorDialog(message = it, title = "Đã gửi email", onDismiss = { infoMessage = null })
    }
}

@Preview(name = "Đăng nhập Email", widthDp = 390, heightDp = 844)
@Composable
fun EmailLoginPreview() {
    EmailLoginScreen(authService = FakeAuthService(), onBack = {}, onSignedIn = {})
}
