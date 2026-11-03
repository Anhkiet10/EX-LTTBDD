package com.example.bai3.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bai3.R
import com.example.bai3.model.PasswordValidator
import com.example.bai3.ui.components.AppInput
import com.example.bai3.ui.components.AuthHeader
import com.example.bai3.ui.components.ErrorText
import com.example.bai3.ui.components.PrimaryButton
import com.example.bai3.viewmodel.ResetPasswordViewModel

/** Màn 3 – Create new password: nhập mật khẩu và xác nhận. */
@Composable
fun NewPasswordScreen(
    vm: ResetPasswordViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf(vm.data.value.password) }
    var confirm by rememberSaveable { mutableStateOf(vm.data.value.password) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        AuthHeader(
            title = "Create new password",
            subtitle = "Your new password must be different form previously used password",
            onBack = onBack
        )

        AppInput(
            value = password,
            onValueChange = { password = it; error = null },
            placeholder = "Password",
            icon = R.drawable.ic_lock,
            isPassword = true
        )
        AppInput(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            placeholder = "Confirm Password",
            icon = R.drawable.ic_lock,
            isPassword = true
        )
        ErrorText(error)

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Next") {
            error = PasswordValidator(confirm).validate(password)
            if (error == null) {
                vm.setPassword(password)
                onNext()
            }
        }
    }
}
