package com.example.ui.staff

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
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ApplyLeaveDialog(
    staffList: List<Staff>,
    onDismiss: () -> Unit,
    onConfirm: (LeaveRequest) -> Unit
) {
    var selectedStaff by remember { mutableStateOf(staffList.firstOrNull()) }
    var staffDropdown by remember { mutableStateOf(false) }

    var leaveType by remember { mutableStateOf("Casual Leave") }
    var leaveTypeDropdown by remember { mutableStateOf(false) }
    val leaveTypes = listOf("Casual Leave", "Sick Leave", "Annual Leave", "Maternity/Paternity", "Bereavement")

    var startDate by remember { mutableStateOf("2026-10-12") }
    var endDate by remember { mutableStateOf("2026-10-14") }
    var daysCountStr by remember { mutableStateOf("3") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Submit Staff Leave Request",
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
                // Staff Picker
                Text("Select Faculty / Staff Member", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { staffDropdown = true },
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
                            text = selectedStaff?.let { "${it.name} (${it.role})" } ?: "Select Staff",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = staffDropdown, onDismissRequest = { staffDropdown = false }) {
                    staffList.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} - ${s.role} (${s.department})") },
                            onClick = {
                                selectedStaff = s
                                staffDropdown = false
                            }
                        )
                    }
                }

                // Leave Type Picker
                Text("Leave Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { leaveTypeDropdown = true },
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
                        Text(leaveType, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = leaveTypeDropdown, onDismissRequest = { leaveTypeDropdown = false }) {
                    leaveTypes.forEach { lt ->
                        DropdownMenuItem(
                            text = { Text(lt) },
                            onClick = {
                                leaveType = lt
                                leaveTypeDropdown = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = daysCountStr,
                    onValueChange = { daysCountStr = it },
                    label = { Text("Number of Days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Absence / Leave") },
                    placeholder = { Text("e.g. Attending academic conference") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().testTag("leave_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val staff = selectedStaff
                    if (staff != null && reason.isNotBlank()) {
                        val days = daysCountStr.toIntOrNull() ?: 1
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val req = LeaveRequest(
                            staffId = staff.id,
                            staffName = staff.name,
                            leaveType = leaveType,
                            startDate = startDate.trim(),
                            endDate = endDate.trim(),
                            daysCount = days,
                            reason = reason.trim(),
                            status = "Pending",
                            appliedDate = today
                        )
                        onConfirm(req)
                    }
                },
                modifier = Modifier.testTag("submit_leave_button")
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
