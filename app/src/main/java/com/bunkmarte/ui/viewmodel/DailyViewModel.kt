package com.bunkmarte.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunkmarte.BunkMarteApp
import com.bunkmarte.data.model.AttendanceRecord
import com.bunkmarte.data.model.AttendanceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * UI state for a single class slot on the daily view.
 */
data class SlotUiState(
    val startTime: String,
    val endTime: String,
    val originalSubject: String,
    val effectiveSubject: String,
    val status: AttendanceStatus? = null,
    val isOverridden: Boolean = false,
    val slotCount: Int = 1
)

/**
 * Full UI state for the Daily screen.
 */
data class DailyUiState(
    val selectedDay: String = "",
    val date: String = "",                      // ISO: "2026-07-30"
    val displayDate: String = "",               // "Wednesday, July 30, 2026"
    val slots: List<SlotUiState> = emptyList(),
    val allSubjects: List<String> = emptyList(),
    val isExamDay: Boolean = false,
    val isWeekend: Boolean = false,
    val showExamDayDialog: Boolean = false
)

/**
 * ViewModel for the Daily (home) screen.
 *
 * Responsibilities:
 * - Auto-detect current day and display appropriate timetable slots
 * - Handle day switching within the current week
 * - Persist attendance status changes immediately to Room
 * - Handle override subjects, exam day marking, and status toggling
 * - Restore previously saved states when reopening the app
 */
class DailyViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BunkMarteApp

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val dayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY ||
                        today.dayOfWeek == DayOfWeek.SUNDAY

        // On weekends, default to Monday; otherwise show today
        val initialDay = if (isWeekend) "Monday" else dayName
        selectDay(initialDay)
    }

    /**
     * Switch the displayed day. Loads timetable slots for that day and
     * restores any previously saved attendance records from the database.
     */
    fun selectDay(dayName: String) {
        viewModelScope.launch {
            val date = getDateForDayInCurrentWeek(dayName)
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val displayDate = date.format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)
            )
            val mergedSlots = app.timetable[dayName] ?: emptyList()
            val existingRecords = app.repository.getRecordsForDate(dateStr)

            val isExamDay = existingRecords.any {
                it.status == AttendanceStatus.EXAM_DAY.name
            }

            val slotStates = mergedSlots.map { slot ->
                val record = existingRecords.find { r ->
                    r.startTime == slot.startTime && r.endTime == slot.endTime
                }
                SlotUiState(
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    originalSubject = slot.subject,
                    effectiveSubject = record?.effectiveSubject ?: slot.subject,
                    status = record?.status?.let { AttendanceStatus.fromName(it) },
                    isOverridden = record?.let {
                        it.effectiveSubject != it.originalSubject
                    } ?: false,
                    slotCount = slot.slotCount
                )
            }

            val today = LocalDate.now()
            _uiState.value = DailyUiState(
                selectedDay = dayName,
                date = dateStr,
                displayDate = displayDate,
                slots = slotStates,
                allSubjects = app.allSubjects,
                isExamDay = isExamDay,
                isWeekend = today.dayOfWeek == DayOfWeek.SATURDAY ||
                            today.dayOfWeek == DayOfWeek.SUNDAY,
                showExamDayDialog = false
            )
        }
    }

    /**
     * Set the attendance status for a specific slot.
     *
     * Special behaviors:
     * - EXAM_DAY: Opens confirmation dialog (doesn't persist immediately)
     * - Same status clicked again: Toggles off (removes record from DB)
     * - Any status while exam day is active: Clears exam day, sets this slot's status
     */
    fun setStatus(slotIndex: Int, status: AttendanceStatus) {
        val state = _uiState.value
        if (slotIndex !in state.slots.indices) return

        val slot = state.slots[slotIndex]

        // Toggle off: clicking the same status removes it
        if (slot.status == status && status != AttendanceStatus.EXAM_DAY) {
            viewModelScope.launch {
                app.repository.deleteRecord(state.date, slot.startTime, slot.endTime)
                val updatedSlots = state.slots.toMutableList()
                updatedSlots[slotIndex] = slot.copy(status = null)
                _uiState.value = state.copy(slots = updatedSlots)
            }
            return
        }

        // Exam Day requires confirmation
        if (status == AttendanceStatus.EXAM_DAY) {
            _uiState.value = state.copy(showExamDayDialog = true)
            return
        }

        viewModelScope.launch {
            // If currently exam day, clear all EXAM_DAY records first
            if (state.isExamDay) {
                app.repository.deleteRecordsForDate(state.date)
            }

            val record = AttendanceRecord(
                date = state.date,
                day = state.selectedDay,
                startTime = slot.startTime,
                endTime = slot.endTime,
                originalSubject = slot.originalSubject,
                effectiveSubject = slot.effectiveSubject,
                status = status.name
            )
            app.repository.upsert(record)

            val updatedSlots = state.slots.toMutableList()
            if (state.isExamDay) {
                // Clear all exam day statuses from UI
                for (i in updatedSlots.indices) {
                    updatedSlots[i] = updatedSlots[i].copy(status = null)
                }
            }
            updatedSlots[slotIndex] = updatedSlots[slotIndex].copy(status = status)

            _uiState.value = state.copy(
                slots = updatedSlots,
                isExamDay = false
            )
        }
    }

    /**
     * Confirm marking the entire day as Exam Day.
     * Replaces all records for this date with EXAM_DAY status.
     */
    fun confirmExamDay() {
        viewModelScope.launch {
            val state = _uiState.value
            val mergedSlots = app.timetable[state.selectedDay] ?: emptyList()
            app.repository.markExamDay(state.date, state.selectedDay, mergedSlots)

            val updatedSlots = state.slots.map {
                it.copy(status = AttendanceStatus.EXAM_DAY)
            }
            _uiState.value = state.copy(
                slots = updatedSlots,
                isExamDay = true,
                showExamDayDialog = false
            )
        }
    }

    /** Dismiss the Exam Day confirmation dialog without action. */
    fun dismissExamDayDialog() {
        _uiState.value = _uiState.value.copy(showExamDayDialog = false)
    }

    /**
     * Override the subject for a specific slot.
     * If a status has already been recorded, updates the DB immediately.
     */
    fun overrideSubject(slotIndex: Int, newSubject: String) {
        val state = _uiState.value
        if (slotIndex !in state.slots.indices) return

        val slot = state.slots[slotIndex]

        viewModelScope.launch {
            val updatedSlots = state.slots.toMutableList()
            updatedSlots[slotIndex] = slot.copy(
                effectiveSubject = newSubject,
                isOverridden = newSubject != slot.originalSubject
            )
            _uiState.value = state.copy(slots = updatedSlots)

            // If there's already a status recorded, update the DB too
            if (slot.status != null) {
                val record = AttendanceRecord(
                    date = state.date,
                    day = state.selectedDay,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    originalSubject = slot.originalSubject,
                    effectiveSubject = newSubject,
                    status = slot.status.name
                )
                app.repository.upsert(record)
            }
        }
    }

    /**
     * Calculate the date for a given day name within the current week.
     *
     * - On a weekday: returns the date of that day in the current Mon–Fri week
     * - On a weekend: returns the date of that day from the previous week
     */
    private fun getDateForDayInCurrentWeek(dayName: String): LocalDate {
        val today = LocalDate.now()
        val targetDow = when (dayName) {
            "Monday" -> DayOfWeek.MONDAY
            "Tuesday" -> DayOfWeek.TUESDAY
            "Wednesday" -> DayOfWeek.WEDNESDAY
            "Thursday" -> DayOfWeek.THURSDAY
            "Friday" -> DayOfWeek.FRIDAY
            else -> return today
        }
        val diff = targetDow.value - today.dayOfWeek.value
        return today.plusDays(diff.toLong())
    }
}
