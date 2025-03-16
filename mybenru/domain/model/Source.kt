package com.mybenru.domain.model

/**
 * Domain model representing a source of novels
 */
data class Source(
    val id: String,
    val name: String,
    val baseUrl: String,
    val iconUrl: String? = null,
    val language: String = "en",
    val isEnabled: Boolean = true,
    val supportsLatest: Boolean = true,
    val supportsFindNovels: Boolean = true,
    val supportsNovelStatus: Boolean = true,
    val description: String? = null,
    val prefixUrl: String = "",
    val parser: ParserType = ParserType.HTML
)

/**
 * Type of parser used for extracting data from sources
 */
enum class ParserType {
    HTML,
    JSON,
    RSS,
    API
}