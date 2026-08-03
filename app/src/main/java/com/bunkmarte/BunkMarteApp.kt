package com.bunkmarte

import android.app.Application
import com.bunkmarte.data.db.AttendanceDatabase
import com.bunkmarte.data.model.MergedSlot
import com.bunkmarte.data.parser.TimetableParser
import com.bunkmarte.data.repository.AttendanceRepository

/**
 * Application class for BunkMarte.
 *
 * Provides lazy-initialized singletons for the database, repository,
 * parsed timetable, and subject list. These are accessed by ViewModels
 * via (application as BunkMarteApp).
 */
class BunkMarteApp : Application() {

    /** Room database singleton. */
    val database: AttendanceDatabase by lazy {
        AttendanceDatabase.getInstance(this)
    }

    /** Repository abstracting database access. */
    val repository: AttendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao())
    }

    /**
     * Parsed timetable: map of day name → list of merged class slots.
     * Parsed once from assets/timetable.json on first access.
     */
    val timetable: Map<String, List<MergedSlot>> by lazy {
        TimetableParser.parse(this)
    }

    /**
     * Sorted list of all unique subject names from the timetable.
     * Used to populate the override dropdown.
     */
    val allSubjects: List<String> by lazy {
        TimetableParser.extractAllSubjects(this)
    }
}
