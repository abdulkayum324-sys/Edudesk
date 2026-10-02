package com.example.ui.fees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordTransactionDialog(
    students: List<Student>,
    invoices: List<FeeRecord>,
    preSelectedStudent: Student? = null,
    preSelectedInvoice: FeeRecord? = null,
    onDismiss: () -> Unit,
    onConfirmTransaction: (invoiceId: Long, amount: Double, method: String, remarks: String?, transactionDate: String) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(preSelectedStudent ?: students.firstOrNull())
    }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    // Filter invoices for the chosen student
    val studentInvoices = remember(selectedStudent, invoices) {
        val stId = selectedStudent?.id ?: 0L
        invoices.filter { it.studentId == stId }
    }

    var selectedInvoice by remember(selectedStudent) {
        mutableStateOf(
            preSelectedInvoice?.takeIf { it.studentId == selectedStudent?.id }
                ?: studentInvoices.firstOrNull { it.pendingAmount > 0 }
                ?: studentInvoices.firstOrNull()
        )
    }
    var invoiceDropdownExpanded by remember { mutableStateOf(false) }

    var amountStr by remember(selectedInvoice) {
        mutableStateOf(selectedInvoice?.let { if (it.pendingAmount > 0) it.pendingAmount.toString() else it.totalAmount.toString() } ?: "450.0")
    }

    var selectedMethod by remember { mutableStateOf("Cash") }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cash", "Online/UPI", "Bank Transfer", "Cheque", "Credit Card")

    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var transactionDate by remember { mutableStateOf(currentDateStr) }
    var receiptRef by remember {
        mutableStateOf("REC-${(10000..99999).random()}")
    }
    var remarks by remember { mutableStateOf("Tuition fee collected at bursar desk") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Record Fee Payment Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Step 1: Select Student Account
                Text("1. Student Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { studentDropdownExpanded = true }
                        .testTag("transaction_student_selector"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedStudent?.let { "${it.name} (${it.grade}-${it.section})" } ?: "Select Student",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = studentDropdownExpanded,
                    onDismissRequest = { studentDropdownExpanded = false }
                ) {
                    students.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} - ${s.grade} (Roll: ${s.rollNo})") },
                            onClick = {
                                selectedStudent = s
                                studentDropdownExpanded = false
                            }
                        )
                    }
                }

                // Step 2: Select Invoice / Billing Month
                Text("2. Fee Invoice / Billing Assessment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { invoiceDropdownExpanded = true }
                        .testTag("transaction_invoice_selector"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedInvoice?.let { "${it.monthYear} (${it.invoiceNo})" } ?: "No invoices available",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            selectedInvoice?.let {
                                Text(
                                    text = "Total: ${FormatUtils.formatCurrency(it.totalAmount)} | Due: ${FormatUtils.formatCurrency(it.pendingAmount)}",
                                    fontSize = 11.sp,
                                    color = if (it.pendingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = invoiceDropdownExpanded,
                    onDismissRequest = { invoiceDropdownExpanded = false }
                ) {
                    if (studentInvoices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No invoice records found for this student") },
                            onClick = { invoiceDropdownExpanded = false }
                        )
                    } else {
                        studentInvoices.forEach { inv ->
                            DropdownMenuItem(
                                text = { Text("${inv.monthYear} - ${inv.invoiceNo} (Due: ${FormatUtils.formatCurrency(inv.pendingAmount)})") },
                                onClick = {
                                    selectedInvoice = inv
                                    amountStr = if (inv.pendingAmount > 0) inv.pendingAmount.toString() else inv.totalAmount.toString()
                                    invoiceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Step 3: Transaction Amount & Payment Method
                Text("3. Transaction Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount Paid ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("transaction_amount_input")
                    )

                    // Payment Method
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Payment Method", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { methodDropdownExpanded = true }
                                .testTag("transaction_method_selector"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedMethod, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = methodDropdownExpanded,
                            onDismissRequest = { methodDropdownExpanded = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        selectedMethod = method
                                        methodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = transactionDate,
                        onValueChange = { transactionDate = it },
                        label = { Text("Transaction Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = receiptRef,
                        onValueChange = { receiptRef = it },
                        label = { Text("Receipt / Ref #") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("transaction_receipt_input")
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Cashier Remarks / Transaction Memo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val invoice = selectedInvoice
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (invoice != null && amount > 0) {
                        val fullRemarks = "$remarks (Ref: $receiptRef)"
                        onConfirmTransaction(
                            invoice.id,
                            amount,
                            selectedMethod,
                            fullRemarks,
                            transactionDate.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_record_transaction_button")
            ) {
                Text("Confirm Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
