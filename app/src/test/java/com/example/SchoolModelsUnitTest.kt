package com.example

import com.example.data.model.FeeRecord
import com.example.data.model.Staff
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolModelsUnitTest {

    @Test
    fun testGradeCalculation() {
        val (gradeAplus, gpaAplus) = FormatUtils.calculateGrade(94.0)
        assertEquals("A+", gradeAplus)
        assertEquals(4.0, gpaAplus, 0.01)

        val (gradeB, gpaB) = FormatUtils.calculateGrade(65.0)
        assertEquals("B", gradeB)
        assertEquals(3.0, gpaB, 0.01)

        val (gradeF, gpaF) = FormatUtils.calculateGrade(35.0)
        assertEquals("F", gradeF)
        assertEquals(0.0, gpaF, 0.01)
    }

    @Test
    fun testStaffNetSalaryCalculation() {
        val staff = Staff(
            id = 1,
            employeeId = "EMP-001",
            name = "Sarah Connor",
            role = "Teacher",
            department = "Science",
            email = "sarah@school.edu",
            phone = "555-1234",
            address = "Campus Road",
            joiningDate = "2023-01-01",
            subjectsTaught = "Biology",
            assignedClasses = "Grade 10",
            baseSalary = 4000.0,
            allowances = 500.0,
            deductions = 200.0
        )
        assertEquals(4300.0, staff.netSalary, 0.01)
    }

    @Test
    fun testFeePendingCalculation() {
        val fee = FeeRecord(
            id = 1,
            studentId = 1,
            invoiceNo = "INV-001",
            monthYear = "September 2026",
            dueDate = "2026-09-15",
            tuitionFee = 450.0,
            libraryFee = 50.0,
            totalAmount = 500.0,
            paidAmount = 300.0,
            paymentStatus = "Partial"
        )
        assertEquals(200.0, fee.pendingAmount, 0.01)
    }

    @Test
    fun testDashboardSummaryMetrics() {
        val summary = com.example.ui.DashboardSummary(
            totalStudents = 120,
            activeStudentsCount = 118,
            totalStaff = 15,
            activeStaffCount = 14,
            onLeaveStaffCount = 1,
            totalCollectedFees = 48500.0,
            totalPendingFees = 6200.0,
            pendingFeeAccountsCount = 12,
            pendingLeavesCount = 2
        )
        assertEquals(120, summary.totalStudents)
        assertEquals(118, summary.activeStudentsCount)
        assertEquals(14, summary.activeStaffCount)
        assertEquals(6200.0, summary.totalPendingFees, 0.01)
        assertEquals(12, summary.pendingFeeAccountsCount)
    }

    @Test
    fun testNepaliRupeesCurrencyFormatting() {
        val formatted = FormatUtils.formatCurrency(4500.0)
        assertEquals("Rs. 4,500.00", formatted)

        val devanagari = FormatUtils.formatNepaliRupees(4500.0, useDevanagari = true)
        assertEquals("रू 4,500.00", devanagari)

        val zero = FormatUtils.formatCurrency(0.0)
        assertEquals("Rs. 0.00", zero)
    }
}
