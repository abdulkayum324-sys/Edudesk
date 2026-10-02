package com.example.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.StudentAttendance
import com.example.ui.theme.NavyPrimary
import com.example.util.FormatUtils
import java.util.Locale

data class DayAttendanceStat(
    val dateLabel: String,
    val fullDate: String,
    val totalCheckedIn: Int,
    val present: Int,
    val late: Int,
    val absent: Int,
    val attendancePct: Float
)

@Composable
fun DashboardVisualChartsCard(
    attendanceList: List<StudentAttendance>,
    feeRecords: List<FeeRecord>,
    totalStudentsCount: Int,
    onNavigateAttendance: () -> Unit,
    onNavigateFees: () -> Unit
) {
    // 0: Daily Attendance Trends, 1: Month Fee Breakdown
    var activeChartTab by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_visual_charts_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Chart Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeChartTab == 0) Icons.Default.TrendingUp else Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (activeChartTab == 0) "Daily Attendance Trends" else "Fee Collections & Dues",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (activeChartTab == 0) "Last 5 School Sessions" else "Current Month Ledger Breakdown",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chart Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = activeChartTab == 0,
                        onClick = { activeChartTab = 0 },
                        label = { Text("Attendance", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chart_tab_attendance")
                    )

                    FilterChip(
                        selected = activeChartTab == 1,
                        onClick = { activeChartTab = 1 },
                        label = { Text("Fees", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chart_tab_fees")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeChartTab == 0) {
                AttendanceTrendsChart(
                    attendanceList = attendanceList,
                    totalStudents = totalStudentsCount,
                    onViewMore = onNavigateAttendance
                )
            } else {
                FeeCollectionsDuesChart(
                    feeRecords = feeRecords,
                    onViewMore = onNavigateFees
                )
            }
        }
    }
}

@Composable
fun AttendanceTrendsChart(
    attendanceList: List<StudentAttendance>,
    totalStudents: Int,
    onViewMore: () -> Unit
) {
    // Group attendance by date
    val dayStats: List<DayAttendanceStat> = remember(attendanceList, totalStudents) {
        val grouped = attendanceList.groupBy { it.date }
        val sortedDates = grouped.keys.sorted().takeLast(5)
        val totalCount = if (totalStudents > 0) totalStudents else 6

        sortedDates.map { date ->
            val logs = grouped[date] ?: emptyList()
            val present = logs.count { it.status == "Present" }
            val late = logs.count { it.status == "Late" }
            val absent = logs.count { it.status == "Absent" }
            val pct = ((present + late).toFloat() / totalCount.toFloat()) * 100f

            // Format date label e.g., "09-28"
            val label = if (date.length >= 10) date.substring(5) else date

            DayAttendanceStat(
                dateLabel = label,
                fullDate = date,
                totalCheckedIn = logs.size,
                present = present,
                late = late,
                absent = absent,
                attendancePct = pct.coerceIn(0f, 100f)
            )
        }
    }

    if (dayStats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No attendance trends recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val totalBarsCount = dayStats.size
        Column {
            // Visual Canvas Trend Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val barWidth = 24.dp.toPx()
                    val spacing = (w - (barWidth * totalBarsCount)) / (totalBarsCount + 1)

                    val points = mutableListOf<Offset>()

                    dayStats.forEachIndexed { i, stat ->
                        val x = spacing + i * (barWidth + spacing) + barWidth / 2
                        val barHeight = (stat.attendancePct / 100f) * (h * 0.75f)
                        val barTop = h - barHeight

                        // Draw background track
                        drawRoundRect(
                            color = Color(0xFFE2E8F0),
                            topLeft = Offset(x - barWidth / 2, 0f),
                            size = Size(barWidth, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )

                        // Draw Bar Fill
                        val barColor = when {
                            stat.attendancePct >= 90f -> Color(0xFF059669)
                            stat.attendancePct >= 75f -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x - barWidth / 2, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )

                        points.add(Offset(x, barTop))
                    }

                    // Draw Trend Curve
                    if (points.size > 1) {
                        val path = Path()
                        path.moveTo(points.first().x, points.first().y)
                        for (j in 1 until points.size) {
                            path.lineTo(points[j].x, points[j].y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF1E3A8A),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Trend Points
                        points.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                            drawCircle(color = Color(0xFF1E3A8A), radius = 2.5.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayStats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stat.dateLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f%%", stat.attendancePct),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stat.attendancePct >= 90f) Color(0xFF059669) else Color(0xFFD97706)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem(color = Color(0xFF059669), label = ">=90% High")
                    LegendItem(color = Color(0xFFD97706), label = "75-89% Moderate")
                    LegendItem(color = Color(0xFFDC2626), label = "<75% Low")
                }

                Text(
                    text = "Open Register \u2192",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.clickable(onClick = onViewMore)
                )
            }
        }
    }
}

@Composable
fun FeeCollectionsDuesChart(
    feeRecords: List<FeeRecord>,
    onViewMore: () -> Unit
) {
    val totalBilled = remember(feeRecords) { feeRecords.sumOf { it.totalAmount } }
    val totalCollected = remember(feeRecords) { feeRecords.sumOf { it.paidAmount } }
    val totalPending = remember(feeRecords) { feeRecords.sumOf { it.pendingAmount } }

    val collectionPct = if (totalBilled > 0) ((totalCollected / totalBilled) * 100.0).toFloat() else 0f
    val pendingPct = if (totalBilled > 0) ((totalPending / totalBilled) * 100.0).toFloat() else 0f

    val paidCount = remember(feeRecords) { feeRecords.count { it.paymentStatus == "Paid" } }
    val partialCount = remember(feeRecords) { feeRecords.count { it.paymentStatus == "Partial" } }
    val pendingCount = remember(feeRecords) { feeRecords.count { it.paymentStatus == "Pending" } }

    Column {
        // High Level Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Billed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(FormatUtils.formatCurrency(totalBilled), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Collected (${String.format(Locale.US, "%.1f%%", collectionPct)})", fontSize = 11.sp, color = Color(0xFF059669))
                Text(FormatUtils.formatCurrency(totalCollected), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Pending (${String.format(Locale.US, "%.1f%%", pendingPct)})", fontSize = 11.sp, color = Color(0xFFDC2626))
                Text(FormatUtils.formatCurrency(totalPending), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Split Ratio Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            if (collectionPct > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(collectionPct.coerceAtLeast(1f))
                        .background(Color(0xFF059669))
                )
            }
            if (pendingPct > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(pendingPct.coerceAtLeast(1f))
                        .background(Color(0xFFDC2626))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Invoices Count Status Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCountChip(
                label = "Paid in Full",
                count = paidCount,
                color = Color(0xFF059669),
                bg = Color(0xFFD1FAE5),
                modifier = Modifier.weight(1f)
            )
            StatusCountChip(
                label = "Partial Paid",
                count = partialCount,
                color = Color(0xFFD97706),
                bg = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f)
            )
            StatusCountChip(
                label = "Unpaid Dues",
                count = pendingCount,
                color = Color(0xFFDC2626),
                bg = Color(0xFFFEE2E2),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Manage Invoices & Billing \u2192",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                modifier = Modifier.clickable(onClick = onViewMore)
            )
        }
    }
}

@Composable
fun StatusCountChip(
    label: String,
    count: Int,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = color)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
