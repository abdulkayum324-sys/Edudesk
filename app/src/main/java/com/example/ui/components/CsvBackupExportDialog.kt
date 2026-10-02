package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.ui.theme.NavyPrimary
import com.example.util.CsvExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CsvBackupExportDialog(
    students: List<Student>,
    feeRecords: List<FeeRecord>,
    initialTab: Int = 0, // 0: Students, 1: Fee Transactions
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedExportType by remember { mutableIntStateOf(initialTab) } // 0: Students, 1: Fee Transactions, 2: Complete
    var showPreview by remember { mutableStateOf(false) }

    val studentsMap = remember(students) { students.associateBy { it.id } }

    val dateStr = remember {
        SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
    }

    val (currentCsvContent, fileName, titleLabel) = remember(selectedExportType, students, feeRecords) {
        when (selectedExportType) {
            0 -> Triple(
                CsvExportUtils.generateStudentsCsv(students),
                "Oakridge_Students_Backup_$dateStr.csv",
                "Student Master Records"
            )
            1 -> Triple(
                CsvExportUtils.generateFeeTransactionsCsv(feeRecords, studentsMap),
                "Oakridge_Fee_Transactions_$dateStr.csv",
                "Fee Billing & Transactions Ledger"
            )
            else -> {
                val combined = "=== STUDENT MASTER RECORDS ===\r\n" +
                        CsvExportUtils.generateStudentsCsv(students) +
                        "\r\n=== FEE INVOICES & PAYMENT TRANSACTIONS ===\r\n" +
                        CsvExportUtils.generateFeeTransactionsCsv(feeRecords, studentsMap)
                Triple(
                    combined,
                    "Oakridge_Full_School_Accounts_Backup_$dateStr.csv",
                    "Complete School Accounts Backup"
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Export External CSV Backup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spreadsheet & Accounting Export",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Data Set to Export:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Export Options Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedExportType == 0,
                        onClick = { selectedExportType = 0 },
                        label = { Text("Students (${students.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("export_type_students")
                    )

                    FilterChip(
                        selected = selectedExportType == 1,
                        onClick = { selectedExportType = 1 },
                        label = { Text("Fee Ledger (${feeRecords.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("export_type_fees")
                    )

                    FilterChip(
                        selected = selectedExportType == 2,
                        onClick = { selectedExportType = 2 },
                        label = { Text("All", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("export_type_all")
                    )
                }

                // Metadata Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Target File: $fileName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Format: RFC 4180 Standard Comma-Separated Values (CSV)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Compatibility: Microsoft Excel, Google Sheets, Apple Numbers, QuickBooks",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons for Sharing & Copying
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            CsvExportUtils.shareCsvFile(
                                context = context,
                                fileName = fileName,
                                csvContent = currentCsvContent,
                                chooserTitle = "Share / Save $titleLabel"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_csv"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share / Save", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            CsvExportUtils.copyToClipboard(
                                context = context,
                                label = titleLabel,
                                content = currentCsvContent
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_csv")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Text", fontSize = 12.sp)
                    }
                }

                // Preview Toggle
                OutlinedButton(
                    onClick = { showPreview = !showPreview },
                    modifier = Modifier.fillMaxWidth().testTag("btn_toggle_csv_preview")
                ) {
                    Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showPreview) "Hide CSV Raw Preview" else "View CSV Raw Preview", fontSize = 12.sp)
                }

                if (showPreview) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = currentCsvContent,
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
