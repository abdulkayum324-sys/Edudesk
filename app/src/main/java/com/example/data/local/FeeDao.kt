package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FeeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {

    @Query("SELECT * FROM fee_records ORDER BY id DESC")
    fun getAllFeeRecords(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getFeeRecordsForStudent(studentId: Long): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE id = :invoiceId LIMIT 1")
    suspend fun getFeeRecordById(invoiceId: Long): FeeRecord?

    @Query("SELECT * FROM fee_records WHERE paymentStatus != 'Paid' ORDER BY id DESC")
    fun getPendingFeeRecords(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE paymentStatus = :status ORDER BY id DESC")
    fun getFeeRecordsByStatus(status: String): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(feeRecord: FeeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecords(feeRecords: List<FeeRecord>)

    @Update
    suspend fun updateFeeRecord(feeRecord: FeeRecord)

    @Delete
    suspend fun deleteFeeRecord(feeRecord: FeeRecord)

    @Query("SELECT SUM(paidAmount) FROM fee_records")
    fun getTotalCollectedAmount(): Flow<Double?>

    @Query("SELECT SUM(totalAmount - paidAmount) FROM fee_records WHERE paymentStatus != 'Paid'")
    fun getTotalPendingAmount(): Flow<Double?>
}
