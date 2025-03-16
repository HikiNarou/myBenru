package com.mybenru.domain.model

/**
 * Domain model representing a user-defined library category
 */
data class LibraryCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val order: Int,
    val isDefaultCategory: Boolean = false,
    val novelIds: List<String> = emptyList()
)