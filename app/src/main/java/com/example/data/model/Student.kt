package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val admissionNo: String,
    val name: String,
    val grade: String,          // e.g. "Grade 10", "Grade 9"
    val section: String,        // e.g. "A", "B"
    val rollNo: String,
    val guardianName: String,
    val phone: String,
    val email: String,
    val address: String = "Main Campus District",
    val monthlyFee: Double,     // Base monthly tuition fee
    val admissionDate: String,  // e.g. "2024-08-15"
    val status: String = "Active" // "Active", "Inactive"
)
