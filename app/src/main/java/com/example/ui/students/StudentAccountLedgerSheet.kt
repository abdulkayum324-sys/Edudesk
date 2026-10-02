package com.example.ui.students

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OnDangerContainer
import com.example.ui.theme.OnSuccessContainer
import com.example.ui.theme.SuccessContainer
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAccountLedgerSheet(
    student: Student,
    feeHistory: List<FeeRecord>,
    onDismiss: () -> Unit,
    onEditStudent: () -> Unit,
    onDeleteStudent: () -> Unit,
    onCreateFeeBill: () -> Unit,
    onCollectPayment: (FeeRecord) -> Unit,
    onPrintBill: (FeeRecord) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalBilled = feeHistory.sumOf { it.totalAmount }
    val totalPaid = feeHistory.sumOf { it.paidAmount }
    val totalPending = feeHistory.sumOf { it.pendingAmount }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student Profile Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${student.grade} - Section ${student.section} \u2022 Roll #${student.rollNo}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Adm No: ${student.admissionNo} \u2022 Admitted: ${student.admissionDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onEditStudent) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary)
                    }
                    IconButton(onClick = onDeleteStudent) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                    }
                }
            }

            // Contact & Guardian card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardian: ${student.guardianName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phone: ${student.phone}", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Email: ${student.email}", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Student Account Ledger Balance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Invoiced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(totalBilled), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(totalPaid), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Balance Due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(totalPending), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (totalPending > 0) Color(0xFFDC2626) else Color(0xFF059669))
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCreateFeeBill,
                        modifier = Modifier.weight(1f).testTag("student_new_bill_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Fee Bill", fontSize = 13.sp)
                    }
                }
            }

            // Account Ledger History
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Fee Ledger (${feeHistory.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (feeHistory.isEmpty()) {
                item {
                    Text(
                        text = "No fee records generated yet for this student.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(feeHistory) { invoice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(invoice.invoiceNo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${invoice.monthYear} \u2022 Due: ${invoice.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(status = invoice.paymentStatus)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total: ${FormatUtils.formatCurrency(invoice.totalAmount)} | Paid: ${FormatUtils.formatCurrency(invoice.paidAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (invoice.pendingAmount > 0) {
                                        Text(
                                            text = "Due: ${FormatUtils.formatCurrency(invoice.pendingAmount)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (invoice.pendingAmount > 0) {
                                        OutlinedButton(
                                            onClick = { onCollectPayment(invoice) },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Collect", fontSize = 11.sp)
                                        }
                                    }
                                    IconButton(
                                        onClick = { onPrintBill(invoice) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "Print Bill", tint = NavyPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
