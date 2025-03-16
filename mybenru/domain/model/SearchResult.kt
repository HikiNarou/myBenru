package com.mybenru.domain.model

/**
 * Domain model representing a search result
 */
data class SearchResult(
    val novels: List<Novel>,
    val hasNextPage: Boolean,
    val currentPage: Int,
    val totalPages: Int,
    val totalResults: Int,
    val sourceId: String?
)