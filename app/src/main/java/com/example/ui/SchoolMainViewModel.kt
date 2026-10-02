package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SchoolDataSeeder
import com.example.data.local.SchoolDatabase
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.data.model.StaffPayrollSummary
import com.example.data.model.Student
import com.example.data.model.StudentAttendance
import com.example.data.model.StudentAttendanceSummary
import com.example.data.model.StudentReportCard
import com.example.data.model.Subject
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    STUDENTS("Students"),
    ATTENDANCE("Attendance"),
    BILLING("Billing & Fees"),
    EXAMS("Exams & Results"),
    STAFF("Staff & Leaves")
}

data class DashboardSummary(
    val totalStudents: Int = 0,
    val activeStudentsCount: Int = 0,
    val totalStaff: Int = 0,
    val activeStaffCount: Int = 0,
    val onLeaveStaffCount: Int = 0,
    val totalCollectedFees: Double = 0.0,
    val totalPendingFees: Double = 0.0,
    val pendingFeeAccountsCount: Int = 0,
    val pendingLeavesCount: Int = 0,
    val recentInvoices: List<FeeRecord> = emptyList()
)

data class DashboardSearchResults(
    val matchingStudents: List<Student> = emptyList(),
    val matchingStaff: List<Staff> = emptyList(),
    val matchingFees: List<FeeRecord> = emptyList()
) {
    val totalMatches: Int get() = matchingStudents.size + matchingStaff.size + matchingFees.size
    val isEmpty: Boolean get() = totalMatches == 0
}

class SchoolMainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SchoolDatabase.getDatabase(application)
    private val repository = SchoolRepository(db)

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _dashboardSearchQuery = MutableStateFlow("")
    val dashboardSearchQuery: StateFlow<String> = _dashboardSearchQuery.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow("All")
    val selectedGradeFilter: StateFlow<String> = _selectedGradeFilter.asStateFlow()

    private val _studentStatusFilter = MutableStateFlow("All")
    val studentStatusFilter: StateFlow<String> = _studentStatusFilter.asStateFlow()

    private val _feeStatusFilter = MutableStateFlow("All")
    val feeStatusFilter: StateFlow<String> = _feeStatusFilter.asStateFlow()

    private val _staffRoleFilter = MutableStateFlow("All")
    val staffRoleFilter: StateFlow<String> = _staffRoleFilter.asStateFlow()

    private val _leaveStatusFilter = MutableStateFlow("All")
    val leaveStatusFilter: StateFlow<String> = _leaveStatusFilter.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    // Base flows from repository
    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feeRecords: StateFlow<List<FeeRecord>> = repository.allFeeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examResults: StateFlow<List<ExamResult>> = repository.allExamResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffList: StateFlow<List<Staff>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaveRequests: StateFlow<List<LeaveRequest>> = repository.allLeaveRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<StudentAttendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedAttendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    private val _attendanceFilterGrade = MutableStateFlow("All")
    val attendanceFilterGrade: StateFlow<String> = _attendanceFilterGrade.asStateFlow()

    // Derived Student Attendance Summaries Flow
    val studentAttendanceSummaries: StateFlow<List<StudentAttendanceSummary>> = combine(
        students,
        allAttendance,
        _selectedAttendanceDate,
        _attendanceFilterGrade
    ) { studentList, attendanceList, date, gradeFilter ->
        val filteredStudents = if (gradeFilter == "All") studentList else studentList.filter { it.grade == gradeFilter }
        val attendanceByStudent = attendanceList.groupBy { it.studentId }

        filteredStudents.map { student ->
            val logs = attendanceByStudent[student.id] ?: emptyList()
            val totalDays = logs.size
            val presentCount = logs.count { it.status == "Present" }
            val absentCount = logs.count { it.status == "Absent" }
            val lateCount = logs.count { it.status == "Late" }
            val excusedCount = logs.count { it.status == "Excused" }
            val pct = if (totalDays > 0) ((presentCount + lateCount).toDouble() / totalDays) * 100.0 else 100.0

            val todayLog = logs.firstOrNull { it.date == date }

            StudentAttendanceSummary(
                student = student,
                totalRecordedDays = totalDays,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendancePercentage = pct,
                todayStatus = todayLog?.status,
                todayCheckInTime = todayLog?.checkInTime,
                todayRemarks = todayLog?.remarks
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students Flow
    val filteredStudents = combine(students, _studentSearchQuery, _selectedGradeFilter, _studentStatusFilter) { list, query, grade, status ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNo.contains(query, ignoreCase = true) ||
                    student.admissionNo.contains(query, ignoreCase = true)
            val matchesGrade = grade == "All" || student.grade == grade
            val matchesStatus = status == "All" || student.status.equals(status, ignoreCase = true)
            matchesQuery && matchesGrade && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Fees Flow
    val filteredFees = combine(feeRecords, _feeStatusFilter) { list, filter ->
        if (filter == "All") list else list.filter { it.paymentStatus.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Staff Flow
    val filteredStaff = combine(staffList, _staffRoleFilter) { list, filter ->
        if (filter == "All") list else list.filter { it.role.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Leaves Flow
    val filteredLeaves = combine(leaveRequests, _leaveStatusFilter) { list, filter ->
        if (filter == "All") list else list.filter { it.status.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Universal Dashboard Search Results Flow
    val dashboardSearchResults: StateFlow<DashboardSearchResults> = combine(
        students,
        staffList,
        feeRecords,
        _dashboardSearchQuery
    ) { sList, stList, fList, query ->
        val q = query.trim()
        if (q.isBlank()) {
            DashboardSearchResults()
        } else {
            val matchingStudents = sList.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.admissionNo.contains(q, ignoreCase = true) ||
                it.rollNo.contains(q, ignoreCase = true) ||
                it.grade.contains(q, ignoreCase = true) ||
                it.id.toString() == q
            }.take(5)

            val matchingStaff = stList.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.employeeId.contains(q, ignoreCase = true) ||
                it.role.contains(q, ignoreCase = true) ||
                it.department.contains(q, ignoreCase = true) ||
                it.id.toString() == q
            }.take(5)

            val matchingFees = fList.filter {
                it.invoiceNo.contains(q, ignoreCase = true) ||
                (it.receiptNo ?: "").contains(q, ignoreCase = true) ||
                it.monthYear.contains(q, ignoreCase = true) ||
                it.paymentStatus.contains(q, ignoreCase = true) ||
                it.id.toString() == q
            }.take(5)

            DashboardSearchResults(
                matchingStudents = matchingStudents,
                matchingStaff = matchingStaff,
                matchingFees = matchingFees
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSearchResults())

    // Dashboard KPI State
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        students,
        staffList,
        feeRecords,
        leaveRequests
    ) { stds, stff, fees, lvs ->
        val totalCollected = fees.sumOf { it.paidAmount }
        val totalPending = fees.sumOf { it.pendingAmount }
        val pendingCount = fees.count { it.pendingAmount > 0 }
        val pendingLeaves = lvs.count { it.status == "Pending" }
        val activeStaff = stff.count { it.status == "Active" }
        val onLeaveStaff = stff.count { it.status == "On Leave" }
        val activeStudents = stds.count { it.status == "Active" }
        DashboardSummary(
            totalStudents = stds.size,
            activeStudentsCount = activeStudents,
            totalStaff = stff.size,
            activeStaffCount = activeStaff,
            onLeaveStaffCount = onLeaveStaff,
            totalCollectedFees = totalCollected,
            totalPendingFees = totalPending,
            pendingFeeAccountsCount = pendingCount,
            pendingLeavesCount = pendingLeaves,
            recentInvoices = fees.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    init {
        viewModelScope.launch {
            SchoolDataSeeder.seedIfEmpty(db.schoolDao())
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedGradeFilter(grade: String) {
        _selectedGradeFilter.value = grade
    }

    fun setStudentStatusFilter(status: String) {
        _studentStatusFilter.value = status
    }

    fun setFeeStatusFilter(status: String) {
        _feeStatusFilter.value = status
    }

    fun setStaffRoleFilter(role: String) {
        _staffRoleFilter.value = role
    }

    fun setLeaveStatusFilter(status: String) {
        _leaveStatusFilter.value = status
    }

    // Student CRUD
    fun addOrUpdateStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.addStudent(student)
                _toastMessages.emit("Student added successfully.")
            } else {
                repository.updateStudent(student)
                _toastMessages.emit("Student record updated.")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastMessages.emit("Student ${student.name} deleted.")
        }
    }

    // Fee Operations
    fun createFeeInvoice(invoice: FeeRecord) {
        viewModelScope.launch {
            repository.createFeeRecord(invoice)
            _toastMessages.emit("Invoice ${invoice.invoiceNo} generated.")
        }
    }

    fun recordFeePayment(invoiceId: Long, amount: Double, method: String, remarks: String?) {
        viewModelScope.launch {
            val success = repository.recordFeePayment(invoiceId, amount, method, remarks)
            if (success) {
                _toastMessages.emit("Payment recorded successfully.")
            } else {
                _toastMessages.emit("Failed to locate invoice.")
            }
        }
    }

    fun generateBatchMonthlyBills(monthYear: String, dueDate: String) {
        viewModelScope.launch {
            val count = repository.generateMonthlyInvoicesForAllStudents(monthYear, dueDate)
            _toastMessages.emit("Generated $count invoices for $monthYear.")
        }
    }

    // Exam & Results
    fun addExam(exam: Exam) {
        viewModelScope.launch {
            repository.addExam(exam)
            _toastMessages.emit("Exam ${exam.name} scheduled.")
        }
    }

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            repository.addSubject(subject)
            _toastMessages.emit("Subject ${subject.name} added.")
        }
    }

    fun recordSubjectMarks(
        examId: Long,
        studentId: Long,
        subjectId: Long,
        marksObtained: Double,
        maxMarks: Double,
        gradeLetter: String,
        gradePoint: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val result = ExamResult(
                examId = examId,
                studentId = studentId,
                subjectId = subjectId,
                marksObtained = marksObtained,
                maxMarks = maxMarks,
                gradeLetter = gradeLetter,
                gradePoint = gradePoint,
                remarks = remarks
            )
            repository.saveExamResult(result)
            _toastMessages.emit("Marks recorded successfully.")
        }
    }

    suspend fun getStudentReportCard(examId: Long, studentId: Long): StudentReportCard? {
        return repository.getStudentReportCard(examId, studentId)
    }

    // Staff Management
    fun addOrUpdateStaff(staff: Staff) {
        viewModelScope.launch {
            if (staff.id == 0L) {
                repository.addStaff(staff)
                _toastMessages.emit("Staff member ${staff.name} added.")
            } else {
                repository.updateStaff(staff)
                _toastMessages.emit("Staff details updated.")
            }
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
            _toastMessages.emit("Staff member removed.")
        }
    }

    fun calculatePayroll(): StaffPayrollSummary {
        return repository.calculatePayrollSummary(staffList.value)
    }

    // Leave Management
    fun submitLeaveRequest(leave: LeaveRequest) {
        viewModelScope.launch {
            repository.addLeaveRequest(leave)
            _toastMessages.emit("Leave request submitted.")
        }
    }

    fun updateLeaveStatus(leaveId: Long, newStatus: String, adminNotes: String?) {
        viewModelScope.launch {
            repository.updateLeaveStatus(leaveId, newStatus, adminNotes)
            _toastMessages.emit("Leave request $newStatus.")
        }
    }

    // Attendance Management
    fun setAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
    }

    fun setAttendanceFilterGrade(grade: String) {
        _attendanceFilterGrade.value = grade
    }

    fun recordStudentCheckIn(
        studentId: Long,
        status: String,
        checkInTime: String? = null,
        remarks: String? = null,
        date: String? = null
    ) {
        viewModelScope.launch {
            val targetDate = date ?: _selectedAttendanceDate.value
            val time = checkInTime ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            repository.recordAttendance(
                studentId = studentId,
                date = targetDate,
                status = status,
                checkInTime = time,
                remarks = remarks
            )
            _toastMessages.emit("Check-in marked: $status")
        }
    }

    fun markAllStudentsPresentForDate(date: String? = null) {
        viewModelScope.launch {
            val targetDate = date ?: _selectedAttendanceDate.value
            val activeStudents = students.value.filter { it.status == "Active" }
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            repository.markAllStudentsPresent(targetDate, activeStudents, time)
            _toastMessages.emit("Marked ${activeStudents.size} active students Present for $targetDate.")
        }
    }

    // Universal Search
    fun setDashboardSearchQuery(query: String) {
        _dashboardSearchQuery.value = query
    }

    // Local Admin Notifications Trigger
    fun triggerAdminNotificationAlerts(context: android.content.Context) {
        val msg = com.example.util.NotificationHelper.checkAndTriggerAdminAlerts(
            context = context,
            students = students.value,
            feeRecords = feeRecords.value,
            attendanceList = allAttendance.value,
            dateStr = selectedAttendanceDate.value
        )
        viewModelScope.launch {
            _toastMessages.emit(msg)
        }
    }
}
