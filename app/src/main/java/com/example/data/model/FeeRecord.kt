package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fee_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"]), Index(value = ["invoiceNo"])]
)
data class FeeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val invoiceNo: String,       // e.g. "INV-2026-1001"
    val monthYear: String,       // e.g. "September 2026"
    val dueDate: String,         // e.g. "2026-09-15"
    val tuitionFee: Double,
    val libraryFee: Double = 0.0,
    val transportFee: Double = 0.0,
    val labOrExamFee: Double = 0.0,
    val otherFee: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentStatus: String,   // "Paid", "Partial", "Pending"
    val paymentDate: String? = null,
    val paymentMethod: String? = null, // "Cash", "Online/UPI", "Bank Transfer", "Cheque"
    val receiptNo: String? = null,      // e.g. "REC-9042"
    val remarks: String? = null
) {
    val pendingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
