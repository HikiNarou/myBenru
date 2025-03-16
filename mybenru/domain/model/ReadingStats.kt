package com.mybenru.domain.model

/**
 * Domain model representing reading statistics
 */
data class ReadingStats(
    val novelsInLibrary: Int = 0,
    val completedNovels: Int = 0,
    val chaptersRead: Int = 0,
    val totalWordCount: Int = 0,
    val totalReadingTimeMs: Long = 0L,
    val averageChaptersPerDay: Float = 0f
) {
    /**
     * Get the total reading time in hours
     *
     * @return Total reading time in hours
     */
    fun getTotalReadingTimeHours(): Float {
        return totalReadingTimeMs / (1000f * 60f * 60f)
    }

    /**
     * Get the total reading time as a formatted string
     *
     * @return Formatted reading time string (e.g., "5h 30m")
     */
    fun getFormattedReadingTime(): String {
        val hours = totalReadingTimeMs / (1000 * 60 * 60)
        val minutes = (totalReadingTimeMs / (1000 * 60)) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}