package com.example.bai2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* =====================================================================
 *
 *  4 đặc trưng OOP:
 *  1. TRỪU TƯỢNG (Abstraction): interface Displayable, Borrowable;
 *     abstract class Person, LibraryItem (khai báo "cái gì", lớp con lo "làm thế nào").
 *  2. ĐÓNG GÓI (Encapsulation): thuộc tính private / private set, dữ liệu chỉ
 *     thay đổi qua phương thức có kiểm tra (rename, LibraryManager.borrow, ...).
 *  3. KẾ THỪA (Inheritance): Student : Person, GraduateStudent : Student,
 *     Book : LibraryItem, Textbook : Book, ReferenceBook : Book.
 *  4. ĐA HÌNH (Polymorphism): role(), displayInfo(), maxBooks, loanDays,
 *     canBeBorrowedBy() được ghi đè; giao diện gọi qua kiểu cha.
 * ===================================================================== */

// ---------------------------------------------------------------------
// 1. TRỪU TƯỢNG – interface
// ---------------------------------------------------------------------

/** Đối tượng nào cũng tự mô tả được thông tin của nó. */
interface Displayable {
    fun displayInfo(): String
}

/** Đối tượng có thể cho mượn. */
interface Borrowable {
    /** Số ngày được mượn. */
    val loanDays: Int

    /** Trả về null nếu được mượn, ngược lại trả về lý do không được mượn. */
    fun borrowRestriction(student: Student): String?

    fun canBeBorrowedBy(student: Student): Boolean = borrowRestriction(student) == null
}

// ---------------------------------------------------------------------
// NGƯỜI DÙNG: Person (trừu tượng) → Student → GraduateStudent
// ---------------------------------------------------------------------

abstract class Person(id: String, name: String) : Displayable {

    // ĐÓNG GÓI: id chỉ đọc, name chỉ đổi được qua rename() có kiểm tra
    val id: String = id.trim().uppercase()

    var name: String = name.trim()
        private set

    init {
        require(this.id.isNotEmpty()) { "Mã không được để trống" }
        require(this.name.isNotEmpty()) { "Tên không được để trống" }
    }

    fun rename(newName: String): Boolean {
        if (newName.isBlank()) return false
        name = newName.trim()
        return true
    }

    /** TRỪU TƯỢNG: mỗi loại người tự định nghĩa vai trò. */
    abstract fun role(): String

    override fun displayInfo(): String = "${role()}: $name"
}

/** KẾ THỪA từ Person. */
open class Student(
    id: String,
    name: String,
    val className: String,
) : Person(id, name) {

    /** Số sách tối đa được mượn cùng lúc – lớp con có thể ghi đè. */
    open val maxBooks: Int = 3

    override fun role(): String = "Sinh viên"

    override fun displayInfo(): String = "MSSV $id • Lớp $className"
}

/** KẾ THỪA nhiều tầng + ĐA HÌNH: học viên cao học được mượn nhiều hơn. */
class GraduateStudent(
    id: String,
    name: String,
    className: String,
) : Student(id, name, className) {

    override val maxBooks: Int = 5

    override fun role(): String = "Học viên cao học"
}

// ---------------------------------------------------------------------
// TÀI LIỆU: LibraryItem (trừu tượng) → Book → Textbook / ReferenceBook
// ---------------------------------------------------------------------

abstract class LibraryItem(id: String, title: String, copies: Int) : Displayable, Borrowable {

    val id: String = id.trim().uppercase()
    val title: String = title.trim()

    // ĐÓNG GÓI: số bản chỉ tăng qua addCopies(), không gán trực tiếp từ bên ngoài
    // (dùng state của Compose để giao diện tự cập nhật khi số bản thay đổi)
    var totalCopies: Int by mutableIntStateOf(copies)
        private set

    init {
        require(this.title.isNotEmpty()) { "Tên sách không được để trống" }
        require(copies > 0) { "Số bản phải lớn hơn 0" }
    }

    fun addCopies(amount: Int): Boolean {
        if (amount <= 0) return false
        totalCopies += amount
        return true
    }

    /** Nhãn loại tài liệu – lớp con định nghĩa. */
    abstract val typeLabel: String

    override fun borrowRestriction(student: Student): String? = null
}

/** Sách thường: mượn 14 ngày. */
open class Book(
    id: String,
    title: String,
    val author: String,
    val year: Int,
    copies: Int = 1,
) : LibraryItem(id, title, copies) {

    override val typeLabel: String = "Sách"
    override val loanDays: Int = 14

    override fun displayInfo(): String = "$author • $year"
}

/** Giáo trình: mượn 7 ngày, có môn học. */
class Textbook(
    id: String,
    title: String,
    author: String,
    year: Int,
    val subject: String,
    copies: Int = 1,
) : Book(id, title, author, year, copies) {

    override val typeLabel: String = "Giáo trình"
    override val loanDays: Int = 7

    override fun displayInfo(): String = "${super.displayInfo()} • Môn $subject"
}

/** Sách tham khảo: mượn 3 ngày, chỉ học viên cao học được mượn. */
class ReferenceBook(
    id: String,
    title: String,
    author: String,
    year: Int,
    copies: Int = 1,
) : Book(id, title, author, year, copies) {

    override val typeLabel: String = "Tham khảo"
    override val loanDays: Int = 3

    override fun borrowRestriction(student: Student): String? =
        if (student is GraduateStudent) null
        else "Sách tham khảo chỉ dành cho học viên cao học"
}

/** Các loại sách có thể tạo mới (dùng cho form thêm sách). */
enum class BookType(val label: String) {
    BOOK("Sách"),
    TEXTBOOK("Giáo trình"),
    REFERENCE("Tham khảo");

    /** Factory: tạo đúng lớp con theo loại. */
    fun create(id: String, title: String, author: String, year: Int, subject: String, copies: Int): LibraryItem =
        when (this) {
            BOOK -> Book(id, title, author, year, copies)
            TEXTBOOK -> Textbook(id, title, author, year, subject.ifBlank { "Chung" }, copies)
            REFERENCE -> ReferenceBook(id, title, author, year, copies)
        }
}

// ---------------------------------------------------------------------
// PHIẾU MƯỢN
// ---------------------------------------------------------------------

class BorrowRecord(
    val student: Student,
    val item: LibraryItem,
    private val borrowedAtMillis: Long = System.currentTimeMillis(),
) : Displayable {

    private val dueAtMillis: Long
        get() = borrowedAtMillis + item.loanDays * DAY_MILLIS

    fun borrowedDateText(): String = format(borrowedAtMillis)
    fun dueDateText(): String = format(dueAtMillis)
    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean = now > dueAtMillis

    override fun displayInfo(): String = "Mượn ${borrowedDateText()} • Hạn trả ${dueDateText()}"

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
        fun format(millis: Long): String =
            SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date(millis))
    }
}

// ---------------------------------------------------------------------
// KẾT QUẢ THAO TÁC
// ---------------------------------------------------------------------

sealed class LibraryResult(val message: String) {
    class Success(message: String) : LibraryResult(message)
    class Failure(message: String) : LibraryResult(message)

    val isSuccess: Boolean get() = this is Success
}
