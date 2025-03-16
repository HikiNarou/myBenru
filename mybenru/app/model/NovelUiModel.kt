package com.mybenru.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * UI model for representing a novel in the presentation layer.
 * (Hasil penggabungan dua versi NovelUiModel)
 */
data class NovelUiModel(
    val id: String,
    val sourceId: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val authors: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val status: String = "Unknown",
    // Menggunakan tipe Float untuk rating (default 0.0f)
    val rating: Float = 0.0f,
    val isInLibrary: Boolean = false,
    // Menggabungkan totalChapters (Bagian 1) dan chapterCount (Bagian 2)
    val totalChapters: Int = 0,
    // Menggunakan versi Bagian 1 untuk lastReadChapter dan lastReadChapterTitle
    val lastReadChapter: Int? = null,
    val lastReadChapterTitle: String? = null,
    // Menggabungkan unreadChaptersCount (Bagian 1) dan unreadChapterCount (Bagian 2)
    val unreadChaptersCount: Int = 0,
    val updateCount: Int = 0,
    // Menggabungkan lastUpdated (Bagian 1) dan lastUpdatedTimestamp (Bagian 2)
    val lastUpdated: Long = 0,
    val downloadedChapters: Int = 0,
    val readChapters: Int = 0,
    val readingProgress: Float = 0f,
    // Properti dateAddedToLibrary sama pada kedua bagian
    val dateAddedToLibrary: Long = 0,
    val alternativeTitles: List<String> = emptyList(),
    val language: String? = null,
    val popularity: Int = 0,
    val contentRating: String = "Unknown",
    val yearOfRelease: Int? = null,
    val tags: List<String> = emptyList()
) {

    /**
     * Format authors into a readable string.
     */
    fun getFormattedAuthors(): String {
        return when {
            authors.isEmpty() -> "Unknown Author"
            authors.size == 1 -> authors.first()
            else -> authors.joinToString(", ")
        }
    }

    /**
     * Format genres into a readable string.
     */
    fun getFormattedGenres(): String {
        return when {
            genres.isEmpty() -> "No Genres"
            else -> genres.joinToString(", ")
        }
    }

    /**
     * Get progress as a percentage based on readChapters and totalChapters.
     */
    fun getProgressPercentage(): Int {
        if (totalChapters <= 0) return 0
        return ((readChapters.toFloat() / totalChapters) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Get formatted progress text.
     */
    fun getFormattedProgress(): String {
        return if (totalChapters > 0) {
            "$readChapters/$totalChapters"
        } else {
            "$readChapters/?"
        }
    }

    /**
     * Check if the novel has been started by the user.
     */
    fun hasBeenStarted(): Boolean {
        return lastReadChapter != null
    }

    /**
     * Get badge text for updates if any.
     */
    fun getUpdateBadgeText(): String? {
        return if (updateCount > 0) {
            "+$updateCount"
        } else {
            null
        }
    }

    /**
     * Format the last updated date into a readable string.
     */
    fun getFormattedLastUpdated(): String {
        if (lastUpdated <= 0) return "Never"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.format(Date(lastUpdated))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Format date added to library into a readable string.
     */
    fun getFormattedDateAdded(): String {
        if (dateAddedToLibrary <= 0) return "Not in Library"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.format(Date(dateAddedToLibrary))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Get a short description (first 100 characters).
     */
    fun getShortDescription(): String {
        return if (description.length > 100) {
            description.substring(0, 100) + "..."
        } else {
            description
        }
    }

    /**
     * Calculate reading completion percentage based on readingProgress.
     */
    fun getReadingCompletionPercentage(): Int {
        return (readingProgress * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Format reading progress as a percentage string.
     */
    fun getReadingProgressPercentage(): String {
        val percentage = getReadingCompletionPercentage()
        return "$percentage%"
    }

    /**
     * Check if novel has any downloaded chapters.
     */
    fun hasDownloads(): Boolean {
        return downloadedChapters > 0
    }

    /**
     * Check if novel is fully downloaded.
     */
    fun isFullyDownloaded(): Boolean {
        return totalChapters > 0 && downloadedChapters >= totalChapters
    }

    /**
     * Check if novel is fully read.
     */
    fun isFullyRead(): Boolean {
        return totalChapters > 0 && readChapters >= totalChapters
    }

    /**
     * Get download status text.
     */
    fun getDownloadStatusText(): String {
        return when {
            downloadedChapters <= 0 -> "Not Downloaded"
            isFullyDownloaded() -> "Fully Downloaded"
            else -> "$downloadedChapters Downloaded"
        }
    }

    /**
     * Get alternative titles as a formatted string.
     */
    fun getFormattedAlternativeTitles(): String {
        return if (alternativeTitles.isEmpty()) "" else alternativeTitles.joinToString(", ")
    }

    /**
     * Get tags as a formatted string.
     */
    fun getFormattedTags(): String {
        return if (tags.isEmpty()) "No Tags" else tags.joinToString(", ")
    }
}
