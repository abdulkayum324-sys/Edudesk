package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val role: String, // "ADMIN", "ACCOUNTANT", "TEACHER", "PARENT"
    val schoolTenantId: String = "school-1",
    val status: String = "Active",
    val createdAt: String = "2026-10-01"
)
