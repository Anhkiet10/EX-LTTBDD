package com.example.bai01

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/* ============================================================
 * Bảng (bottom sheet) Thêm / Sửa / Xoá công việc
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    /** null = thêm mới; khác null = sửa việc này. */
    initial: TodoTask?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: Category, time: String) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.STUDY) }
    var time by remember { mutableStateOf(initial?.time?.ifBlank { null } ?: nextHourTime()) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    fun pickTime() {
        val (h, m) = time.split(":").map { it.toIntOrNull() ?: 0 }.let { it[0] to it.getOrElse(1) { 0 } }
        TimePickerDialog(context, { _, hour, minute ->
            time = "%02d:%02d".format(hour, minute)
        }, h, m, true).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (initial == null) "Thêm việc mới" else "Sửa công việc",
                style = uthText(20.sp, FontWeight.Bold, UthDark, 26.sp, (-0.3).sp)
            )

            UthTextField(
                value = title,
                onValueChange = { title = it.take(80); titleError = null },
                label = "Tên công việc",
                placeholder = "Vd: Ôn chương 4 – Đồ thị",
                imeAction = ImeAction.Done,
                error = titleError,
            )

            // ---- Nhóm ----
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nhóm", style = uthText(13.sp, FontWeight.SemiBold, UthDark, 16.sp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Category.entries.forEach { c ->
                        val selected = c == category
                        Text(
                            c.label,
                            style = uthText(
                                14.sp, FontWeight.SemiBold,
                                if (selected) Color(c.chipText) else UthSkip, 18.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(c.chipBackground) else Color.White)
                                .border(
                                    1.dp,
                                    if (selected) Color(c.chipText).copy(alpha = 0.4f) else UthBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { category = c }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // ---- Giờ ----
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Giờ", style = uthText(13.sp, FontWeight.SemiBold, UthDark, 16.sp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, UthBorder, RoundedCornerShape(12.dp))
                        .clickable { pickTime() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, style = uthText(16.sp, FontWeight.SemiBold, UthDark, 20.sp))
                    Spacer(Modifier.width(10.dp))
                    Text("Đổi giờ", style = uthText(13.sp, FontWeight.SemiBold, UthBlue, 16.sp))
                }
            }

            Spacer(Modifier.height(4.dp))
            UthPrimaryButton(
                label = if (initial == null) "Thêm việc" else "Lưu thay đổi",
                onClick = {
                    if (title.isBlank()) titleError = "Vui lòng nhập tên công việc"
                    else onSave(title.trim(), category, time)
                },
            )

            if (onDelete != null) {
                Text(
                    "Xoá công việc",
                    style = uthText(15.sp, FontWeight.SemiBold, UthRed, 19.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { confirmDelete = true }
                        .padding(vertical = 12.dp)
                )
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Xoá công việc?", style = uthText(18.sp, FontWeight.Bold, UthDark, 24.sp)) },
            text = {
                Text(
                    "\"${initial?.title}\" sẽ bị xoá vĩnh viễn.",
                    style = uthText(14.sp, FontWeight.Normal, UthBody, 20.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Xoá", style = uthText(15.sp, FontWeight.SemiBold, UthRed))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Huỷ", style = uthText(15.sp, FontWeight.Medium, UthSkip))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/** Giờ tròn kế tiếp, vd bây giờ 14:20 → "15:00". */
private fun nextHourTime(): String {
    val h = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24
    return "%02d:00".format(h)
}
