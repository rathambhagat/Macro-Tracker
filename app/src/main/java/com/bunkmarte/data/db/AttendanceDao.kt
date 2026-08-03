package com.bunkmarte.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bunkmarte.data.model.AttendanceRecord

/**
 * Data Access Object for attendance records.
 * Uses REPLACE strategy on the composite PK (date, start_time, end_time)
 * for upsert behavior — updating an existing record or inserting a new one.
 */
@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getRecordsForDate(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecords(): List<AttendanceRecord>

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteRecordsForDate(date: String)

    @Query(
        "DELETE FROM attendance_records " +
        "WHERE date = :date AND start_time = :startTime AND end_time = :endTime"
    )
    suspend fun deleteRecord(date: String, startTime: String, endTime: String)

    /**
     * Atomically replaces all records for a given date.
     * Used for Exam Day: deletes existing records, then inserts EXAM_DAY for every slot.
     */
    @Transaction
    suspend fun replaceAllForDate(date: String, records: List<AttendanceRecord>) {
        deleteRecordsForDate(date)
        records.forEach { upsert(it) }
    }
}
