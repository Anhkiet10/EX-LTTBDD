package com.example.bai2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * TRỪU TƯỢNG: giao diện chỉ biết đến "dịch vụ thư viện",
 * không cần biết dữ liệu được lưu thế nào.
 */
interface LibraryService {
    val students: List<Student>
    val items: List<LibraryItem>
    val currentStudent: Student?

    fun findStudent(query: String): Student?
    fun selectStudent(student: Student)
    fun addStudent(name: String, className: String, isGraduate: Boolean): LibraryResult
    fun addItem(type: BookType, title: String, author: String, year: Int, subject: String, copies: Int): LibraryResult

    fun loansOf(student: Student): List<BorrowRecord>
    fun loansOf(item: LibraryItem): List<BorrowRecord>
    fun availableCopies(item: LibraryItem): Int

    fun borrow(student: Student, item: LibraryItem): LibraryResult
    fun returnBook(record: BorrowRecord): LibraryResult
}

/**
 * ĐÓNG GÓI: các danh sách là private (mutable), bên ngoài chỉ thấy List chỉ-đọc.
 * Mọi thay đổi phải đi qua phương thức có kiểm tra điều kiện.
 */
class LibraryManager : LibraryService {

    private val _students = mutableStateListOf<Student>()
    private val _items = mutableStateListOf<LibraryItem>()
    private val _records = mutableStateListOf<BorrowRecord>()

    private var studentCounter = 0
    private var itemCounter = 0

    override val students: List<Student> get() = _students
    override val items: List<LibraryItem> get() = _items

    private var selected: Student? by mutableStateOf(null)
    override val currentStudent: Student? get() = selected

    // ---------------- Sinh viên ----------------

    override fun findStudent(query: String): Student? {
        val q = query.trim()
        if (q.isEmpty()) return null
        return _students.firstOrNull { it.name.equals(q, ignoreCase = true) || it.id.equals(q, ignoreCase = true) }
    }

    override fun selectStudent(student: Student) {
        if (student in _students) selected = student
    }

    override fun addStudent(name: String, className: String, isGraduate: Boolean): LibraryResult {
        if (name.isBlank()) return LibraryResult.Failure("Vui lòng nhập tên sinh viên")
        if (findStudent(name) != null) return LibraryResult.Failure("Sinh viên \"${name.trim()}\" đã tồn tại")
        val id = nextStudentId()
        val cls = className.ifBlank { "Chưa xếp lớp" }
        // ĐA HÌNH: biến kiểu Student có thể giữ đối tượng GraduateStudent
        val student: Student =
            if (isGraduate) GraduateStudent(id, name, cls) else Student(id, name, cls)
        _students.add(student)
        if (selected == null) selected = student
        return LibraryResult.Success("Đã thêm ${student.role().lowercase()} ${student.name}")
    }

    // ---------------- Sách ----------------

    override fun addItem(
        type: BookType,
        title: String,
        author: String,
        year: Int,
        subject: String,
        copies: Int,
    ): LibraryResult {
        if (title.isBlank()) return LibraryResult.Failure("Vui lòng nhập tên sách")
        if (copies <= 0) return LibraryResult.Failure("Số bản phải lớn hơn 0")
        val existing = _items.firstOrNull { it.title.equals(title.trim(), ignoreCase = true) }
        if (existing != null) {
            existing.addCopies(copies)
            return LibraryResult.Success("Đã thêm $copies bản cho \"${existing.title}\"")
        }
        val item = type.create(nextItemId(), title, author.ifBlank { "Chưa rõ" }, year, subject, copies)
        _items.add(item)
        return LibraryResult.Success("Đã thêm ${item.typeLabel.lowercase()} \"${item.title}\"")
    }

    // ---------------- Mượn / trả ----------------

    override fun loansOf(student: Student): List<BorrowRecord> = _records.filter { it.student == student }

    override fun loansOf(item: LibraryItem): List<BorrowRecord> = _records.filter { it.item == item }

    override fun availableCopies(item: LibraryItem): Int = item.totalCopies - loansOf(item).size

    override fun borrow(student: Student, item: LibraryItem): LibraryResult {
        if (loansOf(student).any { it.item == item }) {
            return LibraryResult.Failure("${student.name} đã mượn \"${item.title}\"")
        }
        // ĐA HÌNH: mỗi loại sách tự quyết định ai được mượn
        item.borrowRestriction(student)?.let { return LibraryResult.Failure(it) }
        // ĐA HÌNH: mỗi loại sinh viên có giới hạn khác nhau
        if (loansOf(student).size >= student.maxBooks) {
            return LibraryResult.Failure("${student.role()} chỉ được mượn tối đa ${student.maxBooks} quyển")
        }
        if (availableCopies(item) <= 0) {
            return LibraryResult.Failure("\"${item.title}\" đã hết bản")
        }
        _records.add(BorrowRecord(student, item))
        return LibraryResult.Success("${student.name} đã mượn \"${item.title}\"")
    }

    override fun returnBook(record: BorrowRecord): LibraryResult =
        if (_records.remove(record)) LibraryResult.Success("Đã trả \"${record.item.title}\"")
        else LibraryResult.Failure("Không tìm thấy phiếu mượn")

    // ---------------- Nội bộ ----------------

    private fun nextStudentId(): String = "SV%03d".format(++studentCounter)
    private fun nextItemId(): String = "S%03d".format(++itemCounter)

    companion object {
        /** Dữ liệu mẫu giống các màn trong Figma (A: 2 sách, B: 1 sách, C: chưa mượn). */
        fun withSampleData(): LibraryManager = LibraryManager().apply {
            addStudent("Nguyen Van A", "CNTT01", isGraduate = false)
            addStudent("Nguyen Thi B", "CNTT02", isGraduate = false)
            addStudent("Nguyen Van C", "KHMT01", isGraduate = true)

            addItem(BookType.BOOK, "Sách 01", "Nguyễn Nhật Ánh", 2018, "", copies = 3)
            addItem(BookType.BOOK, "Sách 02", "Tô Hoài", 2015, "", copies = 2)
            addItem(BookType.TEXTBOOK, "Sách 03", "ĐH GTVT TP.HCM", 2023, "Lập trình di động", copies = 2)
            addItem(BookType.TEXTBOOK, "Sách 04", "ĐH GTVT TP.HCM", 2022, "Cấu trúc dữ liệu", copies = 1)
            addItem(BookType.REFERENCE, "Sách 05", "Robert C. Martin", 2008, "", copies = 1)

            val a = students[0]
            val b = students[1]
            borrow(a, items[0])
            borrow(a, items[1])
            borrow(b, items[0])
            selectStudent(a)
        }
    }
}
