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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bai3.R
import com.example.bai3.ui.components.AppInput
import com.example.bai3.ui.components.AuthHeader
import com.example.bai3.ui.components.PrimaryButton
import com.example.bai3.viewmodel.ResetPasswordViewModel

/** Màn 4 – Confirm: hiển thị lại dữ liệu đã nhập ở 3 màn trước (chỉ đọc). */
@Composable
fun ConfirmScreen(
    vm: ResetPasswordViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    // Lắng nghe StateFlow: dữ liệu đổi thì giao diện tự cập nhật
    val data by vm.data.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        AuthHeader(
            title = "Confirm",
            subtitle = "We are here to help you!",
            onBack = onBack
        )

        AppInput(
            value = data.email, onValueChange = {}, placeholder = "Email",
            icon = R.drawable.ic_person, readOnly = true
        )
        AppInput(
            value = data.code, onValueChange = {}, placeholder = "Code",
            icon = R.drawable.ic_email, readOnly = true
        )
        AppInput(
            value = data.password, onValueChange = {}, placeholder = "Password",
            icon = R.drawable.ic_lock, isPassword = true, readOnly = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Summit", onClick = onSubmit)
    }
}
