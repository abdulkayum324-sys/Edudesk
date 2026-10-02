package com.example.ui.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.SchoolMainViewModel
import com.example.ui.components.CsvBackupExportDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesBillingScreen(
    viewModel: SchoolMainViewModel,
    onOpenCreateInvoice: () -> Unit,
    onOpenCollectFee: (FeeRecord) -> Unit,
    onPrintBill: (Student, FeeRecord) -> Unit
) {
    // 0: Fee Invoices & Dues, 1: Payment Transactions Ledger
    var selectedFeeTab by remember { mutableIntStateOf(0) }

    val feeRecords by viewModel.filteredFees.collectAsState()
    val allFeeRecords by viewModel.feeRecords.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedFilter by viewModel.feeStatusFilter.collectAsState()

    var showBatchDialog by remember { mutableStateOf(false) }
    var showRecordTransactionDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var transactionFilterMethod by remember { mutableStateOf("All") }

    val invoiceFilterOptions = listOf("All", "Pending", "Partial", "Paid")
    val paymentMethodsFilter = listOf("All", "Cash", "Online/UPI", "Bank Transfer", "Cheque")

    val studentsMap = remember(students) { students.associateBy { it.id } }

    val totalBilled = allFeeRecords.sumOf { it.totalAmount }
    val totalCollected = allFeeRecords.sumOf { it.paidAmount }
    val totalOutstanding = allFeeRecords.sumOf { it.pendingAmount }

    // List of payment transactions (all fee records where paidAmount > 0)
    val transactions = remember(allFeeRecords, transactionFilterMethod) {
        val list = allFeeRecords.filter { it.paidAmount > 0 }
        if (transactionFilterMethod == "All") {
            list
        } else {
            list.filter { it.paymentMethod.equals(transactionFilterMethod, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRecordTransactionDialog = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_record_transaction")
            ) {
                Icon(Icons.Default.Payment, contentDescription = "Record Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Navigation Tabs: Invoices vs Payment Transactions
            TabRow(
                selectedTabIndex = selectedFeeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedFeeTab == 0,
                    onClick = { selectedFeeTab = 0 },
                    text = { Text("Fee Invoices & Dues", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_fee_invoices")
                )
                Tab(
                    selected = selectedFeeTab == 1,
                    onClick = { selectedFeeTab = 1 },
                    text = {
                        Text(
                            "Payments Ledger (${allFeeRecords.count { it.paidAmount > 0 }})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_payment_transactions")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financial Summary KPI Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "School Accounts & Billing Overview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatCurrency(totalBilled), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Collected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatCurrency(totalCollected), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatCurrency(totalOutstanding), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showRecordTransactionDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("btn_record_new_transaction"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Payment", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenCreateInvoice,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_new_invoice")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Invoice", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showBatchDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_batch_bills")
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Batch Bills", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showExportCsvDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_fees_csv")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedFeeTab == 0) {
                // ================= TAB 0: FEE INVOICES & DUES =================
                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(invoiceFilterOptions) { filter ->
                        FilterChip(
                            selected = selectedFilter.equals(filter, ignoreCase = true),
                            onClick = { viewModel.setFeeStatusFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Invoices & Billing Assessments (${feeRecords.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (feeRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No fee invoices match filter '$selectedFilter'.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(feeRecords) { invoice ->
                            val student = studentsMap[invoice.studentId]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fee_card_${invoice.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student?.name ?: "Student #${invoice.studentId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "${student?.grade ?: ""} \u2022 Invoice #${invoice.invoiceNo}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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
                                                text = "Month: ${invoice.monthYear} \u2022 Due: ${invoice.dueDate}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text(
                                                    text = "Total: ${FormatUtils.formatCurrency(invoice.totalAmount)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Paid: ${FormatUtils.formatCurrency(invoice.paidAmount)}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF059669)
                                                )
                                                if (invoice.pendingAmount > 0) {
                                                    Text(
                                                        text = "Due: ${FormatUtils.formatCurrency(invoice.pendingAmount)}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFDC2626)
                                                    )
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (invoice.pendingAmount > 0) {
                                                Button(
                                                    onClick = { onOpenCollectFee(invoice) },
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .testTag("collect_fee_btn_${invoice.id}"),
                                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                                ) {
                                                    Text("Collect", fontSize = 11.sp)
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    student?.let { onPrintBill(it, invoice) }
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = "Print", tint = NavyPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ================= TAB 1: PAYMENT TRANSACTIONS LEDGER =================
                // Method Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(paymentMethodsFilter) { method ->
                        FilterChip(
                            selected = transactionFilterMethod == method,
                            onClick = { transactionFilterMethod = method },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Recorded Transactions (${transactions.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No payment transactions found for '$transactionFilterMethod'.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(transactions) { invoice ->
                            val student = studentsMap[invoice.studentId]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transaction_card_${invoice.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD1FAE5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Payment,
                                                contentDescription = null,
                                                tint = Color(0xFF065F46),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student?.name ?: "Student #${invoice.studentId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Receipt: ${invoice.receiptNo ?: "REC-${invoice.id}"} \u2022 ${invoice.paymentMethod ?: "Cash"}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "+ ${FormatUtils.formatCurrency(invoice.paidAmount)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF059669)
                                            )
                                            Text(
                                                text = invoice.paymentDate ?: "Recently",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Invoice: ${invoice.invoiceNo} (${invoice.monthYear})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        IconButton(
                                            onClick = {
                                                student?.let { onPrintBill(it, invoice) }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Print,
                                                contentDescription = "Print Receipt",
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (!invoice.remarks.isNullOrBlank()) {
                                        Text(
                                            text = "Note: ${invoice.remarks}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Transaction Dialog Form
    if (showRecordTransactionDialog) {
        RecordTransactionDialog(
            students = students,
            invoices = allFeeRecords,
            onDismiss = { showRecordTransactionDialog = false },
            onConfirmTransaction = { invoiceId, amount, method, remarks, date ->
                viewModel.recordFeePayment(invoiceId, amount, method, remarks)
                showRecordTransactionDialog = false
            }
        )
    }

    // Batch Billing Dialog
    if (showBatchDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text("Generate Monthly Batch Bills", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will automatically generate monthly tuition and facility fee invoices for all active students for November 2026."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.generateBatchMonthlyBills("November 2026", "2026-11-15")
                        showBatchDialog = false
                    },
                    modifier = Modifier.testTag("confirm_batch_bills_button")
                ) {
                    Text("Generate Batch Invoices")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBatchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CSV Backup Export Dialog
    if (showExportCsvDialog) {
        CsvBackupExportDialog(
            students = students,
            feeRecords = allFeeRecords,
            initialTab = 1,
            onDismiss = { showExportCsvDialog = false }
        )
    }
}
