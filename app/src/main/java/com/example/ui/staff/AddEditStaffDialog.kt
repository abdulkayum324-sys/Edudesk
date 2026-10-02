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
import com.example.data.model.Staff
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditStaffDialog(
    initialStaff: Staff? = null,
    onDismiss: () -> Unit,
    onConfirm: (Staff) -> Unit
) {
    val isEditing = initialStaff != null

    var name by remember { mutableStateOf(initialStaff?.name ?: "") }
    var employeeId by remember {
        mutableStateOf(initialStaff?.employeeId ?: "EMP-${(100..999).random()}")
    }
    var role by remember { mutableStateOf(initialStaff?.role ?: "Teacher") }
    var department by remember { mutableStateOf(initialStaff?.department ?: "Mathematics") }
    var email by remember { mutableStateOf(initialStaff?.email ?: "") }
    var phone by remember { mutableStateOf(initialStaff?.phone ?: "") }
    var address by remember { mutableStateOf(initialStaff?.address ?: "") }
    var subjectsTaught by remember { mutableStateOf(initialStaff?.subjectsTaught ?: "") }
    var assignedClasses by remember { mutableStateOf(initialStaff?.assignedClasses ?: "") }

    var baseSalaryStr by remember { mutableStateOf(initialStaff?.baseSalary?.toString() ?: "4200.0") }
    var allowancesStr by remember { mutableStateOf(initialStaff?.allowances?.toString() ?: "400.0") }
    var deductionsStr by remember { mutableStateOf(initialStaff?.deductions?.toString() ?: "200.0") }

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val roles = listOf("Teacher", "Department Head", "Accountant", "Administrator", "Librarian", "Support Staff")

    val baseSalary = baseSalaryStr.toDoubleOrNull() ?: 0.0
    val allowances = allowancesStr.toDoubleOrNull() ?: 0.0
    val deductions = deductionsStr.toDoubleOrNull() ?: 0.0
    val netSalary = (baseSalary + allowances - deductions).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Staff Record" else "Add New Staff Member",
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Staff Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("staff_emp_id_input")
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Role Dropdown
                Text("Assigned Role", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { roleDropdownExpanded = true },
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
                        Text(role, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = roleDropdownExpanded, onDismissRequest = { roleDropdownExpanded = false }) {
                    roles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                role = r
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Campus Residence") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subjectsTaught,
                    onValueChange = { subjectsTaught = it },
                    label = { Text("Subjects Taught (if applicable)") },
                    placeholder = { Text("e.g. Mathematics, Calculus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = assignedClasses,
                    onValueChange = { assignedClasses = it },
                    label = { Text("Assigned Classes / Grades") },
                    placeholder = { Text("e.g. Grade 10-A, Grade 9-B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Payroll Fields
                Text("Monthly Payroll Configuration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = baseSalaryStr,
                        onValueChange = { baseSalaryStr = it },
                        label = { Text("Base Salary ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("staff_salary_input")
                    )
                    OutlinedTextField(
                        value = allowancesStr,
                        onValueChange = { allowancesStr = it },
                        label = { Text("Allowances ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deductionsStr,
                        onValueChange = { deductionsStr = it },
                        label = { Text("Deductions ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Net Pay preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calculated Net Pay:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(FormatUtils.formatCurrency(netSalary), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val joiningDate = initialStaff?.joiningDate
                            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val staff = Staff(
                            id = initialStaff?.id ?: 0L,
                            employeeId = employeeId.trim(),
                            name = name.trim(),
                            role = role,
                            department = department.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            joiningDate = joiningDate,
                            subjectsTaught = subjectsTaught.trim().ifBlank { "N/A" },
                            assignedClasses = assignedClasses.trim().ifBlank { "N/A" },
                            baseSalary = baseSalary,
                            allowances = allowances,
                            deductions = deductions,
                            status = initialStaff?.status ?: "Active"
                        )
                        onConfirm(staff)
                    }
                },
                modifier = Modifier.testTag("save_staff_button")
            ) {
                Text(if (isEditing) "Save Staff" else "Add Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
