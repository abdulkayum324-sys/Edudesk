package com.example.data.repository

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
import com.example.data.model.StaffPayrollSummary
import com.example.data.model.Student
import com.example.data.model.StudentAttendance
import com.example.data.model.StudentReportCard
import com.example.data.model.Subject
import com.example.data.model.SubjectResultDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolRepository(
    private val studentDao: StudentDao,
    private val feeDao: FeeDao,
    private val staffDao: StaffDao,
    private val examDao: ExamDao,
    private val attendanceDao: AttendanceDao,
    private val schoolDao: com.example.data.local.SchoolDao? = null
) {
    constructor(database: SchoolDatabase) : this(
        studentDao = database.studentDao(),
        feeDao = database.feeDao(),
        staffDao = database.staffDao(),
        examDao = database.examDao(),
        attendanceDao = database.attendanceDao(),
        schoolDao = database.schoolDao()
    )

    // --- School Profile ---
    val schoolProfile: Flow<com.example.data.model.SchoolProfile?> =
        schoolDao?.getSchoolProfile() ?: kotlinx.coroutines.flow.flowOf(com.example.data.model.SchoolProfile())

    suspend fun updateSchoolProfile(profile: com.example.data.model.SchoolProfile) {
        schoolDao?.insertOrUpdateSchoolProfile(profile)
    }

    // --- Student Attendance ---
    val allAttendance: Flow<List<StudentAttendance>> = attendanceDao.getAllAttendance()

    fun getAttendanceForDate(date: String): Flow<List<StudentAttendance>> =
        attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long): Flow<List<StudentAttendance>> =
        attendanceDao.getAttendanceForStudent(studentId)

    suspend fun recordAttendance(
        studentId: Long,
        date: String,
        status: String,
        checkInTime: String? = null,
        remarks: String? = null
    ): Long {
        val existing = attendanceDao.getStudentAttendanceForDate(studentId, date)
        val record = (existing ?: StudentAttendance(studentId = studentId, date = date)).copy(
            status = status,
            checkInTime = checkInTime ?: existing?.checkInTime,
            remarks = remarks ?: existing?.remarks
        )
        return attendanceDao.insertOrUpdateAttendance(record)
    }

    suspend fun markAllStudentsPresent(date: String, students: List<Student>, timeStr: String = "08:00 AM") {
        val records = students.map { s ->
            val existing = attendanceDao.getStudentAttendanceForDate(s.id, date)
            (existing ?: StudentAttendance(studentId = s.id, date = date)).copy(
                status = "Present",
                checkInTime = existing?.checkInTime ?: timeStr
            )
        }
        attendanceDao.insertOrUpdateAttendanceList(records)
    }

    // --- Students ---
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    suspend fun getStudentById(studentId: Long): Student? = studentDao.getStudentById(studentId)

    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)

    suspend fun addStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    // --- Fees & Invoices ---
    val allFeeRecords: Flow<List<FeeRecord>> = feeDao.getAllFeeRecords()
    val pendingFeeRecords: Flow<List<FeeRecord>> = feeDao.getPendingFeeRecords()

    fun getFeeRecordsForStudent(studentId: Long): Flow<List<FeeRecord>> =
        feeDao.getFeeRecordsForStudent(studentId)

    suspend fun getFeeRecordById(invoiceId: Long): FeeRecord? = feeDao.getFeeRecordById(invoiceId)

    suspend fun createFeeRecord(record: FeeRecord): Long = feeDao.insertFeeRecord(record)

    suspend fun updateFeeRecord(record: FeeRecord) = feeDao.updateFeeRecord(record)

    suspend fun deleteFeeRecord(record: FeeRecord) = feeDao.deleteFeeRecord(record)

    suspend fun recordFeePayment(
        invoiceId: Long,
        amountToPay: Double,
        method: String,
        remarks: String?
    ): Boolean {
        val invoice = feeDao.getFeeRecordById(invoiceId) ?: return false
        val newPaid = (invoice.paidAmount + amountToPay).coerceAtMost(invoice.totalAmount)
        val status = if (newPaid >= invoice.totalAmount) "Paid" else "Partial"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val receiptNumber = invoice.receiptNo ?: "REC-${System.currentTimeMillis() % 100000}"

        val updated = invoice.copy(
            paidAmount = newPaid,
            paymentStatus = status,
            paymentDate = currentDate,
            paymentMethod = method,
            receiptNo = receiptNumber,
            remarks = remarks ?: invoice.remarks
        )
        feeDao.updateFeeRecord(updated)
        return true
    }

    suspend fun generateMonthlyInvoicesForAllStudents(
        monthYear: String,
        dueDate: String,
        libraryFee: Double = 30.0,
        transportFee: Double = 50.0,
        labFee: Double = 40.0
    ): Int {
        val students = studentDao.getAllStudents().first().filter { it.status == "Active" }
        var generatedCount = 0
        val timestamp = System.currentTimeMillis() % 10000

        students.forEachIndexed { index, student ->
            val total = student.monthlyFee + libraryFee + transportFee + labFee
            val invoice = FeeRecord(
                studentId = student.id,
                invoiceNo = "INV-${monthYear.take(3).uppercase()}-${timestamp + index}",
                monthYear = monthYear,
                dueDate = dueDate,
                tuitionFee = student.monthlyFee,
                libraryFee = libraryFee,
                transportFee = transportFee,
                labOrExamFee = labFee,
                otherFee = 0.0,
                totalAmount = total,
                paidAmount = 0.0,
                paymentStatus = "Pending",
                remarks = "Auto-generated monthly tuition & campus dues"
            )
            feeDao.insertFeeRecord(invoice)
            generatedCount++
        }
        return generatedCount
    }

    // --- Exams & Results ---
    val allExams: Flow<List<Exam>> = examDao.getAllExams()
    val allSubjects: Flow<List<Subject>> = examDao.getAllSubjects()
    val allExamResults: Flow<List<ExamResult>> = examDao.getAllExamResults()

    fun getResultsForStudentInExam(examId: Long, studentId: Long): Flow<List<ExamResult>> =
        examDao.getResultsForStudentInExam(examId, studentId)

    suspend fun addExam(exam: Exam): Long = examDao.insertExam(exam)

    suspend fun addSubject(subject: Subject): Long = examDao.insertSubject(subject)

    suspend fun saveExamResult(result: ExamResult): Long = examDao.insertExamResult(result)

    suspend fun updateExamResult(result: ExamResult) = examDao.updateExamResult(result)

    suspend fun deleteExamResult(result: ExamResult) = examDao.deleteExamResult(result)

    suspend fun getStudentReportCard(examId: Long, studentId: Long): StudentReportCard? {
        val student = studentDao.getStudentById(studentId) ?: return null
        val exam = examDao.getExamById(examId) ?: return null
        val subjects = examDao.getAllSubjects().first().associateBy { it.id }
        val rawResults = examDao.getResultsForStudentInExam(examId, studentId).first()

        val subjectResults = rawResults.map { r ->
            val subject = subjects[r.subjectId]
            SubjectResultDetail(
                resultId = r.id,
                subjectId = r.subjectId,
                subjectName = subject?.name ?: "Subject #${r.subjectId}",
                subjectCode = subject?.code ?: "SUB",
                marksObtained = r.marksObtained,
                maxMarks = r.maxMarks,
                passMarks = subject?.passMarks ?: 40.0,
                gradeLetter = r.gradeLetter,
                gradePoint = r.gradePoint,
                remarks = r.remarks
            )
        }

        val totalObtained = subjectResults.sumOf { it.marksObtained }
        val totalMax = subjectResults.sumOf { it.maxMarks }
        val percentage = if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0
        val avgGpa = if (subjectResults.isNotEmpty()) subjectResults.map { it.gradePoint }.average() else 0.0
        val passed = subjectResults.count { it.isPassed }
        val failed = subjectResults.count { !it.isPassed }

        val overallGrade = when {
            percentage >= 90 -> "A+"
            percentage >= 80 -> "A"
            percentage >= 70 -> "B+"
            percentage >= 60 -> "B"
            percentage >= 50 -> "C"
            percentage >= 40 -> "D"
            else -> "F"
        }

        return StudentReportCard(
            student = student,
            exam = exam,
            subjectResults = subjectResults,
            totalMarksObtained = totalObtained,
            totalMaxMarks = totalMax,
            overallPercentage = percentage,
            overallGrade = overallGrade,
            gpa = avgGpa,
            passedCount = passed,
            failedCount = failed,
            classRank = 1,
            totalStudentsInClass = 25,
            attendancePercentage = 95.0,
            generalRemarks = if (failed == 0) "Promoted with distinction." else "Needs remedial tutoring in failed subjects."
        )
    }

    // --- Staff Management ---
    val allStaff: Flow<List<Staff>> = staffDao.getAllStaff()

    suspend fun getStaffById(staffId: Long): Staff? = staffDao.getStaffById(staffId)

    suspend fun addStaff(staff: Staff): Long = staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: Staff) = staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)

    fun calculatePayrollSummary(staffList: List<Staff>): StaffPayrollSummary {
        val totalBase = staffList.sumOf { it.baseSalary }
        val totalAllowances = staffList.sumOf { it.allowances }
        val totalDeductions = staffList.sumOf { it.deductions }
        val totalNet = staffList.sumOf { it.netSalary }
        return StaffPayrollSummary(
            totalStaffCount = staffList.size,
            totalBaseSalary = totalBase,
            totalAllowances = totalAllowances,
            totalDeductions = totalDeductions,
            totalNetPayroll = totalNet,
            paidCount = staffList.count { it.status == "Active" },
            pendingCount = staffList.count { it.status != "Active" }
        )
    }

    // --- Leave Requests ---
    val allLeaveRequests: Flow<List<LeaveRequest>> = staffDao.getAllLeaveRequests()

    suspend fun addLeaveRequest(leave: LeaveRequest): Long = staffDao.insertLeaveRequest(leave)

    suspend fun updateLeaveStatus(leaveId: Long, newStatus: String, notes: String?) {
        val leaves = staffDao.getAllLeaveRequests().first()
        val leave = leaves.firstOrNull { it.id == leaveId } ?: return
        staffDao.updateLeaveRequest(leave.copy(status = newStatus, adminNotes = notes))
    }

    suspend fun deleteLeaveRequest(leave: LeaveRequest) = staffDao.deleteLeaveRequest(leave)

    // --- User Accounts (RBAC) ---
    val allUserAccounts: Flow<List<com.example.data.model.UserAccount>> =
        schoolDao?.getAllUserAccounts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun createUserAccount(user: com.example.data.model.UserAccount): Long =
        schoolDao?.insertUserAccount(user) ?: 0L

    suspend fun updateUserAccount(user: com.example.data.model.UserAccount) {
        schoolDao?.updateUserAccount(user)
    }

    suspend fun deleteUserAccount(user: com.example.data.model.UserAccount) {
        schoolDao?.deleteUserAccount(user)
    }
}
