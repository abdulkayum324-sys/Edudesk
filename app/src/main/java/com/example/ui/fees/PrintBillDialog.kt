package com.example.ui.fees

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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils
import com.example.util.PrintManagerHelper

@Composable
fun PrintBillDialog(
    student: Student,
    invoice: FeeRecord,
    schoolName: String = "Oakridge International Academy",
    schoolAddress: String = "Main Academic Campus, Kathmandu, Nepal",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val html = PrintManagerHelper.generateBillHtml(student, invoice, schoolName, schoolAddress)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Official Student Fee Bill",
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
                // Bill Document Representation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Text(
                            text = schoolName.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Student Accounts & Bursar Office \u2022 Invoice #${invoice.invoiceNo}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Student Information
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Student: ${student.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Adm: ${student.admissionNo} \u2022 Roll: ${student.rollNo}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Grade: ${student.grade}-${student.section}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(status = invoice.paymentStatus)
                                Text("Due: ${invoice.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Month: ${invoice.monthYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Line Items
                        BillItemRow(title = "Monthly Tuition Fee", amount = invoice.tuitionFee)
                        if (invoice.libraryFee > 0) BillItemRow(title = "Library & Learning Media", amount = invoice.libraryFee)
                        if (invoice.transportFee > 0) BillItemRow(title = "School Transport Service", amount = invoice.transportFee)
                        if (invoice.labOrExamFee > 0) BillItemRow(title = "Science Lab & Examination", amount = invoice.labOrExamFee)
                        if (invoice.otherFee > 0) BillItemRow(title = "Campus Activity / Other", amount = invoice.otherFee)

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Billed:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(FormatUtils.formatCurrency(invoice.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Paid:", fontSize = 12.sp, color = Color(0xFF059669))
                            Text(FormatUtils.formatCurrency(invoice.paidAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (invoice.pendingAmount > 0) Color(0xFFDC2626) else Color(0xFF059669))
                            Text(FormatUtils.formatCurrency(invoice.pendingAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (invoice.pendingAmount > 0) Color(0xFFDC2626) else Color(0xFF059669))
                        }

                        if (invoice.paymentMethod != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Paid via ${invoice.paymentMethod} on ${invoice.paymentDate ?: ""} (Receipt: ${invoice.receiptNo ?: "N/A"})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    PrintManagerHelper.printHtml(context, html, "Fee_Bill_${invoice.invoiceNo}")
                },
                modifier = Modifier.testTag("print_bill_btn")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print Bill")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val shareText = "Oakridge Academy Fee Bill for ${student.name}\nInvoice: ${invoice.invoiceNo}\nMonth: ${invoice.monthYear}\nTotal: ${FormatUtils.formatCurrency(invoice.totalAmount)}\nPaid: ${FormatUtils.formatCurrency(invoice.paidAmount)}\nDue: ${FormatUtils.formatCurrency(invoice.pendingAmount)}\nStatus: ${invoice.paymentStatus}"
                    PrintManagerHelper.shareDocumentText(context, shareText, "Fee Bill ${invoice.invoiceNo}")
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share")
            }
        }
    )
}

@Composable
fun BillItemRow(title: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(FormatUtils.formatCurrency(amount), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
