package com.mybenru.app.model

/**
 * UI model for chapter navigation
 */
data class ChapterNavigationUiModel(
    val novelId: String = "",
    val chapterId: String = "",
    val chapterNumber: Int = 0,
    val chapterTitle: String? = null,
    val novelTitle: String = "",
    val hasPreviousChapter: Boolean = false,
    val hasNextChapter: Boolean = false,
    val previousChapterNumber: Int? = null,
    val nextChapterNumber: Int? = null
)