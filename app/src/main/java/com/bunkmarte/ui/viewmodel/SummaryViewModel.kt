package com.bunkmarte.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunkmarte.BunkMarteApp
import com.bunkmarte.data.model.AttendanceStatus
import com.bunkmarte.domain.AttendanceCalculator
import com.bunkmarte.domain.SubjectStatsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Summary screen.
 */
data class SummaryUiState(
    val subjectStats: List<SubjectStatsUi> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Subject Summary screen.
 *
 * Loads all attendance records from the database, groups them by effective
 * subject, and calculates the 3-case percentages + predictor for each subject.
 *
 * EXAM_DAY records are fully excluded from all calculations.
 * All timetable subjects are shown even if they have no records yet.
 */
class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BunkMarteApp

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    /**
     * Reload all attendance statistics from the database.
     * Called on init and when navigating back to this screen.
     */
    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val allRecords = app.repository.getAllRecords()

            // Filter out EXAM_DAY records — they don't count in any formula
            val relevantRecords = allRecords.filter {
                it.status != AttendanceStatus.EXAM_DAY.name
            }

            // Group by the effective (possibly overridden) subject
            val grouped = relevantRecords.groupBy { it.effectiveSubject }

            // Calculate stats for every subject in the timetable (even with 0 records)
            val stats = app.allSubjects.map { subject ->
                val subjectRecords = grouped[subject] ?: emptyList()
                AttendanceCalculator.calculateStats(subject, subjectRecords)
            }

            _uiState.value = SummaryUiState(
                subjectStats = stats,
                isLoading = false
            )
        }
    }
}
