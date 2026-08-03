package com.bunkmarte.data.model

enum class AttendanceStatus(
    val displayName: String,
    val shortLabel: String
) {
    PRESENT("Present", "Present"),
    ABSENT("Absent", "Absent"),
    BUNK("Bunk", "Bunk"),
    MASS_BUNK("Mass Bunk", "Mass Bunk"),
    PROXY("Proxy/Freebie", "Proxy"),
    CANCELLED("Cancelled", "Cancelled"),
    EXAM_DAY("Exam Day", "Exam Day"),
    HOLIDAY("Holiday", "Holiday");

    companion object {
        fun fromName(name: String): AttendanceStatus? {
            return try {
                valueOf(name)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}