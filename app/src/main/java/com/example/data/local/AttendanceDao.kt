package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentAttendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM student_attendance ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<StudentAttendance>>

    @Query("SELECT * FROM student_attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<StudentAttendance>>

    @Query("SELECT * FROM student_attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<StudentAttendance>>

    @Query("SELECT * FROM student_attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getStudentAttendanceForDate(studentId: Long, date: String): StudentAttendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: StudentAttendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendanceList(attendances: List<StudentAttendance>)

    @Update
    suspend fun updateAttendance(attendance: StudentAttendance)

    @Delete
    suspend fun deleteAttendance(attendance: StudentAttendance)

    @Query("DELETE FROM student_attendance WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceForDate(studentId: Long, date: String)
}
