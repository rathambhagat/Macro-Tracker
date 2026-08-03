package com.bunkmarte.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity representing a single attendance record.
 *
 * Composite primary key: (date, start_time, end_time) ensures exactly one
 * record per slot per day. For merged labs, start_time and end_time span
 * the full merged range — this counts as ONE class in the database.
 */
@Entity(
    tableName = "attendance_records",
    primaryKeys = ["date", "start_time", "end_time"]
)
data class AttendanceRecord(
    @ColumnInfo(name = "date")
    val date: String,               // ISO format: "2026-07-30"

    @ColumnInfo(name = "day")
    val day: String,                // "Monday", "Tuesday", etc.

    @ColumnInfo(name = "start_time")
    val startTime: String,          // "10:00"

    @ColumnInfo(name = "end_time")
    val endTime: String,            // "12:00"

    @ColumnInfo(name = "original_subject")
    val originalSubject: String,    // From timetable.json

    @ColumnInfo(name = "effective_subject")
    val effectiveSubject: String,   // After override (defaults to original)

    @ColumnInfo(name = "status")
    val status: String              // AttendanceStatus enum name
)
