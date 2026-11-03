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
import com.example.bai3.model.CodeValidator
import com.example.bai3.ui.components.AuthHeader
import com.example.bai3.ui.components.CodeInput
import com.example.bai3.ui.components.ErrorText
import com.example.bai3.ui.components.PrimaryButton
import com.example.bai3.viewmodel.ResetPasswordViewModel
import com.example.bai3.viewmodel.ResetPasswordViewModel.Companion.CODE_LENGTH

/** Màn 2 – Verify Code: nhập mã 5 số. */
@Composable
fun VerifyCodeScreen(
    vm: ResetPasswordViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf(vm.data.value.code) }
    var error by remember { mutableStateOf<String?>(null) }
    val validator = remember { CodeValidator(CODE_LENGTH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        AuthHeader(
            title = "Verify Code",
            subtitle = "Enter the the code\nwe just sent you on your registered Email",
            onBack = onBack
        )

        CodeInput(code = code, length = CODE_LENGTH) { code = it; error = null }
        ErrorText(error)

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Next") {
            error = validator.validate(code)
            if (error == null) {
                vm.setCode(code)
                onNext()
            }
        }
    }
}
