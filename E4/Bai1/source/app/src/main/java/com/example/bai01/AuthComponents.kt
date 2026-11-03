package com.example.bai01

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ============================================================
 * Thành phần dùng chung cho các màn đăng nhập
 * ============================================================ */

/** Thông báo đăng nhập thành công (dùng chung cho mọi cách đăng nhập). */
fun showSignedInToast(context: Context, user: UthUser) {
    Toast.makeText(context, "Đăng nhập thành công! Xin chào ${user.shortName}", Toast.LENGTH_SHORT).show()
}

/** Lấy Activity từ LocalContext (Phone Auth cần Activity). */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Nút viền trắng: "Tiếp tục với Google / Email / số điện thoại". */
@Composable
fun AuthOptionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingLabel: String = "",
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, UthBorder, RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = UthBlue, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text(loadingLabel, style = uthText(15.sp, FontWeight.SemiBold, UthDark, 19.sp))
            } else {
                icon()
                Spacer(Modifier.width(10.dp))
                Text(label, style = uthText(15.sp, FontWeight.SemiBold, UthDark, 19.sp))
            }
        }
    }
}

/** Nút xanh chính (Đăng nhập, Gửi mã, Xác nhận…). */
@Composable
fun UthPrimaryButton(
    label: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val active = enabled && !loading
    Box(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) UthBlue else UthBlue.copy(alpha = 0.4f))
            .clickable(enabled = active) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(label, style = uthText(16.sp, FontWeight.SemiBold, Color.White, 20.sp))
        }
    }
}

/** Ô nhập liệu theo màu UTH. */
@Composable
fun UthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    error: String? = null,
    prefix: String? = null,
    enabled: Boolean = true,
) {
    var visible by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = uthText(13.sp, FontWeight.SemiBold, UthDark, 16.sp))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle = uthText(15.sp, FontWeight.Medium, UthDark, 20.sp),
            placeholder = { Text(placeholder, style = uthText(15.sp, FontWeight.Normal, UthMuted, 20.sp)) },
            prefix = if (prefix != null) {
                { Text(prefix, style = uthText(15.sp, FontWeight.SemiBold, UthDark, 20.sp)) }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    Text(
                        if (visible) "Ẩn" else "Hiện",
                        style = uthText(13.sp, FontWeight.SemiBold, UthBlue, 16.sp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { visible = !visible }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else null,
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UthBlue,
                unfocusedBorderColor = UthBorder,
                errorBorderColor = UthRed,
                cursorColor = UthBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = UthScreenBg,
            )
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = uthText(12.sp, FontWeight.Normal, UthRed, 15.sp))
        }
    }
}

/** Thanh trên cùng có nút quay lại. */
@Composable
fun AuthTopBar(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            LineIcon(UthIcons.ChevronLeft, UthDark, size = 22.dp)
        }
    }
}

/** Tiêu đề + mô tả của màn đăng nhập phụ. */
@Composable
fun AuthHeader(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = uthText(26.sp, FontWeight.Bold, UthDark, 34.sp, (-0.4).sp))
        Text(description, style = uthText(14.sp, FontWeight.Normal, UthBody, 21.sp))
    }
}

/** Hộp thoại báo lỗi đăng nhập. */
@Composable
fun AuthErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String = "Đăng nhập thất bại",
    onRetry: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = uthText(18.sp, FontWeight.Bold, UthDark, 24.sp)) },
        text = { Text(message, style = uthText(14.sp, FontWeight.Normal, UthBody, 20.sp)) },
        confirmButton = {
            if (onRetry != null) {
                TextButton(onClick = { onDismiss(); onRetry() }) {
                    Text("Thử lại", style = uthText(15.sp, FontWeight.SemiBold, UthBlue))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Đã hiểu", style = uthText(15.sp, FontWeight.SemiBold, UthBlue))
                }
            }
        },
        dismissButton = if (onRetry != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text("Đóng", style = uthText(15.sp, FontWeight.Medium, UthSkip))
                }
            }
        } else null,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
