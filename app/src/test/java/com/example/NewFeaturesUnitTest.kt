package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentAttendance
import com.example.data.model.StudentReportCard
import com.example.data.model.Subject
import com.example.data.model.SubjectResultDetail
import com.example.ui.DashboardSearchResults
import com.example.util.NotificationHelper
import com.example.util.PdfReportCardUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NewFeaturesUnitTest {

    @Test
    fun testDashboardSearchResultsFiltering() {
        val students = listOf(
            Student(id = 1, admissionNo = "ADM-101", name = "Emma Watson", grade = "Grade 10", section = "A", rollNo = "101", guardianName = "Chris", phone = "555-01", email = "emma@school.edu", monthlyFee = 450.0, admissionDate = "2026-09-01"),
            Student(id = 2, admissionNo = "ADM-102", name = "Liam Vance", grade = "Grade 10", section = "B", rollNo = "102", guardianName = "David", phone = "555-02", email = "liam@school.edu", monthlyFee = 450.0, admissionDate = "2026-09-01")
        )

        val staff = listOf(
            Staff(
                id = 1,
                employeeId = "EMP-001",
                name = "Dr. Arthur Pendelton",
                role = "Principal",
                department = "Administration",
                email = "arthur@school.edu",
                phone = "555-10",
                address = "42 Campus Way",
                joiningDate = "2018-08-01",
                subjectsTaught = "Leadership",
                assignedClasses = "All",
                baseSalary = 5000.0
            ),
            Staff(
                id = 2,
                employeeId = "EMP-002",
                name = "Prof. Emma Holloway",
                role = "Teacher",
                department = "Mathematics",
                email = "holloway@school.edu",
                phone = "555-11",
                address = "18 Academy Blvd",
                joiningDate = "2020-08-01",
                subjectsTaught = "Mathematics",
                assignedClasses = "Grade 10-A",
                baseSalary = 3800.0
            )
        )

        val fees = listOf(
            FeeRecord(id = 1, studentId = 1, invoiceNo = "INV-2026-001", monthYear = "October 2026", dueDate = "2026-10-15", tuitionFee = 450.0, totalAmount = 450.0, paidAmount = 0.0, paymentStatus = "Pending")
        )

        // 1. Search "Emma" should match 1 student and 1 staff
        val q1 = "Emma"
        val sMatch = students.filter { it.name.contains(q1, ignoreCase = true) }
        val stMatch = staff.filter { it.name.contains(q1, ignoreCase = true) }
        val fMatch = fees.filter { it.invoiceNo.contains(q1, ignoreCase = true) }

        val res1 = DashboardSearchResults(sMatch, stMatch, fMatch)
        assertEquals(2, res1.totalMatches)
        assertEquals(1, res1.matchingStudents.size)
        assertEquals(1, res1.matchingStaff.size)
        assertEquals(0, res1.matchingFees.size)

        // 2. Search "INV-2026" should match fee record
        val q2 = "INV-2026"
        val res2 = DashboardSearchResults(
            matchingFees = fees.filter { it.invoiceNo.contains(q2, ignoreCase = true) }
        )
        assertEquals(1, res2.totalMatches)
        assertFalse(res2.isEmpty)
    }

    @Test
    fun testPdfReportCardGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val student = Student(id = 1, admissionNo = "ADM-101", name = "Sophia Chen", grade = "Grade 10", section = "A", rollNo = "105", guardianName = "David", phone = "555-01", email = "sophia@school.edu", monthlyFee = 450.0, admissionDate = "2026-09-01")
        val exam = Exam(id = 1, name = "Midterm Examination 2026", academicYear = "2025-2026", term = "Midterm", startDate = "2026-10-10")

        val detail1 = SubjectResultDetail(
            resultId = 1,
            subjectId = 1,
            subjectName = "Mathematics",
            subjectCode = "MTH-101",
            marksObtained = 92.0,
            maxMarks = 100.0,
            passMarks = 40.0,
            gradeLetter = "A+",
            gradePoint = 4.0,
            remarks = "Excellent"
        )
        val detail2 = SubjectResultDetail(
            resultId = 2,
            subjectId = 2,
            subjectName = "Physics",
            subjectCode = "PHY-101",
            marksObtained = 88.0,
            maxMarks = 100.0,
            passMarks = 40.0,
            gradeLetter = "A",
            gradePoint = 3.8,
            remarks = "Good work"
        )

        val reportCard = StudentReportCard(
            exam = exam,
            student = student,
            subjectResults = listOf(detail1, detail2),
            totalMarksObtained = 180.0,
            totalMaxMarks = 200.0,
            overallPercentage = 90.0,
            gpa = 3.9,
            overallGrade = "A+",
            passedCount = 2,
            failedCount = 0,
            attendancePercentage = 96.0,
            classRank = 1,
            totalStudentsInClass = 25,
            generalRemarks = "Outstanding academic dedication."
        )

        val file = PdfReportCardUtils.generateReportCardPdf(context, reportCard)
        assertNotNull(file)
        assertTrue(file?.exists() == true)
        assertTrue(file?.length() ?: 0 > 0)
        assertTrue(file?.name?.endsWith(".pdf") == true)
    }

    @Test
    fun testNotificationChannelsAndAlertTrigger() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.createNotificationChannels(context)

        val students = listOf(
            Student(id = 1, admissionNo = "ADM-101", name = "Sophia Chen", grade = "Grade 10", section = "A", rollNo = "105", guardianName = "David", phone = "555-01", email = "sophia@school.edu", monthlyFee = 450.0, admissionDate = "2026-09-01", status = "Active")
        )
        val fees = listOf(
            FeeRecord(id = 1, studentId = 1, invoiceNo = "INV-2026-001", monthYear = "October 2026", dueDate = "2026-10-15", tuitionFee = 450.0, totalAmount = 450.0, paidAmount = 0.0, paymentStatus = "Pending")
        )
        val attendance = listOf<StudentAttendance>() // Missing attendance

        val message = NotificationHelper.checkAndTriggerAdminAlerts(
            context = context,
            students = students,
            feeRecords = fees,
            attendanceList = attendance,
            dateStr = "2026-10-01"
        )

        assertTrue(message.contains("alert"))
    }
}
