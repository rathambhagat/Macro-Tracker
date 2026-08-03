package com.bunkmarte.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bunkmarte.data.model.AttendanceRecord

/**
 * Room database for BunkMarte. Single table: attendance_records.
 * Version 1 — initial schema.
 */
@Database(
    entities = [AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AttendanceDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AttendanceDatabase? = null

        fun getInstance(context: Context): AttendanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    "bunkmarte_attendance.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
