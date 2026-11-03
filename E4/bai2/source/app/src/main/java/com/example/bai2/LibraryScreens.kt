package com.example.bai2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------
 * Màu theo Figma "Hệ thống Quản lý Thư viện"
 * ------------------------------------------------------------------ */
object LibColors {
    val Primary = Color(0xFF1550B0)      // nút Thay đổi / Thêm
    val Checkbox = Color(0xFFA3203B)     // ô tick đỏ đậm
    val Panel = Color(0xFFD9D9D9)        // khung xám danh sách
    val FieldBorder = Color(0xFF9E9E9E)
    val TextMain = Color(0xFF111111)
    val TextItem = Color(0xFF333333)
    val TextSub = Color(0xFF6B6B6B)
    val TabActive = Color(0xFF3D72D9)
    val TabInactive = Color(0xFFC9C9C9)
    val Divider = Color(0xFFDDDDDD)
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFC62828)
}

private val PrimaryButtonColors: ButtonColors
    @Composable get() =
    ButtonDefaults.buttonColors(containerColor = LibColors.Primary, contentColor = Color.White)

/* ==================================================================
 * TAB 1 – QUẢN LÝ (giống Figma)
 * ================================================================== */
@Composable
fun ManageScreen(library: LibraryService, onMessage: (LibraryResult) -> Unit) {
    val focus = LocalFocusManager.current
    val current = library.currentStudent
    var nameInput by remember { mutableStateOf(current?.name.orEmpty()) }
    var showBorrowDialog by remember { mutableStateOf(false) }
    var detailRecord by remember { mutableStateOf<BorrowRecord?>(null) }

    LaunchedEffect(current) { nameInput = current?.name.orEmpty() }

    fun changeStudent() {
        focus.clearFocus()
        val found = library.findStudent(nameInput)
        if (found != null) {
            library.selectStudent(found)
            onMessage(LibraryResult.Success("Đã chuyển sang ${found.name}"))
        } else {
            onMessage(LibraryResult.Failure("Không tìm thấy \"${nameInput.trim()}\". Thêm ở tab Sinh viên."))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(36.dp))
        Text(
            "Hệ thống\nQuản lý Thư viện",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            color = LibColors.TextMain
        )

        Spacer(Modifier.height(52.dp))
        SectionLabel("Sinh viên")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                singleLine = true,
                placeholder = { Text("Nhập tên sinh viên") },
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LibColors.TextMain),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LibColors.FieldBorder,
                    focusedBorderColor = LibColors.Primary,
                    cursorColor = LibColors.Primary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { changeStudent() }),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { changeStudent() },
                shape = RoundedCornerShape(8.dp),
                colors = PrimaryButtonColors,
                contentPadding = PaddingValues(horizontal = 14.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionLabel("Danh sách sách")
        Spacer(Modifier.height(10.dp))

        // Khung xám chứa danh sách sách đang mượn
        val loans = current?.let { library.loansOf(it) }.orEmpty()
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LibColors.Panel)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                current == null -> EmptyPanelText(
                    "Chưa chọn sinh viên",
                    "Nhập tên rồi nhấn 'Thay đổi'."
                )
                loans.isEmpty() -> EmptyPanelText(
                    "Bạn chưa mượn quyển sách nào",
                    "Nhấn 'Thêm' để bắt đầu hành trình đọc sách!"
                )
                else -> loans.forEach { record ->
                    BorrowedBookRow(
                        record = record,
                        onReturn = { onMessage(library.returnBook(record)) },
                        onOpen = { detailRecord = record }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (current == null) onMessage(LibraryResult.Failure("Vui lòng chọn sinh viên trước"))
                else showBorrowDialog = true
            },
            shape = RoundedCornerShape(8.dp),
            colors = PrimaryButtonColors,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(200.dp)
                .height(48.dp)
        ) {
            Text("Thêm", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showBorrowDialog && current != null) {
        BorrowDialog(
            library = library,
            student = current,
            onDismiss = { showBorrowDialog = false },
            onMessage = onMessage
        )
    }
    detailRecord?.let { record ->
        BookInfoDialog(library = library, item = record.item, record = record, onDismiss = { detailRecord = null })
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LibColors.TextMain)
}

