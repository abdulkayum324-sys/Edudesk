package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    // --- Exams ---
    @Query("SELECT * FROM exams ORDER BY id DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :examId LIMIT 1")
    suspend fun getExamById(examId: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE grade = :grade OR grade = 'All' ORDER BY name ASC")
    fun getSubjectsForGrade(grade: String): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    // --- Exam Results ---
    @Query("SELECT * FROM exam_results ORDER BY id DESC")
    fun getAllExamResults(): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getAllResultsForStudent(studentId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId AND studentId = :studentId AND subjectId = :subjectId LIMIT 1")
    suspend fun getResult(examId: Long, studentId: Long, subjectId: Long): ExamResult?

    @Query("SELECT * FROM exam_results WHERE examId = :examId AND studentId = :studentId")
    fun getResultsForStudentInExam(examId: Long, studentId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getAllResultsForExam(examId: Long): Flow<List<ExamResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResults(results: List<ExamResult>)

    @Update
    suspend fun updateExamResult(result: ExamResult)

    @Delete
    suspend fun deleteExamResult(result: ExamResult)
}
