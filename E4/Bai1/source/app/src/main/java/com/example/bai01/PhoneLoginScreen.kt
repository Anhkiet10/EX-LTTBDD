package com.example.bai01

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ============================================================
 * Đăng nhập bằng Số điện thoại (OTP qua SMS)
 *  Bước 1: nhập số → Gửi mã
 *  Bước 2: nhập mã 6 số → Xác nhận
 * ============================================================ */

@Composable
fun PhoneLoginScreen(
    authService: AuthService,
    onBack: () -> Unit,
    onSignedIn: (UthUser) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phoneInput by remember { mutableStateOf("") }
    var phoneE164 by remember { mutableStateOf<String?>(null) }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var code by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendSeconds by remember { mutableIntStateOf(0) }

    // Đếm ngược cho nút "Gửi lại mã"
    LaunchedEffect(resendSeconds) {
        if (resendSeconds > 0) {
            delay(1000)
            resendSeconds -= 1
        }
    }

    fun finish(user: UthUser) {
        showSignedInToast(context, user)
        onSignedIn(user)
    }

    fun sendCode() {
        val e164 = normalizeVietnamPhone(phoneInput)
        if (e164 == null) {
            phoneError = "Số điện thoại không hợp lệ (vd: 0912 345 678)"
            return
        }
        phoneError = null
        val activity = context.findActivity() ?: return
        scope.launch {
            loading = true
            val r = authService.sendPhoneCode(activity, e164)
            loading = false
            when (r) {
                is PhoneCodeResult.CodeSent -> {
                    phoneE164 = e164
                    verificationId = r.verificationId
                    code = ""
                    resendSeconds = 60
                }
                is PhoneCodeResult.Verified -> finish(r.user)
                is PhoneCodeResult.Error -> errorMessage = r.message
            }
        }
    }

    fun verify() {
        val id = verificationId ?: return
        if (code.length != 6) {
            errorMessage = "Mã xác nhận gồm 6 chữ số."
            return
        }
        scope.launch {
            loading = true
            val r = authService.verifyPhoneCode(id, code)
            loading = false
            when (r) {
                is AuthResult.Success -> finish(r.user)
                is AuthResult.Error -> errorMessage = r.message
            }
        }
    }

    val codeStep = verificationId != null

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        AuthTopBar(onBack = {
            if (codeStep && !loading) {
                verificationId = null // quay về bước nhập số
            } else onBack()
        })

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!codeStep) {
                AuthHeader(
                    title = "Đăng nhập bằng số điện thoại",
                    description = "Chúng tôi sẽ gửi mã xác nhận gồm 6 chữ số qua SMS."
                )
                Spacer(Modifier.height(4.dp))
                UthTextField(
                    value = phoneInput,
                    onValueChange = { v -> phoneInput = v.filter { it.isDigit() || it == ' ' || it == '+' }.take(16) },
                    label = "Số điện thoại",
                    placeholder = "0912 345 678",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                    error = phoneError,
                    enabled = !loading,
                )
                Spacer(Modifier.height(4.dp))
                UthPrimaryButton(
                    label = "Gửi mã xác nhận",
                    onClick = { sendCode() },
                    loading = loading,
                    enabled = phoneInput.isNotBlank(),
                )
            } else {
                AuthHeader(
                    title = "Nhập mã xác nhận",
                    description = "Mã gồm 6 chữ số đã được gửi tới ${localPhone(phoneE164.orEmpty())}."
                )
                Spacer(Modifier.height(4.dp))
                UthTextField(
                    value = code,
                    onValueChange = { v -> code = v.filter { it.isDigit() }.take(6) },
                    label = "Mã xác nhận",
                    placeholder = "••••••",
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                    enabled = !loading,
                )
                Spacer(Modifier.height(4.dp))
                UthPrimaryButton(
                    label = "Xác nhận",
                    onClick = { verify() },
                    loading = loading,
                    enabled = code.length == 6,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chưa nhận được mã?", style = uthText(14.sp, FontWeight.Normal, UthBody, 18.sp))
                    val canResend = resendSeconds == 0 && !loading
                    Text(
                        if (resendSeconds > 0) "Gửi lại sau ${resendSeconds}s" else "Gửi lại",
                        style = uthText(14.sp, FontWeight.SemiBold, if (canResend) UthBlue else UthMuted, 18.sp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = canResend) { sendCode() }
                            .padding(6.dp)
                    )
                }
            }
        }
    }

    errorMessage?.let {
        AuthErrorDialog(message = it, onDismiss = { errorMessage = null })
    }
}

@Preview(name = "Đăng nhập SĐT", widthDp = 390, heightDp = 844)
@Composable
fun PhoneLoginPreview() {
    PhoneLoginScreen(authService = FakeAuthService(), onBack = {}, onSignedIn = {})
}
