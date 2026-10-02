package com.example.ui.staff

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.ui.SchoolMainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    viewModel: SchoolMainViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Staff & Payroll, 1: Leave Approvals

    val staffList by viewModel.filteredStaff.collectAsState()
    val allStaff by viewModel.staffList.collectAsState()
    val leaveRequests by viewModel.filteredLeaves.collectAsState()
    val roleFilter by viewModel.staffRoleFilter.collectAsState()
    val leaveFilter by viewModel.leaveStatusFilter.collectAsState()

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<Staff?>(null) }
    var selectedStaffForDetail by remember { mutableStateOf<Staff?>(null) }
    var showApplyLeaveDialog by remember { mutableStateOf(false) }

    val payroll = remember(allStaff) { viewModel.calculatePayroll() }
    val roles = listOf("All", "Teacher", "Department Head", "Accountant", "Administrator")
    val leaveStatusOptions = listOf("All", "Pending", "Approved", "Rejected")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedSubTab == 0) {
                        showAddStaffDialog = true
                    } else {
                        showApplyLeaveDialog = true
                    }
                },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("staff_fab_action")
            ) {
                Icon(
                    imageVector = if (selectedSubTab == 0) Icons.Default.PersonAdd else Icons.Default.EventAvailable,
                    contentDescription = if (selectedSubTab == 0) "Add Staff" else "Request Leave"
                )
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

            // Sub-Navigation Tabs
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Staff & Payroll (${allStaff.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Leave Approvals", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSubTab == 0) {
                // --- Staff & Payroll Section ---

                // Payroll KPI Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Institutional Payroll Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${payroll.totalStaffCount} Employed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Base Salary", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(payroll.totalBaseSalary), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Allowances", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("+ ${FormatUtils.formatCurrency(payroll.totalAllowances)}", fontSize = 14.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Monthly Payroll", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatCurrency(payroll.totalNetPayroll), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NavyPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Role Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(roles) { r ->
                        FilterChip(
                            selected = roleFilter.equals(r, ignoreCase = true),
                            onClick = { viewModel.setStaffRoleFilter(r) },
                            label = { Text(r) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Staff List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(staffList) { staff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStaffForDetail = staff }
                                .testTag("staff_card_${staff.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = staff.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = staff.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusBadge(status = staff.status)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${staff.role} \u2022 ${staff.department} \u2022 ID: ${staff.employeeId}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (staff.subjectsTaught.isNotBlank() && staff.subjectsTaught != "N/A") {
                                        Text(
                                            text = "Teaches: ${staff.subjectsTaught}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatCurrency(staff.netSalary),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Net Salary",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- Leave Management Section ---

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(leaveStatusOptions) { status ->
                        FilterChip(
                            selected = leaveFilter.equals(status, ignoreCase = true),
                            onClick = { viewModel.setLeaveStatusFilter(status) },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (leaveRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No leave requests found for '$leaveFilter'.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(leaveRequests) { leave ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("leave_request_${leave.id}"),
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
                                                text = leave.staffName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${leave.leaveType} \u2022 ${leave.daysCount} days (${leave.startDate} to ${leave.endDate})",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = leave.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Reason: ${leave.reason}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (leave.adminNotes != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Admin Note: ${leave.adminNotes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Approve / Reject Actions for Pending requests
                                    if (leave.status.equals("Pending", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.updateLeaveStatus(leave.id, "Rejected", "Declined due to exam schedule.")
                                                },
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("reject_leave_btn_${leave.id}"),
                                                contentPadding = PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reject", fontSize = 11.sp, color = Color(0xFFDC2626))
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = {
                                                    viewModel.updateLeaveStatus(leave.id, "Approved", "Approved by Administration.")
                                                },
                                                modifier = Modifier
                                                    .height(34.dp)
                                                    .testTag("approve_leave_btn_${leave.id}"),
                                                contentPadding = PaddingValues(horizontal = 10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Staff Dialog
    if (showAddStaffDialog || staffToEdit != null) {
        AddEditStaffDialog(
            initialStaff = staffToEdit,
            onDismiss = {
                showAddStaffDialog = false
                staffToEdit = null
            },
            onConfirm = { staff ->
                viewModel.addOrUpdateStaff(staff)
                showAddStaffDialog = false
                staffToEdit = null
            }
        )
    }

    // Staff Detail Sheet
    selectedStaffForDetail?.let { staff ->
        StaffDetailSheet(
            staff = staff,
            onDismiss = { selectedStaffForDetail = null },
            onEdit = {
                val s = staff
                selectedStaffForDetail = null
                staffToEdit = s
            },
            onDelete = {
                viewModel.deleteStaff(staff)
                selectedStaffForDetail = null
            }
        )
    }

    // Apply Leave Dialog
    if (showApplyLeaveDialog) {
        ApplyLeaveDialog(
            staffList = allStaff,
            onDismiss = { showApplyLeaveDialog = false },
            onConfirm = { request ->
                viewModel.submitLeaveRequest(request)
                showApplyLeaveDialog = false
            }
        )
    }
}
