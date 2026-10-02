package com.example.ui.exams

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
import com.example.data.model.Exam
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.util.FormatUtils

@Composable
fun EnterMarksDialog(
    exams: List<Exam>,
    students: List<Student>,
    subjects: List<Subject>,
    preSelectedStudent: Student? = null,
    onDismiss: () -> Unit,
    onSaveMarks: (examId: Long, studentId: Long, subjectId: Long, marks: Double, maxMarks: Double, grade: String, gpa: Double, remarks: String) -> Unit
) {
    var selectedExam by remember { mutableStateOf(exams.firstOrNull()) }
    var selectedStudent by remember { mutableStateOf(preSelectedStudent ?: students.firstOrNull()) }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }

    var examDropdown by remember { mutableStateOf(false) }
    var studentDropdown by remember { mutableStateOf(false) }
    var subjectDropdown by remember { mutableStateOf(false) }

    var marksStr by remember { mutableStateOf("85.0") }
    var maxMarksStr by remember { mutableStateOf("100.0") }
    var remarks by remember { mutableStateOf("Strong academic performance") }

    val marks = marksStr.toDoubleOrNull() ?: 0.0
    val maxMarks = maxMarksStr.toDoubleOrNull() ?: 100.0
    val percentage = if (maxMarks > 0) (marks / maxMarks) * 100.0 else 0.0
    val (gradeLetter, gradePoint) = FormatUtils.calculateGrade(percentage)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Examination Marks",
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
                // Exam Picker
                Text("Select Examination", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { examDropdown = true },
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
                        Text(selectedExam?.name ?: "Select Exam", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = examDropdown, onDismissRequest = { examDropdown = false }) {
                    exams.forEach { e ->
                        DropdownMenuItem(
                            text = { Text(e.name) },
                            onClick = {
                                selectedExam = e
                                examDropdown = false
                            }
                        )
                    }
                }

                // Student Picker
                Text("Select Student", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { studentDropdown = true },
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
                        Text(selectedStudent?.let { "${it.name} (${it.grade})" } ?: "Select Student", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = studentDropdown, onDismissRequest = { studentDropdown = false }) {
                    students.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} - ${s.grade} (Roll: ${s.rollNo})") },
                            onClick = {
                                selectedStudent = s
                                studentDropdown = false
                            }
                        )
                    }
                }

                // Subject Picker
                Text("Select Subject", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { subjectDropdown = true },
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
                        Text(selectedSubject?.name ?: "Select Subject", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                    subjects.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text("${sub.name} (${sub.code})") },
                            onClick = {
                                selectedSubject = sub
                                maxMarksStr = sub.maxMarks.toString()
                                subjectDropdown = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = marksStr,
                        onValueChange = { marksStr = it },
                        label = { Text("Marks Obtained") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("marks_obtained_input")
                    )
                    OutlinedTextField(
                        value = maxMarksStr,
                        onValueChange = { maxMarksStr = it },
                        label = { Text("Max Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Computed Grade Preview
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
                        Text("Grade: $gradeLetter ($gradePoint GPA)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${percentage.toInt()}% Score", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks / Feedback") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ex = selectedExam
                    val st = selectedStudent
                    val sb = selectedSubject
                    if (ex != null && st != null && sb != null) {
                        onSaveMarks(ex.id, st.id, sb.id, marks, maxMarks, gradeLetter, gradePoint, remarks)
                    }
                },
                modifier = Modifier.testTag("save_marks_button")
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
