package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY grade ASC, section ASC, rollNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Query("SELECT * FROM students WHERE grade = :grade ORDER BY rollNo ASC")
    fun getStudentsByGrade(grade: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNo LIKE '%' || :query || '%' OR admissionNo LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
}
