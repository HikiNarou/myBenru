package com.mybenru.domain.model

/**
 * Domain model representing a category of novels
 */
data class Category(
    val id: String,
    val name: String,
    val sourceId: String? = null,
    val novelsCount: Int = 0,
    val imageUrl: String? = null,
    val order: Int = 0,
    val isUserDefined: Boolean = false,
    val description: String? = null
)