package com.example

import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.util.CsvExportUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportUnitTest {

    @Test
    fun testEscapeCsvCell() {
        assertEquals("NormalText", CsvExportUtils.escapeCsvCell("NormalText"))
        assertEquals("\"Contains,Comma\"", CsvExportUtils.escapeCsvCell("Contains,Comma"))
        assertEquals("\"Contains\"\"Quotes\"\"\"", CsvExportUtils.escapeCsvCell("Contains\"Quotes\""))
        assertEquals("\"Line1\nLine2\"", CsvExportUtils.escapeCsvCell("Line1\nLine2"))
        assertEquals("", CsvExportUtils.escapeCsvCell(null))
    }

    @Test
    fun testGenerateStudentsCsv() {
        val students = listOf(
            Student(
                id = 1,
                admissionNo = "ADM-2026-001",
                name = "Emma Watson, Jr.",
                grade = "Grade 10",
                section = "A",
                rollNo = "101",
                guardianName = "Chris Watson",
                phone = "+1-555-0199",
                email = "emma@school.edu",
                address = "124 Park Ave, Suite 3",
                monthlyFee = 450.0,
                admissionDate = "2024-08-15",
                status = "Active"
            )
        )

        val csv = CsvExportUtils.generateStudentsCsv(students)
        assertTrue(csv.contains("Student ID,Admission No,Full Name"))
        assertTrue(csv.contains("ADM-2026-001"))
        assertTrue(csv.contains("\"Emma Watson, Jr.\""))
        assertTrue(csv.contains("450.00"))
        assertTrue(csv.contains("Active"))
    }

    @Test
    fun testGenerateFeeTransactionsCsv() {
        val student = Student(
            id = 1,
            admissionNo = "ADM-2026-001",
            name = "Emma Watson",
            grade = "Grade 10",
            section = "A",
            rollNo = "101",
            guardianName = "Chris Watson",
            phone = "+1-555-0199",
            email = "emma@school.edu",
            monthlyFee = 450.0,
            admissionDate = "2024-08-15"
        )
        val studentsMap = mapOf(1L to student)

        val fees = listOf(
            FeeRecord(
                id = 10,
                studentId = 1,
                invoiceNo = "INV-2026-001",
                receiptNo = "REC-55441",
                monthYear = "October 2026",
                dueDate = "2026-10-15",
                paymentDate = "2026-10-02",
                paymentMethod = "Online/UPI",
                tuitionFee = 450.0,
                libraryFee = 30.0,
                totalAmount = 480.0,
                paidAmount = 480.0,
                paymentStatus = "Paid",
                remarks = "Paid via parent portal"
            )
        )

        val csv = CsvExportUtils.generateFeeTransactionsCsv(fees, studentsMap)
        assertTrue(csv.contains("Invoice ID,Invoice No,Receipt No,Admission No"))
        assertTrue(csv.contains("INV-2026-001"))
        assertTrue(csv.contains("REC-55441"))
        assertTrue(csv.contains("ADM-2026-001"))
        assertTrue(csv.contains("Emma Watson"))
        assertTrue(csv.contains("Online/UPI"))
        assertTrue(csv.contains("480.00"))
        assertTrue(csv.contains("Paid"))
    }
}
