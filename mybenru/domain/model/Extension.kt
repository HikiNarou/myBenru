package com.mybenru.domain.model

/**
 * Domain model representing an extension
 */
data class Extension(
    val id: String,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val language: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val isNsfw: Boolean = false,
    val isInstalled: Boolean = false,
    val hasUpdate: Boolean = false,
    val installedVersionCode: Int = 0,
    val sources: List<Source> = emptyList(),
    val author: String? = null,
    val website: String? = null,
    val isEnabled: Boolean = true,
    val installDate: Long = 0,
    val updateDate: Long = 0,
    val downloadCount: Int = 0,
    val ratings: Float = 0f,
    val settings: Map<String, Any> = emptyMap()
)