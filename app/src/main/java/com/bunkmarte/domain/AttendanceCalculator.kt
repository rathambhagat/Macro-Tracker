package com.bunkmarte.domain

import com.bunkmarte.data.model.AttendanceRecord
import com.bunkmarte.data.model.AttendanceStatus
import kotlin.math.ceil
import kotlin.math.max

/**
 * Warning level for Case 1 percentage, driving color coding in the UI.
 */
enum class WarningLevel {
    SAFE,      // >= 75% — Green
    DANGER,    // 60% to <75% — Amber/Orange
    CRITICAL   // < 60% — Red
}

/**
 * Computed statistics for a single subject, displayed on the Summary screen.
 */
data class SubjectStatsUi(
    val subject: String,
    val totalClasses: Int,
    val presentCount: Int,
    val proxyCount: Int,
    val absentCount: Int,
    val bunkCount: Int,
    val massBunkCount: Int,
    val cancelledCount: Int,
    val case1: Double?,       // Normal percentage (null if denominator = 0)
    val case2: Double?,       // With mass bunk penalty
    val case3: Double?,       // With cancelled bonus
    val classesNeeded: Int?,  // Classes to attend to reach 75% (null = no data, 0 = safe)
    val warningLevel: WarningLevel
)

/**
 * Calculates attendance percentages using the 3-case formula system.
 *
 * In all formulas, Proxy/Freebie counts identically to Present.
 *
 * Case 1 (Normal):
 *   (Present + Proxy) / (Present + Proxy + Absent + Bunk)
 *
 * Case 2 (Mass Bunk Penalty):
 *   (Present + Proxy) / (Present + Proxy + Absent + Bunk + Mass Bunk)
 *
 * Case 3 (Cancelled Bonus):
 *   (Present + Proxy + Cancelled) / (Present + Proxy + Absent + Bunk + Cancelled)
 *
 * Predictor (based on Case 1):
 *   Solve for X in: (P + X) / (P + A + B + X) >= 0.75
 *   Result: X = max(0, ceil( (0.75 * (P+A+B) - P) / 0.25 ))
 *   Simplified: X = max(0, ceil( 3*(A+B) - P ))
 */
object AttendanceCalculator {

    fun calculateStats(
        subject: String,
        records: List<AttendanceRecord>
    ): SubjectStatsUi {
        // Count each status type
        val presentCount = records.count { it.status == AttendanceStatus.PRESENT.name }
        val proxyCount = records.count { it.status == AttendanceStatus.PROXY.name }
        val absentCount = records.count { it.status == AttendanceStatus.ABSENT.name }
        val bunkCount = records.count { it.status == AttendanceStatus.BUNK.name }
        val massBunkCount = records.count { it.status == AttendanceStatus.MASS_BUNK.name }
        val cancelledCount = records.count { it.status == AttendanceStatus.CANCELLED.name }

        // Effective present = Present + Proxy (treated identically)
        val p = presentCount + proxyCount

        // ── Case 1: Normal ──
        val case1Denom = p + absentCount + bunkCount
        val case1: Double? = if (case1Denom > 0) {
            (p.toDouble() / case1Denom) * 100.0
        } else null

        // ── Case 2: Mass Bunk Penalty ──
        val case2Denom = p + absentCount + bunkCount + massBunkCount
        val case2: Double? = if (case2Denom > 0) {
            (p.toDouble() / case2Denom) * 100.0
        } else null

        // ── Case 3: Cancelled Bonus ──
        val case3Denom = p + absentCount + bunkCount + cancelledCount
        val case3: Double? = if (case3Denom > 0) {
            ((p + cancelledCount).toDouble() / case3Denom) * 100.0
        } else null

        // ── Predictor: classes needed to reach 75% (based on Case 1) ──
        //
        // Algebra: (P + X) / (P + A + B + X) >= 0.75
        //   P + X >= 0.75*(P + A + B + X)
        //   0.25X >= 0.75*(P + A + B) - P
        //   X >= (0.75 * case1Denom - P) / 0.25
        //   X >= 3*(A + B) - P
        //
        // Use ceiling to ensure integer result meets threshold.
        val classesNeeded: Int? = when {
            case1Denom == 0 -> null                     // No data yet
            case1 != null && case1 >= 75.0 -> 0         // Already safe
            else -> {
                val x = ceil(
                    (0.75 * case1Denom.toDouble() - p.toDouble()) / 0.25
                ).toInt()
                max(0, x)
            }
        }

        // ── Warning level for Case 1 ──
        val warningLevel = when {
            case1 == null -> WarningLevel.SAFE
            case1 < 60.0 -> WarningLevel.CRITICAL
            case1 < 75.0 -> WarningLevel.DANGER
            else -> WarningLevel.SAFE
        }

        return SubjectStatsUi(
            subject = subject,
            totalClasses = records.size,
            presentCount = presentCount,
            proxyCount = proxyCount,
            absentCount = absentCount,
            bunkCount = bunkCount,
            massBunkCount = massBunkCount,
            cancelledCount = cancelledCount,
            case1 = case1,
            case2 = case2,
            case3 = case3,
            classesNeeded = classesNeeded,
            warningLevel = warningLevel
        )
    }
}
