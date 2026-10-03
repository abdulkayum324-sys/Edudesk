package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_profile")
data class SchoolProfile(
    @PrimaryKey val id: Long = 1L,
    val schoolName: String = "Oakridge International Academy",
    val schoolMotto: String = "Excellence in Academic Leadership & Innovation",
    val campusAddress: String = "Main Academic Campus, Kathmandu, Nepal",
    val contactPhone: String = "+977-1-4567890",
    val contactEmail: String = "info@school.edu.np",
    val academicSession: String = "Session 2025-2026",
    val principalName: String = "Dr. Arthur Pendelton",
    val currencySymbol: String = "Rs.",
    val affiliationCode: String = "NEB-REG-48201",
    val websiteUrl: String = "www.oakridge.edu.np"
)
