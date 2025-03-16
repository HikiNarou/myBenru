package com.mybenru.domain.repository

import com.mybenru.domain.model.Source
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for source operations
 */
interface SourceRepository {
    /**
     * Get all available sources
     *
     * @return Flow of all sources
     */
    fun getAllSources(): Flow<List<Source>>

    /**
     * Get a source by its ID
     *
     * @param sourceId The ID of the source
     * @return The requested source, or null if not found
     */
    suspend fun getSource(sourceId: String): Source?

    /**
     * Add a new source
     *
     * @param source The source to add
     * @return The added source
     */
    suspend fun addSource(source: Source): Source

    /**
     * Update an existing source
     *
     * @param source The source to update
     * @return The updated source
     */
    suspend fun updateSource(source: Source): Source

    /**
     * Delete a source
     *
     * @param sourceId The ID of the source to delete
     * @return True if the source was deleted, false otherwise
     */
    suspend fun deleteSource(sourceId: String): Boolean

    /**
     * Enable or disable a source
     *
     * @param sourceId The ID of the source
     * @param enabled Whether the source is enabled
     * @return The updated source
     */
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean): Source

    /**
     * Get all enabled sources
     *
     * @return List of enabled sources
     */
    suspend fun getEnabledSources(): List<Source>

    /**
     * Get all disabled sources
     *
     * @return List of disabled sources
     */
    suspend fun getDisabledSources(): List<Source>

    /**
     * Search for sources by name
     *
     * @param query The search query
     * @return List of matching sources
     */
    suspend fun searchSources(query: String): List<Source>

    /**
     * Get sources by language
     *
     * @param language The language code (e.g., "en", "ja")
     * @return List of sources in the specified language
     */
    suspend fun getSourcesByLanguage(language: String): List<Source>

    /**
     * Get sources that support a specific feature
     *
     * @param supportsLatest Whether the source supports latest novels
     * @param supportsFindNovels Whether the source supports finding novels
     * @param supportsNovelStatus Whether the source supports novel status
     * @return List of sources that support all specified features
     */
    suspend fun getSourcesByFeature(
        supportsLatest: Boolean? = null,
        supportsFindNovels: Boolean? = null,
        supportsNovelStatus: Boolean? = null
    ): List<Source>

    /**
     * Check if a source is available
     *
     * @param sourceId The ID of the source
     * @return True if the source is available, false otherwise
     */
    suspend fun isSourceAvailable(sourceId: String): Boolean

    /**
     * Install a source extension
     *
     * @param extensionId The ID of the extension
     * @param sourceId The ID of the source
     * @return The installed source
     */
    suspend fun installSourceExtension(extensionId: String, sourceId: String): Source

    /**
     * Uninstall a source extension
     *
     * @param sourceId The ID of the source
     * @return True if the extension was uninstalled, false otherwise
     */
    suspend fun uninstallSourceExtension(sourceId: String): Boolean

    /**
     * Add a source from URL
     *
     * @param url The URL of the source
     * @return The added source
     */
    suspend fun addSourceFromUrl(url: String): Source
}