package com.example.bai3.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bai3.R

/*
 * Các thành phần giao diện DÙNG CHUNG cho cả 4 màn hình.
 * Trong Figma, 4 màn đều có: logo + "SmartTasks" + tiêu đề + mô tả,
 * ô nhập nền xám bo góc, nút xanh bo tròn. Viết 1 lần, dùng lại 4 lần.
 */

// Màu lấy từ Figma
val BluePrimary = Color(0xFF2196F3)
val FieldBackground = Color(0xFFF5F5F5)
val HintGray = Color(0xFF9E9E9E)

/**
 * Phần đầu màn hình: nút back (nếu có) + logo UTH + SmartTasks + tiêu đề + mô tả.
 * @param onBack null = không hiện nút back (màn Forget Password).
 */
@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BluePrimary, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.uth_logo),
            contentDescription = "UTH logo",
            contentScale = ContentScale.Fit,
            // Phải đặt CHIỀU RỘNG: ảnh gốc chỉ 100x68 px nên nếu chỉ đặt height
            // thì chiều rộng bị giữ nhỏ và logo không to lên.
            // Muốn to/nhỏ hơn: chỉ cần đổi số 160.dp, chiều cao tự tính theo tỉ lệ 100:68.
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(100f / 68f)
        )
        Text(
            text = "SmartTasks",
            color = BluePrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(28.dp))
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = HintGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Ô nhập nền xám có icon bên trái (Email, Password, các ô ở màn Confirm). */
@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes icon: Int,
    isPassword: Boolean = false,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = true,
        placeholder = { Text(placeholder, color = HintGray) },
        leadingIcon = {
            Icon(painter = painterResource(icon), contentDescription = null, tint = HintGray)
        },
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

/** Nút xanh bo tròn: Next / Summit. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

/** Dòng báo lỗi màu đỏ dưới ô nhập (chỉ hiện khi message khác null). */
@Composable
fun ErrorText(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        )
    }
}

/**
 * Hàng [length] ô vuông nhập mã (màn Verify Code).
 * Thực chất là 1 ô nhập ẩn, mỗi chữ số được vẽ vào 1 ô.
 */
@Composable
fun CodeInput(code: String, length: Int, onCodeChange: (String) -> Unit) {
    BasicTextField(
        value = code,
        onValueChange = { input ->
            if (input.length <= length && input.all { it.isDigit() }) onCodeChange(input)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(length) { index ->
                    val shape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(FieldBackground, shape)
                            .border(
                                width = 1.dp,
                                color = if (index == code.length) BluePrimary else Color.Transparent,
                                shape = shape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = code.getOrNull(index)?.toString() ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

/**
 * Khung hiển thị thông tin vừa Summit (email, code, password)
 * ở dưới nút Next của màn Forget Password.
 */
@Composable
fun SubmittedInfoCard(email: String, code: String, password: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
            .border(1.dp, BluePrimary, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Submitted information",
            color = BluePrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(icon = R.drawable.ic_person, label = "Email", value = email)
        InfoRow(icon = R.drawable.ic_email, label = "Code", value = code)
        InfoRow(icon = R.drawable.ic_lock, label = "Password", value = password)
    }
}

/** Một dòng: icon + nhãn + giá trị. */
@Composable
private fun InfoRow(@DrawableRes icon: Int, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", color = HintGray, fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
