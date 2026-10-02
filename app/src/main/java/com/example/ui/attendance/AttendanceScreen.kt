package com.example.ui.attendance

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.StudentAttendanceSummary
import com.example.ui.SchoolMainViewModel
import com.example.ui.theme.NavyPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: SchoolMainViewModel
) {
    // 0: Daily Check-In Register, 1: Student Summaries
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentDate by viewModel.selectedAttendanceDate.collectAsState()
    val gradeFilter by viewModel.attendanceFilterGrade.collectAsState()
    val summaries by viewModel.studentAttendanceSummaries.collectAsState()

    var studentForRemarkDialog by remember { mutableStateOf<StudentAttendanceSummary?>(null) }
    var remarkText by remember { mutableStateOf("") }

    val gradeOptions = listOf("All", "Grade 10", "Grade 9", "Grade 8")

    // Daily KPIs for the selected date
    val todayPresent = remember(summaries) { summaries.count { it.todayStatus == "Present" } }
    val todayLate = remember(summaries) { summaries.count { it.todayStatus == "Late" } }
    val todayAbsent = remember(summaries) { summaries.count { it.todayStatus == "Absent" } }
    val todayExcused = remember(summaries) { summaries.count { it.todayStatus == "Excused" } }
    val checkedInCount = todayPresent + todayLate + todayAbsent + todayExcused

    val attendancePct = if (summaries.isNotEmpty()) {
        ((todayPresent + todayLate).toDouble() / summaries.size) * 100.0
    } else 0.0

    // Date navigation helpers
    fun changeDateByDays(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(currentDate) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            viewModel.setAttendanceDate(sdf.format(cal.time))
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Date Picker & Day Navigation Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("attendance_header_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { changeDateByDays(-1) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_prev_date")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Daily Student Attendance",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { changeDateByDays(1) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_next_date")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Attendance KPI Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Present", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("$todayPresent", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Late", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("$todayLate", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Absent", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("$todayAbsent", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Excused", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("$todayExcused", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Today's Rate", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = String.format(Locale.US, "%.1f%%", attendancePct),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Actions & Grade Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.markAllStudentsPresentForDate(currentDate) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_mark_all_present"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark All Present", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.setAttendanceDate(today)
                },
                modifier = Modifier.testTag("btn_jump_today")
            ) {
                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Today", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Daily Check-In ($checkedInCount/${summaries.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_daily_checkin")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Student Summaries (${summaries.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_attendance_summaries")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grade Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(gradeOptions) { grade ->
                FilterChip(
                    selected = gradeFilter == grade,
                    onClick = { viewModel.setAttendanceFilterGrade(grade) },
                    label = { Text(grade) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (summaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No students match grade filter '$gradeFilter'.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else if (selectedTab == 0) {
            // ================= TAB 0: DAILY CHECK-IN REGISTER =================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(summaries) { summary ->
                    val student = summary.student

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_checkin_card_${student.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${student.grade}-${student.section} \u2022 Roll #${student.rollNo}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Check-in Time or Remark indicator
                                summary.todayCheckInTime?.let { time ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = time,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Status Buttons: Present, Late, Absent, Excused
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Present
                                StatusCheckInChip(
                                    label = "Present",
                                    icon = Icons.Default.Check,
                                    selected = summary.todayStatus == "Present",
                                    selectedColor = Color(0xFF059669),
                                    selectedContainer = Color(0xFFD1FAE5),
                                    modifier = Modifier.weight(1f).testTag("checkin_present_${student.id}"),
                                    onClick = { viewModel.recordStudentCheckIn(student.id, "Present") }
                                )

                                // Late
                                StatusCheckInChip(
                                    label = "Late",
                                    icon = Icons.Default.Schedule,
                                    selected = summary.todayStatus == "Late",
                                    selectedColor = Color(0xFFD97706),
                                    selectedContainer = Color(0xFFFEF3C7),
                                    modifier = Modifier.weight(1f).testTag("checkin_late_${student.id}"),
                                    onClick = { viewModel.recordStudentCheckIn(student.id, "Late") }
                                )

                                // Absent
                                StatusCheckInChip(
                                    label = "Absent",
                                    icon = Icons.Default.Close,
                                    selected = summary.todayStatus == "Absent",
                                    selectedColor = Color(0xFFDC2626),
                                    selectedContainer = Color(0xFFFEE2E2),
                                    modifier = Modifier.weight(1f).testTag("checkin_absent_${student.id}"),
                                    onClick = { viewModel.recordStudentCheckIn(student.id, "Absent") }
                                )

                                // Excused
                                StatusCheckInChip(
                                    label = "Excused",
                                    icon = Icons.Default.Info,
                                    selected = summary.todayStatus == "Excused",
                                    selectedColor = Color(0xFF2563EB),
                                    selectedContainer = Color(0xFFDBEAFE),
                                    modifier = Modifier.weight(1f).testTag("checkin_excused_${student.id}"),
                                    onClick = {
                                        studentForRemarkDialog = summary
                                        remarkText = summary.todayRemarks ?: "Authorized absence"
                                    }
                                )
                            }

                            if (!summary.todayRemarks.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Note: ${summary.todayRemarks}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ================= TAB 1: STUDENT ATTENDANCE SUMMARIES =================
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(summaries) { summary ->
                    val student = summary.student

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_summary_card_${student.id}"),
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${student.grade}-${student.section} \u2022 Roll #${student.rollNo}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Overall Attendance Percentage Badge
                                val pctColor = when {
                                    summary.attendancePercentage >= 90.0 -> Color(0xFF059669)
                                    summary.attendancePercentage >= 75.0 -> Color(0xFFD97706)
                                    else -> Color(0xFFDC2626)
                                }
                                val pctBg = when {
                                    summary.attendancePercentage >= 90.0 -> Color(0xFFD1FAE5)
                                    summary.attendancePercentage >= 75.0 -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFEE2E2)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = pctBg
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%%", summary.attendancePercentage),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = pctColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Attendance Rate Progress Bar
                            LinearProgressIndicator(
                                progress = { (summary.attendancePercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (summary.attendancePercentage >= 85.0) NavyPrimary else Color(0xFFDC2626),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Counters Breakdown: Total, Present, Late, Absent, Excused
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CounterTag(label = "Days", value = "${summary.totalRecordedDays}")
                                CounterTag(label = "Present", value = "${summary.presentCount}", color = Color(0xFF059669))
                                CounterTag(label = "Late", value = "${summary.lateCount}", color = Color(0xFFD97706))
                                CounterTag(label = "Absent", value = "${summary.absentCount}", color = Color(0xFFDC2626))
                                CounterTag(label = "Excused", value = "${summary.excusedCount}", color = Color(0xFF2563EB))
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
                                    text = "Status for $currentDate: ${summary.todayStatus ?: "Not recorded yet"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (summary.todayStatus != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )

                                TextButton(
                                    onClick = {
                                        viewModel.recordStudentCheckIn(student.id, "Present")
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Mark Present", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Remark Dialog for Excused / Special Leave
    studentForRemarkDialog?.let { summary ->
        AlertDialog(
            onDismissRequest = { studentForRemarkDialog = null },
            title = { Text("Record Excused Absence / Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student: ${summary.student.name} (${summary.student.grade})", fontSize = 13.sp)
                    OutlinedTextField(
                        value = remarkText,
                        onValueChange = { remarkText = it },
                        label = { Text("Reason / Parent Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordStudentCheckIn(
                            studentId = summary.student.id,
                            status = "Excused",
                            remarks = remarkText.trim()
                        )
                        studentForRemarkDialog = null
                    }
                ) {
                    Text("Save Excused Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForRemarkDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusCheckInChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    selectedColor: Color,
    selectedContainer: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) selectedContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CounterTag(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
