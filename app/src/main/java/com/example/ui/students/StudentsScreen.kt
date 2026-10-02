package com.example.ui.students

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun StudentsScreen(
    viewModel: SchoolMainViewModel,
    onOpenAddStudent: () -> Unit,
    onOpenEditStudent: (Student) -> Unit,
    onCreateFeeBillForStudent: (Student) -> Unit,
    onCollectPaymentForInvoice: (FeeRecord) -> Unit,
    onPrintBill: (Student, FeeRecord) -> Unit
) {
    val students by viewModel.filteredStudents.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val feeRecords by viewModel.feeRecords.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedGrade by viewModel.selectedGradeFilter.collectAsState()
    val selectedStatus by viewModel.studentStatusFilter.collectAsState()

    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var showExportCsvDialog by remember { mutableStateOf(false) }

    val gradeOptions = listOf("All", "Grade 10", "Grade 9", "Grade 8")
    val statusOptions = listOf("All", "Active", "Inactive")

    val activeCount = remember(allStudents) { allStudents.count { it.status == "Active" } }
    val inactiveCount = remember(allStudents) { allStudents.count { it.status != "Active" } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddStudent,
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
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

            // Enrollment Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Records & Enrollment",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${allStudents.size} Registered Students",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFD1FAE5)) {
                            Text(
                                text = "$activeCount Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (inactiveCount > 0) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEE2E2)) {
                                Text(
                                    text = "$inactiveCount Inactive",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setStudentSearchQuery(it) },
                placeholder = { Text("Search by name, roll no, or admission no...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grade & Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(gradeOptions) { grade ->
                    FilterChip(
                        selected = selectedGrade == grade,
                        onClick = { viewModel.setSelectedGradeFilter(grade) },
                        label = { Text(grade, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(statusOptions) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { viewModel.setStudentStatusFilter(status) },
                        label = { Text("Status: $status", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Count Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled Records (${students.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = { showExportCsvDialog = true },
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_export_students_csv"),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No student records match current criteria.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(students) { student ->
                        val studentInvoices = feeRecords.filter { it.studentId == student.id }
                        val pendingAmount = studentInvoices.sumOf { it.pendingAmount }

                        StudentRecordCard(
                            student = student,
                            pendingAmount = pendingAmount,
                            onCardClick = { selectedStudentForDetail = student },
                            onEditClick = { onOpenEditStudent(student) },
                            onDeleteClick = { studentToDelete = student },
                            onViewLedger = { selectedStudentForDetail = student }
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedStudentForDetail?.let { student ->
        val studentInvoices = feeRecords.filter { it.studentId == student.id }
        StudentAccountLedgerSheet(
            student = student,
            feeHistory = studentInvoices,
            onDismiss = { selectedStudentForDetail = null },
            onEditStudent = {
                val s = student
                selectedStudentForDetail = null
                onOpenEditStudent(s)
            },
            onDeleteStudent = {
                val s = student
                selectedStudentForDetail = null
                studentToDelete = s
            },
            onCreateFeeBill = {
                val s = student
                selectedStudentForDetail = null
                onCreateFeeBillForStudent(s)
            },
            onCollectPayment = { invoice ->
                selectedStudentForDetail = null
                onCollectPaymentForInvoice(invoice)
            },
            onPrintBill = { invoice ->
                onPrintBill(student, invoice)
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student Record", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete the student record for ${student.name} (${student.admissionNo})? All associated fee history and results will also be deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_student_btn")
                ) {
                    Text("Delete Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CSV Backup Export Dialog
    if (showExportCsvDialog) {
        CsvBackupExportDialog(
            students = allStudents,
            feeRecords = feeRecords,
            initialTab = 0,
            onDismiss = { showExportCsvDialog = false }
        )
    }
}

@Composable
fun StudentRecordCard(
    student: Student,
    pendingAmount: Double,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewLedger: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Status, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(status = student.status)
                    }
                    Text(
                        text = "Adm: ${student.admissionNo} \u2022 Roll #${student.rollNo}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(34.dp).testTag("edit_student_${student.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(34.dp).testTag("delete_student_${student.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Two-Column Grid: Enrollment Data & Personal Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Enrollment Column
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("ENROLLMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${student.grade} - Sec ${student.section}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Admitted: ${student.admissionDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "Tuition: ${FormatUtils.formatCurrency(student.monthlyFee)}/mo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Personal Info Column
                Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("PERSONAL / GUARDIAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.guardianName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(student.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Outstanding Balance & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pendingAmount > 0) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEE2E2)) {
                        Text(
                            text = "Due: ${FormatUtils.formatCurrency(pendingAmount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFD1FAE5)) {
                        Text(
                            text = "Fees Paid in Full",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onViewLedger,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ledger & Fees", fontSize = 11.sp)
                }
            }
        }
    }
}
