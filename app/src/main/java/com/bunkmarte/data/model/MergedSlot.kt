package com.bunkmarte.data.model

/**
 * Represents one or more consecutive timetable slots that have been merged
 * into a single logical class block.
 *
 * Example: Two consecutive "LAB:AI/DAA" slots (10:00-11:00, 11:00-12:00)
 * merge into MergedSlot("LAB:AI/DAA", "10:00", "12:00", slotCount=2).
 */
data class MergedSlot(
    val subject: String,
    val startTime: String,   // Display format: "10:00"
    val endTime: String,     // Display format: "12:00"
    val slotCount: Int       // How many raw 1-hour slots were merged
)
