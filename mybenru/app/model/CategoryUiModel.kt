package com.mybenru.app.model

/**
 * UI model for representing a novel category in the presentation layer
 */
data class CategoryUiModel(
    val id: String,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val novelCount: Int = 0
)