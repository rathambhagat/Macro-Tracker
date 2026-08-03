package com.bunkmarte.data.repository

import com.bunkmarte.data.db.AttendanceDao
import com.bunkmarte.data.model.AttendanceRecord
import com.bunkmarte.data.model.AttendanceStatus
import com.bunkmarte.data.model.MergedSlot

/**
 * Repository abstracting database access for attendance records.
 * All methods are suspend functions for coroutine support.
 */
class AttendanceRepository(private val dao: AttendanceDao) {

    /** Insert or update a single attendance record (upsert by composite PK). */
    suspend fun upsert(record: AttendanceRecord) {
        dao.upsert(record)
    }

    /** Get all records for a specific date (ISO format). */
    suspend fun getRecordsForDate(date: String): List<AttendanceRecord> {
        return dao.getRecordsForDate(date)
    }

    /** Get every record in the database. */
    suspend fun getAllRecords(): List<AttendanceRecord> {
        return dao.getAllRecords()
    }

    /** Delete all records for a specific date. */
    suspend fun deleteRecordsForDate(date: String) {
        dao.deleteRecordsForDate(date)
    }

    /** Delete a single record by its composite key. Used for toggling off a status. */
    suspend fun deleteRecord(date: String, startTime: String, endTime: String) {
        dao.deleteRecord(date, startTime, endTime)
    }

    /**
     * Mark an entire day as Exam Day.
     *
     * Atomically deletes all existing records for the date and inserts
     * EXAM_DAY records for every merged slot. EXAM_DAY records are excluded
     * from all attendance percentage calculations.
     */
    suspend fun markExamDay(date: String, day: String, mergedSlots: List<MergedSlot>) {
        val records = mergedSlots.map { slot ->
            AttendanceRecord(
                date = date,
                day = day,
                startTime = slot.startTime,
                endTime = slot.endTime,
                originalSubject = slot.subject,
                effectiveSubject = slot.subject,
                status = AttendanceStatus.EXAM_DAY.name
            )
        }
        dao.replaceAllForDate(date, records)
    }
}