@Composable
private fun EmptyPanelText(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LibColors.TextMain, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, fontSize = 14.sp, color = LibColors.TextMain, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BorrowedBookRow(record: BorrowRecord, onReturn: () -> Unit, onOpen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            Modifier.padding(start = 4.dp, end = 14.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = true,
                onCheckedChange = { checked -> if (!checked) onReturn() },
                colors = CheckboxDefaults.colors(
                    checkedColor = LibColors.Checkbox,
                    checkmarkColor = Color.White
                )
            )
            Column(Modifier.weight(1f)) {
                Text(record.item.title, fontSize = 15.sp, color = LibColors.TextItem)
                Text(
                    "Hạn trả ${record.dueDateText()}",
                    fontSize = 11.sp,
                    color = if (record.isOverdue()) LibColors.Warning else LibColors.TextSub
                )
            }
        }
    }
}

/* ---------- Hộp thoại mượn sách (nút "Thêm") ---------- */
@Composable
private fun BorrowDialog(
    library: LibraryService,
    student: Student,
    onDismiss: () -> Unit,
    onMessage: (LibraryResult) -> Unit,
) {
    val selected = remember { mutableStateListOf<LibraryItem>() }
    val borrowedNow = library.loansOf(student).map { it.item }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mượn sách cho ${student.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "${student.role()} • đang mượn ${borrowedNow.size}/${student.maxBooks} quyển",
                    fontSize = 13.sp,
                    color = LibColors.TextSub
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(library.items, key = { it.id }) { item ->
                        // ĐA HÌNH: gọi borrowRestriction() qua kiểu cha LibraryItem
                        val reason = when {
                            item in borrowedNow -> "Đã mượn"
                            library.availableCopies(item) <= 0 -> "Hết bản"
                            else -> item.borrowRestriction(student)
                        }
                        val enabled = reason == null
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled) {
                                    if (item in selected) selected.remove(item) else selected.add(item)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item in selected || item in borrowedNow,
                                enabled = enabled,
                                onCheckedChange = {
                                    if (it) selected.add(item) else selected.remove(item)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = LibColors.Checkbox)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    reason ?: "${item.typeLabel} • còn ${library.availableCopies(item)} bản • ${item.loanDays} ngày",
                                    fontSize = 12.sp,
                                    color = if (enabled) LibColors.TextSub else LibColors.Warning
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selected.forEach { onMessage(library.borrow(student, it)) }
                    onDismiss()
                },
                enabled = selected.isNotEmpty(),
                colors = PrimaryButtonColors,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Mượn (${selected.size})") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ", color = LibColors.Primary) } }
    )
}

/* ---------- Hộp thoại thông tin sách ---------- */
@Composable
private fun BookInfoDialog(
    library: LibraryService,
    item: LibraryItem,
    record: BorrowRecord?,
    onDismiss: () -> Unit,
) {
    val borrowers = library.loansOf(item).joinToString { it.student.name }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoLine("Mã sách", item.id)
                InfoLine("Loại", item.typeLabel)
                // ĐA HÌNH: mỗi lớp con tự mô tả thông tin
                InfoLine("Thông tin", item.displayInfo())
                InfoLine("Thời hạn mượn", "${item.loanDays} ngày")
                InfoLine("Số bản", "còn ${library.availableCopies(item)}/${item.totalCopies}")
                InfoLine("Người đang mượn", borrowers.ifEmpty { "Chưa có" })
                record?.let { InfoLine("Phiếu mượn", it.displayInfo()) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = LibColors.Primary) } }
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = LibColors.TextSub)
        Text(value, fontSize = 15.sp, color = LibColors.TextMain)
    }
}

/* ==================================================================
 * TAB 2 – DS SÁCH
 * ================================================================== */
@Composable
fun BooksScreen(library: LibraryService, onMessage: (LibraryResult) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<LibraryItem?>(null) }
    val totalCopies = library.items.sumOf { it.totalCopies }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        ScreenHeader("Danh sách sách", "${library.items.size} đầu sách • $totalCopies bản")
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(library.items, key = { it.id }) { item ->
                val available = library.availableCopies(item)
                InfoCard(onClick = { detail = item }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibColors.TextMain,
                            modifier = Modifier.weight(1f)
                        )
                        Chip(item.typeLabel)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(item.displayInfo(), fontSize = 13.sp, color = LibColors.TextSub)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Còn $available/${item.totalCopies} bản • mượn ${item.loanDays} ngày",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (available > 0) LibColors.Success else LibColors.Warning
                    )
                }
            }
        }
        BottomActionButton("Thêm sách") { showAdd = true }
    }

    if (showAdd) {
        AddBookDialog(
            onDismiss = { showAdd = false },
            onSubmit = { type, title, author, year, subject, copies ->
                val result = library.addItem(type, title, author, year, subject, copies)
                onMessage(result)
                result.isSuccess
            }
        )
    }
    detail?.let { BookInfoDialog(library = library, item = it, record = null, onDismiss = { detail = null }) }
}

