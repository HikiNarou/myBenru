package com.mybenru.domain.model

/**
 * Domain model representing detailed information of a chapter
 */
data class ChapterDetail(
    val chapter: Chapter,
    val novel: Novel
)