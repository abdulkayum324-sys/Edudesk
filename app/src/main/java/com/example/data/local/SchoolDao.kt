package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Students ---
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

    // --- Fees & Invoices ---
    @Query("SELECT * FROM fee_records ORDER BY id DESC")
    fun getAllFeeRecords(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getFeeRecordsForStudent(studentId: Long): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE id = :invoiceId LIMIT 1")
    suspend fun getFeeRecordById(invoiceId: Long): FeeRecord?

    @Query("SELECT * FROM fee_records WHERE paymentStatus != 'Paid' ORDER BY id DESC")
    fun getPendingFeeRecords(): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(feeRecord: FeeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecords(feeRecords: List<FeeRecord>)

    @Update
    suspend fun updateFeeRecord(feeRecord: FeeRecord)

    @Delete
    suspend fun deleteFeeRecord(feeRecord: FeeRecord)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leave: LeaveRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequests(leaves: List<LeaveRequest>)

    @Update
    suspend fun updateLeaveRequest(leave: LeaveRequest)

    @Delete
    suspend fun deleteLeaveRequest(leave: LeaveRequest)

    // Count checks for seeding
    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    // --- Student Attendance ---
    @Query("SELECT COUNT(*) FROM student_attendance")
    suspend fun getAttendanceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAttendanceList(records: List<com.example.data.model.StudentAttendance>)

    // --- School Profile ---
    @Query("SELECT * FROM school_profile WHERE id = 1 LIMIT 1")
    fun getSchoolProfile(): Flow<com.example.data.model.SchoolProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSchoolProfile(profile: com.example.data.model.SchoolProfile)

    // --- User Accounts (RBAC) ---
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUserAccounts(): Flow<List<com.example.data.model.UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: com.example.data.model.UserAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccounts(users: List<com.example.data.model.UserAccount>)

    @Update
    suspend fun updateUserAccount(user: com.example.data.model.UserAccount)

    @Delete
    suspend fun deleteUserAccount(user: com.example.data.model.UserAccount)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserAccountCount(): Int
}
