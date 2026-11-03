package com.example.bai3.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bai3.R
import com.example.bai3.model.EmailValidator
import com.example.bai3.ui.components.AppInput
import com.example.bai3.ui.components.AuthHeader
import com.example.bai3.ui.components.ErrorText
import com.example.bai3.ui.components.PrimaryButton
import com.example.bai3.ui.components.SubmittedInfoCard
import com.example.bai3.viewmodel.ResetPasswordViewModel

/** Màn 1 – Forget Password?: nhập email rồi bấm Next. */
@Composable
fun ForgetPasswordScreen(
    vm: ResetPasswordViewModel,
    onNext: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(vm.data.value.email) }
    var error by remember { mutableStateOf<String?>(null) }
    val validator = remember { EmailValidator() }
    // Thông tin vừa Summit (null nếu chưa submit)
    val submitted by vm.submitted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        AuthHeader(
            title = "Forget Password?",
            subtitle = "Enter your Email, we will send you a verification code."
        )

        AppInput(
            value = email,
            onValueChange = { email = it; error = null },
            placeholder = "Your Email",
            icon = R.drawable.ic_email,
            keyboardType = KeyboardType.Email
        )
        ErrorText(error)

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Next") {
            error = validator.validate(email)
            if (error == null) {
                vm.setEmail(email)   // lưu dữ liệu vào ViewModel
                onNext()             // chuyển màn
            }
        }

        // Sau khi Summit ở màn Confirm, quay về đây và hiện thông tin dưới nút Next
        submitted?.let { info ->
            SubmittedInfoCard(
                email = info.email,
                code = info.code,
                password = info.password
            )
        }
    }
}
