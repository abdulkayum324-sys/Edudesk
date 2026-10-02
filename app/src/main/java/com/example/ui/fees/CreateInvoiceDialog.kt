package com.example.ui.fees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
fun CreateInvoiceDialog(
    students: List<Student>,
    preSelectedStudent: Student? = null,
    onDismiss: () -> Unit,
    onConfirm: (FeeRecord) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(preSelectedStudent ?: students.firstOrNull())
    }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    var monthYear by remember { mutableStateOf("October 2026") }
    var dueDate by remember { mutableStateOf("2026-10-15") }
    var tuitionFeeStr by remember {
        mutableStateOf(selectedStudent?.monthlyFee?.toString() ?: "450.0")
    }
    var libraryFeeStr by remember { mutableStateOf("30.0") }
    var transportFeeStr by remember { mutableStateOf("50.0") }
    var labFeeStr by remember { mutableStateOf("40.0") }
    var otherFeeStr by remember { mutableStateOf("0.0") }

    val tuition = tuitionFeeStr.toDoubleOrNull() ?: 0.0
    val library = libraryFeeStr.toDoubleOrNull() ?: 0.0
    val transport = transportFeeStr.toDoubleOrNull() ?: 0.0
    val lab = labFeeStr.toDoubleOrNull() ?: 0.0
    val other = otherFeeStr.toDoubleOrNull() ?: 0.0
    val total = tuition + library + transport + lab + other

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Generate Student Fee Invoice",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student Picker
                Text("Select Student Account", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { studentDropdownExpanded = true },
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
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
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
                                tuitionFeeStr = s.monthlyFee.toString()
                                studentDropdownExpanded = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = monthYear,
                        onValueChange = { monthYear = it },
                        label = { Text("Billing Month") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Fee Components Breakdown", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tuitionFeeStr,
                        onValueChange = { tuitionFeeStr = it },
                        label = { Text("Tuition (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = libraryFeeStr,
                        onValueChange = { libraryFeeStr = it },
                        label = { Text("Library (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = transportFeeStr,
                        onValueChange = { transportFeeStr = it },
                        label = { Text("Transport (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = labFeeStr,
                        onValueChange = { labFeeStr = it },
                        label = { Text("Lab/Exam (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = otherFeeStr,
                    onValueChange = { otherFeeStr = it },
                    label = { Text("Miscellaneous / Other (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Total Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Invoice Amount:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(FormatUtils.formatCurrency(total), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val student = selectedStudent
                    if (student != null) {
                        val invoiceNo = "INV-${monthYear.take(3).uppercase()}-${(1000..9999).random()}"
                        val invoice = FeeRecord(
                            studentId = student.id,
                            invoiceNo = invoiceNo,
                            monthYear = monthYear.trim(),
                            dueDate = dueDate.trim(),
                            tuitionFee = tuition,
                            libraryFee = library,
                            transportFee = transport,
                            labOrExamFee = lab,
                            otherFee = other,
                            totalAmount = total,
                            paidAmount = 0.0,
                            paymentStatus = "Pending",
                            remarks = "Individual student fee assessment"
                        )
                        onConfirm(invoice)
                    }
                },
                modifier = Modifier.testTag("save_invoice_button")
            ) {
                Text("Generate Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
