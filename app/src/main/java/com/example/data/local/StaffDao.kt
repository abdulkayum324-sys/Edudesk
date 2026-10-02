package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    // --- Staff Management ---
    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :staffId LIMIT 1")
    suspend fun getStaffById(staffId: Long): Staff?

    @Query("SELECT * FROM staff WHERE role = :role ORDER BY name ASC")
    fun getStaffByRole(role: String): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staffList: List<Staff>)

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)

    // --- Leave Requests ---
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE status = :status ORDER BY id DESC")
    fun getLeaveRequestsByStatus(status: String): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE staffId = :staffId ORDER BY id DESC")
    fun getLeaveRequestsForStaff(staffId: Long): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leave: LeaveRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequests(leaves: List<LeaveRequest>)

    @Update
    suspend fun updateLeaveRequest(leave: LeaveRequest)

    @Delete
    suspend fun deleteLeaveRequest(leave: LeaveRequest)

    @Query("SELECT COUNT(*) FROM leave_requests WHERE status = 'Pending'")
    fun getPendingLeaveCount(): Flow<Int>
}