@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onSubmit: (BookType, String, String, Int, String, Int) -> Boolean,
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2024") }
    var subject by remember { mutableStateOf("") }
    var copies by remember { mutableStateOf("1") }
    var type by remember { mutableStateOf(BookType.BOOK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sách", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookType.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { type = option },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == option, onClick = { type = option })
                        Text(option.label)
                    }
                }
                FormField("Tên sách", title) { title = it }
                FormField("Tác giả", author) { author = it }
                FormField("Năm xuất bản", year, KeyboardType.Number) { year = it.filter(Char::isDigit).take(4) }
                FormField("Số bản", copies, KeyboardType.Number) { copies = it.filter(Char::isDigit).take(3) }
                if (type == BookType.TEXTBOOK) FormField("Môn học", subject) { subject = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onSubmit(
                        type, title, author,
                        year.toIntOrNull() ?: 0, subject,
                        copies.toIntOrNull() ?: 0
                    )
                    if (ok) onDismiss()
                },
                colors = PrimaryButtonColors,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ", color = LibColors.Primary) } }
    )
}

/* ==================================================================
 * TAB 3 – SINH VIÊN
 * ================================================================== */
@Composable
fun StudentsScreen(
    library: LibraryService,
    onMessage: (LibraryResult) -> Unit,
    onStudentSelected: () -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        ScreenHeader("Danh sách sinh viên", "${library.students.size} người • chạm để chọn")
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(library.students, key = { it.id }) { student ->
                val loans = library.loansOf(student)
                val isCurrent = student == library.currentStudent
                InfoCard(
                    highlighted = isCurrent,
                    onClick = {
                        library.selectStudent(student)
                        onStudentSelected()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            student.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibColors.TextMain,
                            modifier = Modifier.weight(1f)
                        )
                        // ĐA HÌNH: role() khác nhau giữa Student và GraduateStudent
                        Chip(student.role())
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(student.displayInfo(), fontSize = 13.sp, color = LibColors.TextSub)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Đang mượn ${loans.size}/${student.maxBooks}" +
                            if (loans.isNotEmpty()) ": " + loans.joinToString { it.item.title } else "",
                        fontSize = 13.sp,
                        color = LibColors.TextItem
                    )
                    if (isCurrent) {
                        Spacer(Modifier.height(4.dp))
                        Text("Đang chọn", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibColors.Primary)
                    }
                }
            }
        }
        BottomActionButton("Thêm sinh viên") { showAdd = true }
    }

    if (showAdd) {
        AddStudentDialog(
            onDismiss = { showAdd = false },
            onSubmit = { name, className, graduate ->
                val result = library.addStudent(name, className, graduate)
                onMessage(result)
                result.isSuccess
            }
        )
    }
}

@Composable
private fun AddStudentDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Boolean) -> Boolean,
) {
    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var graduate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sinh viên", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("Họ tên", name) { name = it }
                FormField("Lớp", className) { className = it }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { graduate = !graduate },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = graduate,
                        onCheckedChange = { graduate = it },
                        colors = CheckboxDefaults.colors(checkedColor = LibColors.Checkbox)
                    )
                    Text("Học viên cao học (mượn tối đa 5 quyển)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (onSubmit(name, className, graduate)) onDismiss() },
                colors = PrimaryButtonColors,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ", color = LibColors.Primary) } }
    )
}

/* ==================================================================
 * Thành phần dùng chung
 * ================================================================== */
@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LibColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = LibColors.TextSub)
    }
}

@Composable
private fun InfoCard(
    highlighted: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(if (highlighted) 2.dp else 1.dp, if (highlighted) LibColors.Primary else LibColors.Divider),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = LibColors.Primary,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LibColors.Primary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun BottomActionButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = PrimaryButtonColors,
            modifier = Modifier
                .width(200.dp)
                .height(48.dp)
        ) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LibColors.Primary,
            focusedLabelColor = LibColors.Primary,
            cursorColor = LibColors.Primary,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
