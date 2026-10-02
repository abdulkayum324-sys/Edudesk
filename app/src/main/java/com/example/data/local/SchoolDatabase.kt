package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeRecord
import com.example.data.model.LeaveRequest
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentAttendance
import com.example.data.model.Subject

@Database(
    entities = [
        Student::class,
        FeeRecord::class,
        Exam::class,
        Subject::class,
        ExamResult::class,
        Staff::class,
        LeaveRequest::class,
        StudentAttendance::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun feeDao(): FeeDao
    abstract fun staffDao(): StaffDao
    abstract fun examDao(): ExamDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "edudesk_school.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
