package com.bunkmarte.data.parser

import android.content.Context
import com.bunkmarte.data.model.MergedSlot
import org.json.JSONObject

/**
 * Parses timetable.json from the app's assets directory.
 * Handles:
 * - Filtering out empty ("") slots
 * - Sorting by 24-hour time (converts 01:00-08:00 to 13:00-20:00)
 * - Merging consecutive LAB slots (subjects containing "LAB") into single MergedSlot entries
 * - Keeping consecutive standard lectures (like TFCS) as separate individual slots
 * - Extracting all unique subjects for the override dropdown
 */
object TimetableParser {

    /**
     * Internal representation of a single raw timetable slot before merging.
     */
    private data class RawSlot(
        val startTime: String,
        val endTime: String,
        val subject: String,
        val sortKey: Int          // Minutes from midnight in 24-hour format
    )

    /**
     * Parses timetable.json and returns a map of day name -> list of class slots.
     * Empty slots are filtered. Consecutive identical LAB subjects are merged.
     */
    fun parse(context: Context): Map<String, List<MergedSlot>> {
        val json = context.assets.open("timetable.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val result = linkedMapOf<String, List<MergedSlot>>()

        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        for (day in dayOrder) {
            if (!root.has(day)) continue
            val dayObj = root.getJSONObject(day)
            val rawSlots = mutableListOf<RawSlot>()

            val keys = dayObj.keys()
            while (keys.hasNext()) {
                val timeRange = keys.next()
                val subject = dayObj.getString(timeRange)

                // Skip empty slots
                if (subject.isBlank()) continue

                val parts = timeRange.split(" To ")
                if (parts.size != 2) continue

                val startTime = parts[0].trim()
                val endTime = parts[1].trim()
                val sortKey = toMinutes24(startTime)

                rawSlots.add(RawSlot(startTime, endTime, subject, sortKey))
            }

            // Sort by 24-hour time to ensure correct ordering
            rawSlots.sortBy { it.sortKey }

            // Merge consecutive slots ONLY if they are LABS
            result[day] = mergeConsecutiveLabs(rawSlots)
        }

        return result
    }

    /**
     * Extracts all unique, non-empty subject names from the timetable.
     * Used to populate the override dropdown.
     */
    fun extractAllSubjects(context: Context): List<String> {
        val json = context.assets.open("timetable.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val subjects = mutableSetOf<String>()

        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        for (day in dayOrder) {
            if (!root.has(day)) continue
            val dayObj = root.getJSONObject(day)
            val keys = dayObj.keys()
            while (keys.hasNext()) {
                val subject = dayObj.getString(keys.next())
                if (subject.isNotBlank()) {
                    subjects.add(subject)
                }
            }
        }

        return subjects.sorted()
    }

    /**
     * Merges consecutive RawSlots ONLY if the subject is a LAB (contains "LAB").
     * Standard back-to-back lectures (e.g., TFCS 03:00-04:00 and TFCS 04:00-05:00)
     * remain separate classes.
     */
    private fun mergeConsecutiveLabs(slots: List<RawSlot>): List<MergedSlot> {
        if (slots.isEmpty()) return emptyList()

        val merged = mutableListOf<MergedSlot>()

        var currentStart = slots[0].startTime
        var currentEnd = slots[0].endTime
        var currentSubject = slots[0].subject
        var count = 1

        for (i in 1 until slots.size) {
            val slot = slots[i]
            val prevEndMinutes = toMinutes24(currentEnd)
            val nextStartMinutes = toMinutes24(slot.startTime)

            // CHECK: Must be same subject, consecutive times, AND must contain "LAB"
            val isLab = currentSubject.uppercase().contains("LAB")

            if (isLab && slot.subject == currentSubject && nextStartMinutes == prevEndMinutes) {
                // Extend the merged lab block
                currentEnd = slot.endTime
                count++
            } else {
                // Commit previous slot/lab block and start a new one
                merged.add(MergedSlot(currentSubject, currentStart, currentEnd, count))
                currentStart = slot.startTime
                currentEnd = slot.endTime
                currentSubject = slot.subject
                count = 1
            }
        }

        // Add final block
        merged.add(MergedSlot(currentSubject, currentStart, currentEnd, count))

        return merged
    }

    /**
     * Converts display time string ("01:00") to minutes from midnight (24-hr format).
     */
    private fun toMinutes24(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        var hour = parts[0].toIntOrNull() ?: return 0
        val minute = parts[1].toIntOrNull() ?: return 0

        if (hour in 1..8) {
            hour += 12
        }

        return hour * 60 + minute
    }
}