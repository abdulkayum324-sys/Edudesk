package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.data.model.StudentAttendance

object NotificationHelper {

    const val CHANNEL_FEES_ID = "channel_fee_reminders"
    const val CHANNEL_ATTENDANCE_ID = "channel_attendance_alerts"

    const val NOTIFICATION_ID_FEES = 1001
    const val NOTIFICATION_ID_ATTENDANCE = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val feeChannel = NotificationChannel(
                CHANNEL_FEES_ID,
                "Fee Due & Billing Deadlines",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts administrators of pending student fee deadlines"
            }

            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE_ID,
                "Daily Student Attendance Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts administrators when daily student check-ins are missing"
            }

            notificationManager.createNotificationChannel(feeChannel)
            notificationManager.createNotificationChannel(attendanceChannel)
        }
    }

    fun sendFeeDeadlineAlert(
        context: Context,
        pendingCount: Int,
        dueDate: String,
        pendingAmount: Double
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "Upcoming Fee Deadline: $dueDate"
        val message = "$pendingCount student invoices have pending dues totaling ${FormatUtils.formatCurrency(pendingAmount)}."

        val builder = NotificationCompat.Builder(context, CHANNEL_FEES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_FEES, builder.build())
        } catch (_: SecurityException) {
            // Permission not yet granted
        }
    }

    fun sendMissingAttendanceAlert(
        context: Context,
        missingStudentsCount: Int,
        dateStr: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "Attendance Alert: $dateStr"
        val message = "$missingStudentsCount active students are missing morning check-ins for $dateStr."

        val builder = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID_ATTENDANCE, builder.build())
        } catch (_: SecurityException) {
            // Permission not yet granted
        }
    }

    /**
     * Checks current status and sends notifications if criteria are met, plus shows feedback toast.
     */
    fun checkAndTriggerAdminAlerts(
        context: Context,
        students: List<Student>,
        feeRecords: List<FeeRecord>,
        attendanceList: List<StudentAttendance>,
        dateStr: String
    ): String {
        var alertsSent = 0

        // 1. Fee Deadline check
        val pendingInvoices = feeRecords.filter { it.pendingAmount > 0 }
        if (pendingInvoices.isNotEmpty()) {
            val totalPending = pendingInvoices.sumOf { it.pendingAmount }
            val nextDueDate = pendingInvoices.minByOrNull { it.dueDate }?.dueDate ?: "Upcoming"
            sendFeeDeadlineAlert(context, pendingInvoices.size, nextDueDate, totalPending)
            alertsSent++
        }

        // 2. Attendance Check
        val activeStudents = students.filter { it.status == "Active" }
        val checkedInIds = attendanceList.filter { it.date == dateStr }.map { it.studentId }.toSet()
        val missingCount = activeStudents.count { !checkedInIds.contains(it.id) }
        if (missingCount > 0) {
            sendMissingAttendanceAlert(context, missingCount, dateStr)
            alertsSent++
        }

        val message = if (alertsSent > 0) {
            "Admin alert dispatched: $alertsSent notification(s) sent to device tray."
        } else {
            "All accounts and attendance records are up to date. No pending alerts."
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        return message
    }
}
