package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,       // e.g. "EMP-204"
    val name: String,
    val role: String,             // "Teacher", "Department Head", "Accountant", "Administrator", "Support Staff"
    val department: String,       // "Science", "Mathematics", "Humanities", "Administration", "Accounts"
    val email: String,
    val phone: String,
    val address: String,
    val joiningDate: String,      // e.g. "2022-09-01"
    val subjectsTaught: String,   // e.g. "Mathematics, Statistics"
    val assignedClasses: String,  // e.g. "Grade 9-A, Grade 10-B"
    val baseSalary: Double,
    val allowances: Double = 0.0,
    val deductions: Double = 0.0,
    val status: String = "Active" // "Active", "On Leave", "Resigned"
) {
    val netSalary: Double
        get() = (baseSalary + allowances - deductions).coerceAtLeast(0.0)
}

@Entity(
    tableName = "leave_requests",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["staffId"])]
)
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: Long,
    val staffName: String,
    val leaveType: String,        // "Casual Leave", "Sick Leave", "Annual Leave", "Maternity/Paternity"
    val startDate: String,        // e.g. "2026-10-05"
    val endDate: String,          // e.g. "2026-10-08"
    val daysCount: Int = 3,
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val appliedDate: String,
    val adminNotes: String? = null
)

data class StaffPayrollSummary(
    val totalStaffCount: Int,
    val totalBaseSalary: Double,
    val totalAllowances: Double,
    val totalDeductions: Double,
    val totalNetPayroll: Double,
    val paidCount: Int = 0,
    val pendingCount: Int = 0
)
