package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // e.g. "Annual Examination 2026"
    val academicYear: String,   // e.g. "2025-2026"
    val term: String,           // e.g. "Term 2", "Midterm", "Final"
    val startDate: String,
    val status: String = "Published" // "Draft", "Published"
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // e.g. "Mathematics", "Physics"
    val code: String,           // e.g. "MTH-101"
    val grade: String,          // e.g. "Grade 10" or "All"
    val maxMarks: Double = 100.0,
    val passMarks: Double = 40.0
)

@Entity(
    tableName = "exam_results",
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["examId"]),
        Index(value = ["studentId"]),
        Index(value = ["subjectId"]),
        Index(value = ["examId", "studentId", "subjectId"], unique = true)
    ]
)
data class ExamResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val subjectId: Long,
    val marksObtained: Double,
    val maxMarks: Double = 100.0,
    val gradeLetter: String,    // "A+", "A", "B", "C", "D", "F"
    val gradePoint: Double,     // 4.0, 3.5, etc.
    val remarks: String = "Satisfactory performance"
)

data class SubjectResultDetail(
    val resultId: Long,
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val marksObtained: Double,
    val maxMarks: Double,
    val passMarks: Double,
    val gradeLetter: String,
    val gradePoint: Double,
    val remarks: String
) {
    val isPassed: Boolean get() = marksObtained >= passMarks
    val percentage: Double get() = if (maxMarks > 0) (marksObtained / maxMarks) * 100.0 else 0.0
}

data class StudentReportCard(
    val student: Student,
    val exam: Exam,
    val subjectResults: List<SubjectResultDetail>,
    val totalMarksObtained: Double,
    val totalMaxMarks: Double,
    val overallPercentage: Double,
    val overallGrade: String,
    val gpa: Double,
    val passedCount: Int,
    val failedCount: Int,
    val classRank: Int = 1,
    val totalStudentsInClass: Int = 1,
    val attendancePercentage: Double = 94.5,
    val generalRemarks: String = "Promoted to next grade with honors."
)

data class StudentGpaSummary(
    val student: Student,
    val examId: Long,
    val examName: String,
    val totalMarksObtained: Double,
    val totalMaxMarks: Double,
    val overallPercentage: Double,
    val overallGrade: String,
    val gpa: Double,
    val subjectsEvaluated: Int,
    val passedCount: Int,
    val failedCount: Int
)
