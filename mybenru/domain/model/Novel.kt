package com.mybenru.domain.model

import java.util.Date

/**
 * Domain model representing a novel
 */
data class Novel(
    val id: String,
    val sourceId: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val status: NovelStatus = NovelStatus.UNKNOWN,
    val rating: Float = 0.0f,
    val chaptersCount: Int = 0,
    val lastChapterDate: Date? = null,
    val initialized: Boolean = false,
    val isInLibrary: Boolean = false,
    val inLibraryAt: Long = 0L,
    val updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,
    val language: String? = null,
    val userCategories: List<String> = emptyList(),
    val readingProgress: Int = 0,
    val lastReadChapterId: String? = null,
    val lastReadChapterTitle: String? = null,
    val lastReadAt: Long = 0L,
    val userCover: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Enum representing the status of a novel
 */
enum class NovelStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    DROPPED,
    UNKNOWN;

    companion object {
        fun fromString(status: String?): NovelStatus {
            return when (status?.lowercase()) {
                "ongoing", "publishing" -> ONGOING
                "completed", "finished" -> COMPLETED
                "hiatus" -> HIATUS
                "dropped", "canceled" -> DROPPED
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Update strategy for novel details
 */
enum class UpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE
}