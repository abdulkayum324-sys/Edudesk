package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.data.model.StudentReportCard

object PrintManagerHelper {

    fun generateBillHtml(
        student: Student,
        invoice: FeeRecord,
        schoolName: String = "Oakridge International Academy",
        schoolAddress: String = "Main Academic Campus, Kathmandu, Nepal"
    ): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Fee Bill - ${invoice.invoiceNo}</title>
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 24px; color: #1e293b; }
                .header { text-align: center; border-bottom: 2px solid #1e3a8a; padding-bottom: 12px; margin-bottom: 20px; }
                .school-title { font-size: 24px; font-weight: bold; color: #1e3a8a; margin: 0; }
                .school-sub { font-size: 13px; color: #64748b; margin-top: 4px; }
                .badge { display: inline-block; padding: 4px 12px; border-radius: 9999px; font-weight: bold; font-size: 12px; }
                .paid { background-color: #d1fae5; color: #065f46; }
                .pending { background-color: #fef3c7; color: #92400e; }
                .partial { background-color: #e0e7ff; color: #3730a3; }
                .details-grid { display: flex; justify-content: space-between; margin-bottom: 24px; font-size: 14px; }
                .details-col { width: 48%; }
                .details-col p { margin: 4px 0; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th, td { border: 1px solid #e2e8f0; padding: 10px 12px; text-align: left; font-size: 13px; }
                th { background-color: #f8fafc; color: #1e293b; }
                .num { text-align: right; }
                .total-row { font-weight: bold; background-color: #f1f5f9; }
                .footer { margin-top: 40px; display: flex; justify-content: space-between; font-size: 12px; color: #64748b; }
                .signature-box { border-top: 1px dashed #94a3b8; width: 180px; text-align: center; padding-top: 8px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="school-title">${schoolName.uppercase()}</div>
                <div class="school-sub">${schoolAddress}</div>
                <h3 style="margin-top: 12px; margin-bottom: 4px; letter-spacing: 1px;">OFFICIAL STUDENT FEE INVOICE</h3>
                <div>Invoice No: <strong>${invoice.invoiceNo}</strong> &bull; Date: <strong>${invoice.dueDate}</strong></div>
            </div>

            <div class="details-grid">
                <div class="details-col">
                    <p><strong>Student Name:</strong> ${student.name}</p>
                    <p><strong>Admission No:</strong> ${student.admissionNo}</p>
                    <p><strong>Grade & Section:</strong> ${student.grade} - Section ${student.section} (Roll: ${student.rollNo})</p>
                    <p><strong>Guardian:</strong> ${student.guardianName}</p>
                </div>
                <div class="details-col" style="text-align: right;">
                    <p><strong>Billing Month:</strong> ${invoice.monthYear}</p>
                    <p><strong>Payment Status:</strong> 
                        <span class="badge ${invoice.paymentStatus.lowercase()}">${invoice.paymentStatus.uppercase()}</span>
                    </p>
                    <p><strong>Payment Method:</strong> ${invoice.paymentMethod ?: "Unpaid"}</p>
                    <p><strong>Receipt Reference:</strong> ${invoice.receiptNo ?: "N/A"}</p>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Fee Description / Account Head</th>
                        <th class="num">Amount (NPR / Rs.)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>1</td>
                        <td>Monthly Tuition Fee (${invoice.monthYear})</td>
                        <td class="num">${FormatUtils.formatNumber(invoice.tuitionFee)}</td>
                    </tr>
                    ${if (invoice.libraryFee > 0) """
                    <tr>
                        <td>2</td>
                        <td>Library & Digital Resource Access</td>
                        <td class="num">${FormatUtils.formatNumber(invoice.libraryFee)}</td>
                    </tr>""" else ""}
                    ${if (invoice.transportFee > 0) """
                    <tr>
                        <td>3</td>
                        <td>Campus Shuttle / Transport Facility</td>
                        <td class="num">${FormatUtils.formatNumber(invoice.transportFee)}</td>
                    </tr>""" else ""}
                    ${if (invoice.labOrExamFee > 0) """
                    <tr>
                        <td>4</td>
                        <td>Science Lab & Examination Assessment</td>
                        <td class="num">${FormatUtils.formatNumber(invoice.labOrExamFee)}</td>
                    </tr>""" else ""}
                    ${if (invoice.otherFee > 0) """
                    <tr>
                        <td>5</td>
                        <td>Miscellaneous / Student Activity Fund</td>
                        <td class="num">${FormatUtils.formatNumber(invoice.otherFee)}</td>
                    </tr>""" else ""}
                    <tr class="total-row">
                        <td colspan="2">TOTAL INVOICE AMOUNT</td>
                        <td class="num">${FormatUtils.formatCurrency(invoice.totalAmount)}</td>
                    </tr>
                    <tr style="color: #059669; font-weight: 600;">
                        <td colspan="2">AMOUNT PAID TO DATE</td>
                        <td class="num">${FormatUtils.formatCurrency(invoice.paidAmount)}</td>
                    </tr>
                    <tr style="color: #dc2626; font-weight: bold; font-size: 14px; background-color: #fef2f2;">
                        <td colspan="2">OUTSTANDING BALANCE DUE</td>
                        <td class="num">${FormatUtils.formatCurrency(invoice.pendingAmount)}</td>
                    </tr>
                </tbody>
            </table>

            <p style="font-size: 12px; color: #64748b;"><strong>Remarks:</strong> ${invoice.remarks ?: "Thank you for your prompt settlement."}</p>

            <div class="footer">
                <div>
                    Generated electronically by EduDesk School ERP.<br>
                    Keep this receipt for academic verification and tax deductions.
                </div>
                <div class="signature-box">
                    Authorized Bursar / Accounts Officer
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateReportCardHtml(
        card: StudentReportCard,
        schoolName: String = "Oakridge International Academy",
        schoolAddress: String = "Main Academic Campus, Kathmandu, Nepal"
    ): String {
        val rows = card.subjectResults.joinToString("") { s ->
            val color = if (s.isPassed) "#059669" else "#dc2626"
            """
            <tr>
                <td><strong>${s.subjectName}</strong> (${s.subjectCode})</td>
                <td style="text-align: center;">${FormatUtils.formatNumber(s.maxMarks)}</td>
                <td style="text-align: center;">${FormatUtils.formatNumber(s.passMarks)}</td>
                <td style="text-align: center; font-weight: bold;">${FormatUtils.formatNumber(s.marksObtained)}</td>
                <td style="text-align: center; color: $color; font-weight: bold;">${s.gradeLetter}</td>
                <td style="text-align: center;">${FormatUtils.formatNumber(s.gradePoint)}</td>
                <td>${s.remarks}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Academic Transcript - ${card.student.name}</title>
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 24px; color: #0f172a; }
                .header { text-align: center; border-bottom: 3px double #1e3a8a; padding-bottom: 14px; margin-bottom: 20px; }
                .school-title { font-size: 26px; font-weight: 800; color: #1e3a8a; }
                .school-address { font-size: 13px; color: #64748b; margin-top: 4px; }
                .card-title { font-size: 18px; font-weight: bold; margin-top: 8px; color: #334155; }
                .student-info { display: flex; justify-content: space-between; margin-bottom: 20px; font-size: 14px; line-height: 1.6; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
                th, td { border: 1px solid #cbd5e1; padding: 8px 10px; font-size: 13px; }
                th { background-color: #1e3a8a; color: white; }
                .summary-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px; margin-bottom: 24px; }
                .summary-row { display: flex; justify-content: space-around; text-align: center; }
                .summary-item .val { font-size: 20px; font-weight: bold; color: #1e3a8a; }
                .summary-item .lbl { font-size: 12px; color: #64748b; }
                .footer { margin-top: 50px; display: flex; justify-content: space-between; font-size: 12px; }
                .sign-box { border-top: 1px solid #64748b; width: 180px; text-align: center; padding-top: 8px; }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="school-title">${schoolName.uppercase()}</div>
                <div class="school-address">${schoolAddress}</div>
                <div class="card-title">OFFICIAL ACADEMIC EVALUATION & TRANSCRIPT</div>
                <div>${card.exam.name} &bull; Academic Session: ${card.exam.academicYear}</div>
            </div>

            <div class="student-info">
                <div>
                    <div><strong>Student Name:</strong> ${card.student.name}</div>
                    <div><strong>Roll No:</strong> ${card.student.rollNo} &bull; <strong>Admission No:</strong> ${card.student.admissionNo}</div>
                    <div><strong>Grade & Section:</strong> ${card.student.grade} - ${card.student.section}</div>
                </div>
                <div style="text-align: right;">
                    <div><strong>Guardian:</strong> ${card.student.guardianName}</div>
                    <div><strong>Class Rank:</strong> #${card.classRank} of ${card.totalStudentsInClass}</div>
                    <div><strong>Attendance:</strong> ${FormatUtils.formatNumber(card.attendancePercentage)}%</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th style="text-align: center;">Max</th>
                        <th style="text-align: center;">Pass</th>
                        <th style="text-align: center;">Marks</th>
                        <th style="text-align: center;">Grade</th>
                        <th style="text-align: center;">Point</th>
                        <th>Instructor Remarks</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>

            <div class="summary-card">
                <div class="summary-row">
                    <div class="summary-item">
                        <div class="val">${FormatUtils.formatNumber(card.totalMarksObtained)} / ${FormatUtils.formatNumber(card.totalMaxMarks)}</div>
                        <div class="lbl">Total Marks</div>
                    </div>
                    <div class="summary-item">
                        <div class="val">${FormatUtils.formatNumber(card.overallPercentage)}%</div>
                        <div class="lbl">Percentage</div>
                    </div>
                    <div class="summary-item">
                        <div class="val">${card.overallGrade}</div>
                        <div class="lbl">Final Grade</div>
                    </div>
                    <div class="summary-item">
                        <div class="val">${FormatUtils.formatNumber(card.gpa)}</div>
                        <div class="lbl">Cumulative GPA</div>
                    </div>
                    <div class="summary-item">
                        <div class="val" style="color: #059669;">${card.passedCount} Pass</div>
                        <div class="lbl">Passed Subjects</div>
                    </div>
                </div>
            </div>

            <div style="font-size: 13px; margin-bottom: 24px;">
                <strong>Faculty Assessment & Remarks:</strong> ${card.generalRemarks}
            </div>

            <div class="footer">
                <div class="sign-box">Class Teacher</div>
                <div class="sign-box">Academic Dean</div>
                <div class="sign-box">Principal's Official Seal</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printHtml(context: Context, htmlContent: String, jobName: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        if (printManager != null) {
                            val printAdapter = webView.createPrintDocumentAdapter(jobName)
                            printManager.print(
                                jobName,
                                printAdapter,
                                PrintAttributes.Builder().build()
                            )
                        }
                    }
                }
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
            } catch (e: Exception) {
                shareDocumentText(context, htmlContent, jobName)
            }
        }
    }

    fun shareDocumentText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
