package com.mybenru.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI Model for Novel Source displaying in the app
 * (Gabungan dari kedua versi)
 */
@Parcelize
data class SourceUiModel(
    val id: String,
    val name: String,
    // Menggabungkan properti 'lang' (versi 1) dan 'language' (versi 2)
    val language: String,
    val iconUrl: String? = null,
    val baseUrl: String,
    val version: String,
    val isEnabled: Boolean = true,
    val supportsLatest: Boolean = false,
    val supportsSearch: Boolean = true,
    // Menggantikan 'supportsFindNavels' (versi 1) dengan 'supportsBrowse' (versi 2)
    val supportsBrowse: Boolean = true,
    // Properti tambahan dari versi 2
    val supportsFilters: Boolean = false,
    // Properti tambahan dari versi 1
    val supportsChapterReading: Boolean = false,
    val isNsfw: Boolean = false,
    // Properti tambahan dari versi 2
    val isPaid: Boolean = false
) : Parcelable {

    /**
     * Get language display name dengan menggabungkan mapping dari kedua versi.
     */
    fun getLanguageDisplayName(): String {
        return when (language.lowercase()) {
            "en" -> "English"
            "fr" -> "Français"
            "es" -> "Español"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "pt" -> "Português"
            "ru" -> "Русский"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "zh" -> "中文"
            "multi" -> "Multiple"
            else -> language
        }
    }

    /**
     * Get features supported by this source dengan menggabungkan fitur dari kedua versi.
     */
    fun getFeatures(): List<String> {
        val features = mutableListOf<String>()
        if (supportsLatest) features.add("Latest Updates")
        if (supportsSearch) features.add("Search")
        if (supportsBrowse) features.add("Browse Catalog")
        if (supportsFilters) features.add("Filters")
        if (isPaid) features.add("Paid")
        if (supportsChapterReading) features.add("Chapter Reading")
        return features
    }
}
