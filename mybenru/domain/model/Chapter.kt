package com.mybenru.domain.model

import java.util.Date

/**
 * Domain model representing a chapter of a novel
 */
data class Chapter(
    val id: String,
    val url: String,
    val title: String,
    val number: Float,
    val novelId: String,
    val uploadDate: Date? = null,
    val wordCount: Int = 0,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val readingProgress: Float = 0f,
    val lastReadPosition: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val hasContent: Boolean = false,
    val content: String? = null,
    val prevChapterId: String? = null,
    val nextChapterId: String? = null
)