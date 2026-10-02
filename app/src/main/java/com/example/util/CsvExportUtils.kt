package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import java.io.File
import java.io.FileWriter

object CsvExportUtils {

    /**
     * Escape a cell according to standard RFC 4180 CSV specifications.
     */
    fun escapeCsvCell(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    /**
     * Generate CSV for all student records.
     */
    fun generateStudentsCsv(students: List<Student>): String {
        val sb = StringBuilder()
        // Header
        sb.append(
            listOf(
                "Student ID",
                "Admission No",
                "Full Name",
                "Grade",
                "Section",
                "Roll No",
                "Guardian Name",
                "Contact Phone",
                "Email Address",
                "Residential Address",
                "Monthly Fee (NPR / Rs.)",
                "Admission Date",
                "Enrollment Status"
            ).joinToString(",") { escapeCsvCell(it) }
        ).append("\r\n")

        // Rows
        for (s in students) {
            val row = listOf(
                s.id,
                s.admissionNo,
                s.name,
                s.grade,
                s.section,
                s.rollNo,
                s.guardianName,
                s.phone,
                s.email,
                s.address,
                String.format(java.util.Locale.US, "%.2f", s.monthlyFee),
                s.admissionDate,
                s.status
            )
            sb.append(row.joinToString(",") { escapeCsvCell(it) }).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Generate CSV for fee invoices and payment transactions.
     */
    fun generateFeeTransactionsCsv(
        feeRecords: List<FeeRecord>,
        studentsMap: Map<Long, Student>
    ): String {
        val sb = StringBuilder()
        // Header
        sb.append(
            listOf(
                "Invoice ID",
                "Invoice No",
                "Receipt No",
                "Admission No",
                "Student Name",
                "Grade",
                "Month/Year",
                "Due Date",
                "Payment Date",
                "Payment Method",
                "Tuition Fee (NPR)",
                "Library Fee (NPR)",
                "Transport Fee (NPR)",
                "Exam/Lab Fee (NPR)",
                "Total Billed (NPR)",
                "Amount Paid (NPR)",
                "Pending Due (NPR)",
                "Payment Status",
                "Remarks"
            ).joinToString(",") { escapeCsvCell(it) }
        ).append("\r\n")

        for (f in feeRecords) {
            val student = studentsMap[f.studentId]
            val row = listOf(
                f.id,
                f.invoiceNo,
                f.receiptNo ?: "",
                student?.admissionNo ?: "",
                student?.name ?: "Student #${f.studentId}",
                student?.grade ?: "",
                f.monthYear,
                f.dueDate,
                f.paymentDate ?: "",
                f.paymentMethod ?: "",
                String.format(java.util.Locale.US, "%.2f", f.tuitionFee),
                String.format(java.util.Locale.US, "%.2f", f.libraryFee),
                String.format(java.util.Locale.US, "%.2f", f.transportFee),
                String.format(java.util.Locale.US, "%.2f", f.labOrExamFee),
                String.format(java.util.Locale.US, "%.2f", f.totalAmount),
                String.format(java.util.Locale.US, "%.2f", f.paidAmount),
                String.format(java.util.Locale.US, "%.2f", f.pendingAmount),
                f.paymentStatus,
                f.remarks ?: ""
            )
            sb.append(row.joinToString(",") { escapeCsvCell(it) }).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Share or export CSV file using Android's native share sheet.
     */
    fun shareCsvFile(
        context: Context,
        fileName: String,
        csvContent: String,
        chooserTitle: String = "Export School Data (CSV)"
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            FileWriter(file).use { it.write(csvContent) }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, "Exported school accounts backup: $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copy CSV content to system clipboard.
     */
    fun copyToClipboard(context: Context, label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard.", Toast.LENGTH_SHORT).show()
    }
}
