package com.example.ui.students

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
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
import com.example.data.model.Student
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditStudentDialog(
    initialStudent: Student? = null,
    onDismiss: () -> Unit,
    onConfirm: (Student) -> Unit
) {
    val isEditing = initialStudent != null

    // Personal Information Fields
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var guardianName by remember { mutableStateOf(initialStudent?.guardianName ?: "") }
    var phone by remember { mutableStateOf(initialStudent?.phone ?: "") }
    var email by remember { mutableStateOf(initialStudent?.email ?: "") }
    var address by remember { mutableStateOf(initialStudent?.address ?: "") }

    // Enrollment & Academic Data Fields
    var admissionNo by remember {
        mutableStateOf(initialStudent?.admissionNo ?: "ADM-2026-${(100..999).random()}")
    }
    var admissionDate by remember {
        mutableStateOf(
            initialStudent?.admissionDate
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
    var grade by remember { mutableStateOf(initialStudent?.grade ?: "Grade 10") }
    var section by remember { mutableStateOf(initialStudent?.section ?: "A") }
    var rollNo by remember { mutableStateOf(initialStudent?.rollNo ?: "") }
    var status by remember { mutableStateOf(initialStudent?.status ?: "Active") }
    var monthlyFeeStr by remember {
        mutableStateOf(initialStudent?.monthlyFee?.toString() ?: "450.0")
    }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Inactive")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Student Record" else "Student Admission Registration",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Personal Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Personal Information",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Jessica Williams") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("student_name_input")
                        )

                        OutlinedTextField(
                            value = guardianName,
                            onValueChange = { guardianName = it },
                            label = { Text("Guardian / Parent Name *") },
                            placeholder = { Text("e.g. Thomas Williams") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("student_guardian_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("student_phone_input")
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Residential Address") },
                            placeholder = { Text("e.g. 42 Highland Terrace") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Section 2: Enrollment & Academic Data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enrollment & Academic Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = admissionNo,
                                onValueChange = { admissionNo = it },
                                label = { Text("Admission No *") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("student_admission_input")
                            )
                            OutlinedTextField(
                                value = admissionDate,
                                onValueChange = { admissionDate = it },
                                label = { Text("Admission Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("student_admission_date_input")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = grade,
                                onValueChange = { grade = it },
                                label = { Text("Class / Grade *") },
                                placeholder = { Text("Grade 10") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("student_grade_input")
                            )
                            OutlinedTextField(
                                value = section,
                                onValueChange = { section = it },
                                label = { Text("Section") },
                                placeholder = { Text("A") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f).testTag("student_section_input")
                            )
                            OutlinedTextField(
                                value = rollNo,
                                onValueChange = { rollNo = it },
                                label = { Text("Roll No") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.7f).testTag("student_roll_input")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = monthlyFeeStr,
                                onValueChange = { monthlyFeeStr = it },
                                label = { Text("Monthly Tuition (Rs.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("student_fee_input")
                            )

                            // Status Dropdown
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { statusDropdownExpanded = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(status, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = statusDropdownExpanded,
                                    onDismissRequest = { statusDropdownExpanded = false }
                                ) {
                                    statusOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                status = opt
                                                statusDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val fee = monthlyFeeStr.toDoubleOrNull() ?: 450.0
                        val student = Student(
                            id = initialStudent?.id ?: 0L,
                            admissionNo = admissionNo.ifBlank { "ADM-${System.currentTimeMillis() % 1000}" },
                            name = name.trim(),
                            grade = grade.trim().ifBlank { "Grade 10" },
                            section = section.trim().ifBlank { "A" },
                            rollNo = rollNo.trim().ifBlank { "1" },
                            guardianName = guardianName.trim().ifBlank { "Guardian" },
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim().ifBlank { "Main Campus District" },
                            monthlyFee = fee,
                            admissionDate = admissionDate.trim(),
                            status = status
                        )
                        onConfirm(student)
                    }
                },
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text(if (isEditing) "Update Record" else "Save Student Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
