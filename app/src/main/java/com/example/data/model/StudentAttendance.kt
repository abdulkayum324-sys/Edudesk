package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["date"]),
        Index(value = ["studentId", "date"], unique = true)
    ]
)
data class StudentAttendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String,               // Format "yyyy-MM-dd" e.g., "2026-10-01"
    val status: String = "Present", // "Present", "Absent", "Late", "Excused"
    val checkInTime: String? = null,// e.g. "08:15 AM"
    val remarks: String? = null
)

data class StudentAttendanceSummary(
    val student: Student,
    val totalRecordedDays: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val excusedCount: Int,
    val attendancePercentage: Double,
    val todayStatus: String?,
    val todayCheckInTime: String?,
    val todayRemarks: String?
)
