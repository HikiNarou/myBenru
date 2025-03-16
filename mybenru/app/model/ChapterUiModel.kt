package com.mybenru.app.model

import com.mybenru.app.utils.DateUtils

/**
 * UI model for representing a chapter in the presentation layer
 */
data class ChapterUiModel(
    val id: String,
    val novelId: String,
    val url: String,
    val title: String,
    val number: Int,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val dateUpload: String,
    val dateFetch: String,
    val wordCount: Int = 0,
    val content: String? = null,
    val previousChapterId: String? = null,
    val nextChapterId: String? = null,
    val sourceId: String,
    val readingPosition: Int = 0,
    val readingProgress: Float = 0f,
    val totalWords: Int = 0,
    val lastRead: Long = 0,
    val readDuration: Long = 0
) {
    /**
     * Get formatted reading time based on word count
     */
    fun getFormattedReadingTime(): String {
        if (wordCount <= 0) return "< 1 min"

        val minutes = DateUtils.calculateReadingTimeMinutes(wordCount)
        return when {
            minutes < 1 -> "< 1 min"
            minutes < 60 -> "$minutes min"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "${hours}h ${remainingMinutes}m"
                } else {
                    "${hours}h"
                }
            }
        }
    }

    /**
     * Get reading progress as a percentage
     */
    fun getReadingProgressPercentage(): Int {
        return (readingProgress * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Format last read date
     */
    fun getFormattedLastRead(): String {
        if (lastRead <= 0) return "Never"
        return DateUtils.formatDate(lastRead)
    }

    /**
     * Format read duration
     */
    fun getFormattedReadDuration(): String {
        if (readDuration <= 0) return "0m"
        return DateUtils.formatDuration(readDuration)
    }

    /**
     * Get status badge text for this chapter
     */
    fun getStatusBadgeText(): String? {
        return when {
            isBookmarked && isNew() -> "New + Bookmark"
            isBookmarked -> "Bookmark"
            isNew() -> "New"
            else -> null
        }
    }

    /**
     * Check if chapter is considered new (recently uploaded)
     */
    private fun isNew(): Boolean {
        // A chapter is considered new if uploaded less than 3 days ago
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
        val uploadTimestamp = try {
            DateUtils.parseDate(dateUpload)
        } catch (e: Exception) {
            0L
        }

        return uploadTimestamp > threeDaysAgo
    }

    /**
     * Calculate reading progress based on position
     */
    fun calculateProgressFromPosition(position: Int, totalLength: Int): Float {
        if (totalLength <= 0) return 0f
        return (position.toFloat() / totalLength).coerceIn(0f, 1f)
    }

    /**
     * Get reading position as a formatted text
     */
    fun getFormattedReadingPosition(): String {
        val progressPercent = getReadingProgressPercentage()
        return "$progressPercent%"
    }
}