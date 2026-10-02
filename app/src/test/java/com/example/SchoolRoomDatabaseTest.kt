package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AttendanceDao
import com.example.data.local.ExamDao
import com.example.data.local.FeeDao
import com.example.data.local.SchoolDatabase
import com.example.data.local.StaffDao
import com.example.data.local.StudentDao
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentAttendance
import com.example.data.model.Subject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SchoolRoomDatabaseTest {

    private lateinit var db: SchoolDatabase
    private lateinit var studentDao: StudentDao
    private lateinit var feeDao: FeeDao
    private lateinit var staffDao: StaffDao
    private lateinit var examDao: ExamDao
    private lateinit var attendanceDao: AttendanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SchoolDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        studentDao = db.studentDao()
        feeDao = db.feeDao()
        staffDao = db.staffDao()
        examDao = db.examDao()
        attendanceDao = db.attendanceDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testStudentInsertAndQuery() = runBlocking {
        val student = Student(
            id = 1,
            admissionNo = "ADM-101",
            name = "Test Student",
            grade = "Grade 10",
            section = "A",
            rollNo = "101",
            guardianName = "Guardian Test",
            phone = "1234567890",
            email = "test@school.edu",
            monthlyFee = 450.0,
            admissionDate = "2026-09-01"
        )
        studentDao.insertStudent(student)

        val retrieved = studentDao.getStudentById(1)
        assertNotNull(retrieved)
        assertEquals("Test Student", retrieved?.name)
        assertEquals(450.0, retrieved?.monthlyFee ?: 0.0, 0.01)

        val all = studentDao.getAllStudents().first()
        assertEquals(1, all.size)
    }

    @Test
    fun testFeeInsertAndPendingQuery() = runBlocking {
        val student = Student(
            id = 1,
            admissionNo = "ADM-101",
            name = "Test Student",
            grade = "Grade 10",
            section = "A",
            rollNo = "101",
            guardianName = "Guardian Test",
            phone = "1234567890",
            email = "test@school.edu",
            monthlyFee = 450.0,
            admissionDate = "2026-09-01"
        )
        studentDao.insertStudent(student)

        val fee = FeeRecord(
            id = 10,
            studentId = 1,
            invoiceNo = "INV-001",
            monthYear = "October 2026",
            dueDate = "2026-10-15",
            tuitionFee = 450.0,
            libraryFee = 30.0,
            totalAmount = 480.0,
            paidAmount = 200.0,
            paymentStatus = "Partial"
        )
        feeDao.insertFeeRecord(fee)

        val pendingList = feeDao.getPendingFeeRecords().first()
        assertEquals(1, pendingList.size)
        assertEquals(280.0, pendingList[0].pendingAmount, 0.01)
    }

    @Test
    fun testFeeTransactionRecording() = runBlocking {
        val student = Student(
            id = 2,
            admissionNo = "ADM-102",
            name = "Alice Transaction",
            grade = "Grade 9",
            section = "B",
            rollNo = "102",
            guardianName = "Guardian Alice",
            phone = "1234567890",
            email = "alice@school.edu",
            monthlyFee = 400.0,
            admissionDate = "2026-09-01"
        )
        studentDao.insertStudent(student)

        val fee = FeeRecord(
            id = 20,
            studentId = 2,
            invoiceNo = "INV-002",
            monthYear = "October 2026",
            dueDate = "2026-10-15",
            tuitionFee = 400.0,
            libraryFee = 0.0,
            totalAmount = 400.0,
            paidAmount = 0.0,
            paymentStatus = "Pending"
        )
        feeDao.insertFeeRecord(fee)

        // Record a transaction of $400 via Online/UPI
        val updated = fee.copy(
            paidAmount = 400.0,
            paymentStatus = "Paid",
            paymentDate = "2026-10-05",
            paymentMethod = "Online/UPI",
            receiptNo = "REC-99887",
            remarks = "Paid via portal"
        )
        feeDao.updateFeeRecord(updated)

        val retrieved = feeDao.getFeeRecordById(20)
        assertNotNull(retrieved)
        assertEquals(400.0, retrieved?.paidAmount ?: 0.0, 0.01)
        assertEquals(0.0, retrieved?.pendingAmount ?: 1.0, 0.01)
        assertEquals("Paid", retrieved?.paymentStatus)
        assertEquals("REC-99887", retrieved?.receiptNo)
        assertEquals("Online/UPI", retrieved?.paymentMethod)
    }

    @Test
    fun testStaffAndLeaveRequest() = runBlocking {
        val staff = Staff(
            id = 1,
            employeeId = "EMP-001",
            name = "John Teacher",
            role = "Teacher",
            department = "Science",
            email = "john@school.edu",
            phone = "555-4321",
            address = "Campus Block B",
            joiningDate = "2024-01-15",
            subjectsTaught = "Physics",
            assignedClasses = "Grade 10",
            baseSalary = 3800.0,
            allowances = 400.0,
            deductions = 200.0
        )
        staffDao.insertStaff(staff)

        val retrievedStaff = staffDao.getStaffById(1)
        assertNotNull(retrievedStaff)
        assertEquals(4000.0, retrievedStaff?.netSalary ?: 0.0, 0.01)

        val leave = LeaveRequest(
            id = 1,
            staffId = 1,
            staffName = "John Teacher",
            leaveType = "Sick Leave",
            startDate = "2026-10-01",
            endDate = "2026-10-02",
            daysCount = 2,
            reason = "Medical rest",
            status = "Pending",
            appliedDate = "2026-09-30"
        )
        staffDao.insertLeaveRequest(leave)

        val pendingLeaves = staffDao.getLeaveRequestsByStatus("Pending").first()
        assertEquals(1, pendingLeaves.size)
        assertEquals("Medical rest", pendingLeaves[0].reason)
    }

    @Test
    fun testExamMarksAndGpaCalculation() = runBlocking {
        // 1. Create student
        val student = Student(
            id = 5,
            admissionNo = "ADM-205",
            name = "David Scholar",
            grade = "Grade 10",
            section = "A",
            rollNo = "105",
            guardianName = "Mrs. Scholar",
            phone = "555-9876",
            email = "david@school.edu",
            monthlyFee = 450.0,
            admissionDate = "2026-09-01"
        )
        studentDao.insertStudent(student)

        // 2. Create Exam & Subjects
        val exam = Exam(
            id = 1,
            name = "Midterm Examination 2026",
            academicYear = "2025-2026",
            term = "Term 1",
            startDate = "2026-10-10",
            status = "Published"
        )
        examDao.insertExam(exam)

        val math = Subject(id = 1, name = "Mathematics", code = "MTH-101", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0)
        val science = Subject(id = 2, name = "Physics", code = "PHY-101", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0)
        val english = Subject(id = 3, name = "English", code = "ENG-101", grade = "Grade 10", maxMarks = 100.0, passMarks = 40.0)
        examDao.insertSubjects(listOf(math, science, english))

        // 3. Add student marks per subject
        // Math: 90/100 -> A+ (4.0)
        examDao.insertExamResult(ExamResult(id = 1, examId = 1, studentId = 5, subjectId = 1, marksObtained = 90.0, maxMarks = 100.0, gradeLetter = "A+", gradePoint = 4.0, remarks = "Exemplary"))
        // Physics: 85/100 -> A (3.7)
        examDao.insertExamResult(ExamResult(id = 2, examId = 1, studentId = 5, subjectId = 2, marksObtained = 85.0, maxMarks = 100.0, gradeLetter = "A", gradePoint = 3.7, remarks = "Very Good"))
        // English: 80/100 -> A (3.5)
        examDao.insertExamResult(ExamResult(id = 3, examId = 1, studentId = 5, subjectId = 3, marksObtained = 80.0, maxMarks = 100.0, gradeLetter = "A", gradePoint = 3.5, remarks = "Good"))

        // 4. Retrieve and verify stored marks
        val results = examDao.getResultsForStudentInExam(1, 5).first()
        assertEquals(3, results.size)

        // 5. Verify GPA calculation
        val avgGpa = results.map { it.gradePoint }.average()
        val totalMarks = results.sumOf { it.marksObtained }
        val percentage = (totalMarks / 300.0) * 100.0

        assertEquals(255.0, totalMarks, 0.01)
        assertEquals(85.0, percentage, 0.01)
        assertEquals(3.73, avgGpa, 0.02)
    }

    @Test
    fun testStudentAttendanceDailyCheckInAndSummary() = runBlocking {
        // 1. Insert a student
        val student = Student(
            id = 10,
            admissionNo = "ADM-310",
            name = "Benjamin Franklin",
            grade = "Grade 10",
            section = "A",
            rollNo = "110",
            guardianName = "Josiah Franklin",
            phone = "555-1776",
            email = "ben@school.edu",
            monthlyFee = 450.0,
            admissionDate = "2026-09-01"
        )
        studentDao.insertStudent(student)

        // 2. Insert attendance check-ins across 4 days
        val day1 = StudentAttendance(id = 1, studentId = 10, date = "2026-10-01", status = "Present", checkInTime = "07:55 AM")
        val day2 = StudentAttendance(id = 2, studentId = 10, date = "2026-10-02", status = "Late", checkInTime = "08:25 AM", remarks = "Traffic delay")
        val day3 = StudentAttendance(id = 3, studentId = 10, date = "2026-10-03", status = "Absent", remarks = "Fever")
        val day4 = StudentAttendance(id = 4, studentId = 10, date = "2026-10-04", status = "Present", checkInTime = "07:50 AM")
        attendanceDao.insertOrUpdateAttendanceList(listOf(day1, day2, day3, day4))

        // 3. Query attendance for a specific date
        val dateRecords = attendanceDao.getAttendanceForDate("2026-10-02").first()
        assertEquals(1, dateRecords.size)
        assertEquals("Late", dateRecords[0].status)
        assertEquals("08:25 AM", dateRecords[0].checkInTime)

        // 4. Query student's full attendance history
        val studentRecords = attendanceDao.getAttendanceForStudent(10).first()
        assertEquals(4, studentRecords.size)

        // 5. Update/Check-in change
        val updatedDay3 = day3.copy(status = "Excused", remarks = "Doctor note submitted")
        attendanceDao.insertOrUpdateAttendance(updatedDay3)

        val retrievedDay3 = attendanceDao.getStudentAttendanceForDate(10, "2026-10-03")
        assertNotNull(retrievedDay3)
        assertEquals("Excused", retrievedDay3?.status)
        assertEquals("Doctor note submitted", retrievedDay3?.remarks)
    }
}
