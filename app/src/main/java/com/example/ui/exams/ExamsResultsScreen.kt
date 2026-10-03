package com.example.ui.exams

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.Student
import com.example.data.model.StudentGpaSummary
import com.example.data.model.StudentReportCard
import com.example.data.model.Subject
import com.example.ui.SchoolMainViewModel
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ExamsResultsScreen(
    viewModel: SchoolMainViewModel
) {
    val exams by viewModel.exams.collectAsState()
    val students by viewModel.students.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val examResults by viewModel.examResults.collectAsState()

    var selectedExam by remember { mutableStateOf<Exam?>(null) }
    var examDropdownExpanded by remember { mutableStateOf(false) }

    var showEnterMarksDialog by remember { mutableStateOf(false) }
    var preSelectedStudentForMarks by remember { mutableStateOf<Student?>(null) }
    var activeReportCard by remember { mutableStateOf<StudentReportCard?>(null) }
    var studentSearchQuery by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val activeExam = selectedExam ?: exams.firstOrNull()

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }

    // Calculate GPA summaries for all students in the active exam
    val gpaSummaries = remember(students, examResults, subjects, activeExam) {
        if (activeExam == null) return@remember emptyList<StudentGpaSummary>()
        val examResultsFiltered = examResults.filter { it.examId == activeExam.id }
        val resultsByStudent = examResultsFiltered.groupBy { it.studentId }

        students.map { student ->
            val stdResults = resultsByStudent[student.id] ?: emptyList()
            val totalObtained = stdResults.sumOf { it.marksObtained }
            val totalMax = stdResults.sumOf { it.maxMarks }
            val pct = if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0
            val gpa = if (stdResults.isNotEmpty()) stdResults.map { it.gradePoint }.average() else 0.0
            val (overallGrade, _) = FormatUtils.calculateGrade(pct)
            val passed = stdResults.count { r ->
                val sub = subjectsMap[r.subjectId]
                r.marksObtained >= (sub?.passMarks ?: 40.0)
            }
            val failed = stdResults.size - passed

            StudentGpaSummary(
                student = student,
                examId = activeExam.id,
                examName = activeExam.name,
                totalMarksObtained = totalObtained,
                totalMaxMarks = totalMax,
                overallPercentage = pct,
                overallGrade = if (stdResults.isEmpty()) "N/A" else overallGrade,
                gpa = gpa,
                subjectsEvaluated = stdResults.size,
                passedCount = passed,
                failedCount = failed
            )
        }
    }

    val filteredSummaries = remember(gpaSummaries, studentSearchQuery) {
        if (studentSearchQuery.isBlank()) {
            gpaSummaries
        } else {
            gpaSummaries.filter {
                it.student.name.contains(studentSearchQuery, ignoreCase = true) ||
                        it.student.rollNo.contains(studentSearchQuery, ignoreCase = true) ||
                        it.student.grade.contains(studentSearchQuery, ignoreCase = true)
            }
        }
    }

    val classAvgGpa = remember(gpaSummaries) {
        val evaluated = gpaSummaries.filter { it.subjectsEvaluated > 0 }
        if (evaluated.isNotEmpty()) evaluated.map { it.gpa }.average() else 0.0
    }

    val classAvgPercentage = remember(gpaSummaries) {
        val evaluated = gpaSummaries.filter { it.subjectsEvaluated > 0 }
        if (evaluated.isNotEmpty()) evaluated.map { it.overallPercentage }.average() else 0.0
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    preSelectedStudentForMarks = null
                    showEnterMarksDialog = true
                },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_enter_marks")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Marks")
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

            // Exam Selector Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_selector_header"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Academic Evaluation & Examination",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { examDropdownExpanded = true }
                            .testTag("exam_dropdown_trigger"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeExam?.name ?: "No Exam Scheduled",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Session: ${activeExam?.academicYear ?: ""} \u2022 ${activeExam?.term ?: ""} (${activeExam?.status ?: ""})",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = examDropdownExpanded,
                        onDismissRequest = { examDropdownExpanded = false }
                    ) {
                        exams.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text("${exam.name} (${exam.academicYear})") },
                                onClick = {
                                    selectedExam = exam
                                    examDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Class GPA Analytics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Class Avg GPA", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = String.format(Locale.US, "%.2f / 4.0", classAvgGpa),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text("Class Avg Score", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", classAvgPercentage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Evaluated Students", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "${gpaSummaries.count { it.subjectsEvaluated > 0 }} of ${students.size}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        preSelectedStudentForMarks = null
                        showEnterMarksDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_enter_marks"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Marks per Subject", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = studentSearchQuery,
                onValueChange = { studentSearchQuery = it },
                placeholder = { Text("Filter student results...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_student_search_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Student Results & GPA Summaries Roster
            Text(
                text = "Student GPA Summaries & Subject Records (${filteredSummaries.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredSummaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No student exam records found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredSummaries) { summary ->
                        val studentResults = remember(examResults, activeExam, summary.student.id) {
                            if (activeExam == null) emptyList()
                            else examResults.filter { it.examId == activeExam.id && it.studentId == summary.student.id }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_result_card_${summary.student.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top Row: Student Name, Roll, and Calculated GPA Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = summary.student.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${summary.student.grade}-${summary.student.section} \u2022 Roll #${summary.student.rollNo}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Calculated GPA & Grade Badge
                                    if (summary.subjectsEvaluated > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (summary.gpa >= 3.0) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = "GPA ${String.format(Locale.US, "%.2f", summary.gpa)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = if (summary.gpa >= 3.0) Color(0xFF065F46) else Color(0xFF92400E)
                                                )
                                                Text(
                                                    text = "${summary.overallGrade} (${summary.overallPercentage.toInt()}%)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (summary.gpa >= 3.0) Color(0xFF047857) else Color(0xFFB45309)
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "No Marks Yet",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))

                                // Subject Marks Chips
                                if (studentResults.isNotEmpty()) {
                                    Text(
                                        text = "Subject Marks Breakdown (${studentResults.size} recorded):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(studentResults) { res ->
                                            val sub = subjectsMap[res.subjectId]
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${sub?.name ?: "Subject"}: ",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${res.marksObtained.toInt()}/${res.maxMarks.toInt()} (${res.gradeLetter})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Card Footer: Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${summary.passedCount} Passed \u2022 ${summary.failedCount} Failed",
                                        fontSize = 11.sp,
                                        color = if (summary.failedCount > 0) MaterialTheme.colorScheme.error else Color(0xFF059669)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                preSelectedStudentForMarks = summary.student
                                                showEnterMarksDialog = true
                                            },
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_add_marks_${summary.student.id}"),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Add Marks", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                activeExam?.let { exam ->
                                                    coroutineScope.launch {
                                                        val card = viewModel.getStudentReportCard(exam.id, summary.student.id)
                                                        activeReportCard = card
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_generate_transcript_${summary.student.id}"),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Report Card", fontSize = 11.sp)
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

    // Enter Marks Dialog
    if (showEnterMarksDialog && activeExam != null) {
        EnterMarksDialog(
            exams = exams,
            students = students,
            subjects = subjects,
            preSelectedStudent = preSelectedStudentForMarks,
            onDismiss = {
                showEnterMarksDialog = false
                preSelectedStudentForMarks = null
            },
            onSaveMarks = { examId, studentId, subjectId, marks, maxMarks, grade, gpa, remarks ->
                viewModel.recordSubjectMarks(examId, studentId, subjectId, marks, maxMarks, grade, gpa, remarks)
                showEnterMarksDialog = false
                preSelectedStudentForMarks = null
            }
        )
    }

    // Active Report Card Dialog
    val schoolProfile by viewModel.schoolProfile.collectAsState()

    activeReportCard?.let { reportCard ->
        StudentReportCardDialog(
            reportCard = reportCard,
            schoolName = schoolProfile.schoolName,
            schoolAddress = schoolProfile.campusAddress,
            onDismiss = { activeReportCard = null }
        )
    }
}
