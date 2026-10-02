package com.example.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentReportCard
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils
import com.example.util.PdfReportCardUtils
import com.example.util.PrintManagerHelper

@Composable
fun StudentReportCardDialog(
    reportCard: StudentReportCard,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val html = PrintManagerHelper.generateReportCardHtml(reportCard)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Official Academic Transcript",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // School Header
                        Text(
                            text = "OAKRIDGE INTERNATIONAL ACADEMY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "${reportCard.exam.name} \u2022 Session: ${reportCard.exam.academicYear}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Student Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Student: ${reportCard.student.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Grade: ${reportCard.student.grade}-${reportCard.student.section} \u2022 Roll #${reportCard.student.rollNo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rank: #${reportCard.classRank} of ${reportCard.totalStudentsInClass}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Text("Attendance: ${reportCard.attendancePercentage.toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject Marks List
                        Text(
                            text = "Subject-Wise Evaluation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        reportCard.subjectResults.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.subjectName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Code: ${sub.subjectCode} \u2022 ${sub.remarks}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "${sub.marksObtained.toInt()}/${sub.maxMarks.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.gradeLetter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sub.isPassed) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Aggregate Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Marks:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("${reportCard.totalMarksObtained.toInt()} / ${reportCard.totalMaxMarks.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Overall Percentage:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("${FormatUtils.formatNumber(reportCard.overallPercentage)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Grade & GPA:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("${reportCard.overallGrade} (${FormatUtils.formatNumber(reportCard.gpa)} GPA)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Teacher Remarks: ${reportCard.generalRemarks}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val pdfFile = PdfReportCardUtils.generateReportCardPdf(context, reportCard)
                        if (pdfFile != null) {
                            PdfReportCardUtils.viewOrSharePdf(context, pdfFile, "Download or Share Official Report Card PDF")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("btn_download_pdf_report_card")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download PDF")
                }

                OutlinedButton(
                    onClick = {
                        PrintManagerHelper.printHtml(
                            context,
                            html,
                            "Report_Card_${reportCard.student.name.replace(" ", "_")}"
                        )
                    },
                    modifier = Modifier.testTag("print_report_card_btn")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
