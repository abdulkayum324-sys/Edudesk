package com.example.ui.dashboard

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.ui.AppTab
import com.example.ui.DashboardSearchResults
import com.example.ui.SchoolMainViewModel
import com.example.ui.components.CsvBackupExportDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OnBlueContainer
import com.example.ui.theme.OnDangerContainer
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.OnSuccessContainer
import com.example.ui.theme.SuccessContainer
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: SchoolMainViewModel,
    onNavigateTab: (AppTab) -> Unit,
    onOpenCollectFee: (FeeRecord?) -> Unit,
    onOpenAddStudent: () -> Unit,
    onViewInvoice: (FeeRecord) -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val students by viewModel.students.collectAsState()
    val feeRecords by viewModel.feeRecords.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val searchQuery by viewModel.dashboardSearchQuery.collectAsState()
    val searchResults by viewModel.dashboardSearchResults.collectAsState()
    val context = LocalContext.current
    var showExportCsvDialog by remember { mutableStateOf(false) }

    val totalRevenue = summary.totalCollectedFees + summary.totalPendingFees
    val collectionRate = if (totalRevenue > 0) (summary.totalCollectedFees / totalRevenue).toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Universal Dashboard Search Bar & Admin Notification Bell
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setDashboardSearchQuery(it) },
                        placeholder = { Text("Search students, staff, or fees...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setDashboardSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_search_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.triggerAdminNotificationAlerts(context) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("btn_trigger_admin_alerts")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trigger Admin Alerts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Search Results Dropdown Panel
                if (searchQuery.isNotBlank()) {
                    DashboardSearchResultsPanel(
                        results = searchResults,
                        onSelectStudent = { st ->
                            viewModel.setStudentSearchQuery(st.name)
                            onNavigateTab(AppTab.STUDENTS)
                        },
                        onSelectStaff = {
                            onNavigateTab(AppTab.STAFF)
                        },
                        onSelectFee = { fee ->
                            onViewInvoice(fee)
                        }
                    )
                }
            }
        }

        // Welcome Institutional Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Oakridge Academy Admin",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Academic Session 2025-2026 \u2022 Accounts & ERP",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Term 2",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Collection Progress bar inside banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fee Collection Progress",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${(collectionRate * 100).toInt()}% collected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { collectionRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF34D399),
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }

        // Key Metric Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Primary Row: Total Students & Active Staff Counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Total Students Summary Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTab(AppTab.STUDENTS) }
                            .testTag("summary_card_total_students"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NavyPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = "Total Students",
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Students",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBlueContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${summary.totalStudents}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${summary.activeStudentsCount} Active Enrolled",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnBlueContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 2. Active Staff Counts Summary Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTab(AppTab.STAFF) }
                            .testTag("summary_card_active_staff"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GoldContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OnGoldContainer.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = "Active Staff",
                                        tint = OnGoldContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Staff",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnGoldContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${summary.activeStaffCount}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnGoldContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${summary.totalStaff} Total \u2022 ${summary.onLeaveStaffCount} on leave",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnGoldContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Secondary Row: Pending Fees & Total Collected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Pending Fees Summary Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setFeeStatusFilter("Pending")
                                onNavigateTab(AppTab.BILLING)
                            }
                            .testTag("summary_card_pending_fees"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OnDangerContainer.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Pending Fees",
                                        tint = OnDangerContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pending Fees",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnDangerContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = FormatUtils.formatCurrency(summary.totalPendingFees),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnDangerContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${summary.pendingFeeAccountsCount} Invoices Due",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnDangerContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 4. Fees Collected Summary Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setFeeStatusFilter("Paid")
                                onNavigateTab(AppTab.BILLING)
                            }
                            .testTag("summary_card_collected_fees"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OnSuccessContainer.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = "Collected Fees",
                                        tint = OnSuccessContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Collected Fees",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSuccessContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = FormatUtils.formatCurrency(summary.totalCollectedFees),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSuccessContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Received to date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSuccessContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Pending Staff Leave Notification (if any)
        if (summary.pendingLeavesCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(AppTab.STAFF) }
                        .testTag("pending_leaves_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DangerContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationImportant,
                                contentDescription = "Alert",
                                tint = OnDangerContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Action Required: Staff Leave",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = OnDangerContainer
                            )
                            Text(
                                text = "${summary.pendingLeavesCount} faculty leave requests await review.",
                                fontSize = 12.sp,
                                color = OnDangerContainer.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = "Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )
                    }
                }
            }
        }

        // Interactive Data Visualizations: Daily Attendance Trends & Fee Collections
        item {
            DashboardVisualChartsCard(
                attendanceList = allAttendance,
                feeRecords = feeRecords,
                totalStudentsCount = students.size,
                onNavigateAttendance = { onNavigateTab(AppTab.ATTENDANCE) },
                onNavigateFees = { onNavigateTab(AppTab.BILLING) }
            )
        }

        // Quick Actions Grid
        item {
            SectionHeader(title = "Quick Management Actions")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    QuickActionButton(
                        title = "Attendance",
                        subtitle = "Daily check-in",
                        icon = Icons.Default.EventAvailable,
                        color = Color(0xFF059669),
                        onClick = { onNavigateTab(AppTab.ATTENDANCE) },
                        tag = "quick_attendance"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Collect Fee",
                        subtitle = "Record payment",
                        icon = Icons.Default.AttachMoney,
                        color = NavyPrimary,
                        onClick = { onOpenCollectFee(null) },
                        tag = "quick_collect_fee"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Add Student",
                        subtitle = "New admission",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF0D9488),
                        onClick = onOpenAddStudent,
                        tag = "quick_add_student"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Exam Results",
                        subtitle = "Generate transcripts",
                        icon = Icons.Default.Grade,
                        color = Color(0xFF7C3AED),
                        onClick = { onNavigateTab(AppTab.EXAMS) },
                        tag = "quick_exam_results"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Staff & Payroll",
                        subtitle = "Staff records & leave",
                        icon = Icons.Default.Badge,
                        color = Color(0xFFB45309),
                        onClick = { onNavigateTab(AppTab.STAFF) },
                        tag = "quick_staff_payroll"
                    )
                }
                item {
                    QuickActionButton(
                        title = "Export CSV",
                        subtitle = "Accounts backup",
                        icon = Icons.Default.FileDownload,
                        color = Color(0xFF0284C7),
                        onClick = { showExportCsvDialog = true },
                        tag = "quick_export_csv"
                    )
                }
            }
        }

        // Recent Invoices / Ledger Records
        item {
            SectionHeader(
                title = "Recent Fee Billing Invoices",
                actionLabel = "View All Bills",
                onActionClick = { onNavigateTab(AppTab.BILLING) }
            )
        }

        if (summary.recentInvoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No billing records found. Tap 'Collect Fee' to generate your first invoice.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(summary.recentInvoices) { invoice ->
                InvoiceCardItem(
                    invoice = invoice,
                    onViewInvoice = { onViewInvoice(invoice) },
                    onCollect = { onOpenCollectFee(invoice) }
                )
            }
        }
    }

    if (showExportCsvDialog) {
        CsvBackupExportDialog(
            students = students,
            feeRecords = feeRecords,
            initialTab = 2,
            onDismiss = { showExportCsvDialog = false }
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InvoiceCardItem(
    invoice: FeeRecord,
    onViewInvoice: () -> Unit,
    onCollect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewInvoice)
            .testTag("invoice_item_${invoice.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceNo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = invoice.paymentStatus)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${invoice.monthYear} \u2022 Due: ${invoice.dueDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(invoice.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (invoice.pendingAmount > 0) {
                    Text(
                        text = "Pending: ${FormatUtils.formatCurrency(invoice.pendingAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    Text(
                        text = "Paid in full",
                        fontSize = 11.sp,
                        color = Color(0xFF059669)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSearchResultsPanel(
    results: DashboardSearchResults,
    onSelectStudent: (Student) -> Unit,
    onSelectStaff: (Staff) -> Unit,
    onSelectFee: (FeeRecord) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_search_results_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Results (${results.totalMatches})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tap to view",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (results.isEmpty) {
                Text(
                    text = "No matching students, staff, or fee records found.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Students Matches
                if (results.matchingStudents.isNotEmpty()) {
                    Text("Students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    results.matchingStudents.forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectStudent(st) }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = st.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${st.grade}-${st.section} \u2022 Roll #${st.rollNo} \u2022 ${st.admissionNo}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "View \u2192",
                                fontSize = 11.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    androidx.compose.material3.Divider()
                }

                // Staff Matches
                if (results.matchingStaff.isNotEmpty()) {
                    Text("Faculty & Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    results.matchingStaff.forEach { staff ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectStaff(staff) }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = staff.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${staff.role} \u2022 ${staff.department} (${staff.employeeId})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "View \u2192",
                                fontSize = 11.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    androidx.compose.material3.Divider()
                }

                // Fee Records Matches
                if (results.matchingFees.isNotEmpty()) {
                    Text("Fee Invoices & Receipts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    results.matchingFees.forEach { fee ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectFee(fee) }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${fee.invoiceNo} \u2022 ${fee.monthYear}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Due: ${fee.dueDate} \u2022 Status: ${fee.paymentStatus}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FormatUtils.formatCurrency(fee.totalAmount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
