package com.mybenru.domain.repository

import com.mybenru.domain.model.Extension
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for extension operations
 */
interface ExtensionRepository {
    /**
     * Get all available extensions
     *
     * @return Flow of all extensions
     */
    fun getAllExtensions(): Flow<List<Extension>>

    /**
     * Get an extension by its ID
     *
     * @param extensionId The ID of the extension
     * @return The requested extension, or null if not found
     */
    suspend fun getExtension(extensionId: String): Extension?

    /**
     * Get all installed extensions
     *
     * @return List of installed extensions
     */
    suspend fun getInstalledExtensions(): List<Extension>

    /**
     * Get all available updates for extensions
     *
     * @return List of extensions with available updates
     */
    suspend fun getAvailableExtensionUpdates(): List<Extension>

    /**
     * Install an extension
     *
     * @param extensionId The ID of the extension to install
     * @return The installed extension
     */
    suspend fun installExtension(extensionId: String): Extension

    /**
     * Uninstall an extension
     *
     * @param extensionId The ID of the extension to uninstall
     * @return True if the extension was uninstalled, false otherwise
     */
    suspend fun uninstallExtension(extensionId: String): Boolean

    /**
     * Update an extension
     *
     * @param extensionId The ID of the extension to update
     * @return The updated extension
     */
    suspend fun updateExtension(extensionId: String): Extension

    /**
     * Check if an extension is installed
     *
     * @param extensionId The ID of the extension
     * @return True if the extension is installed, false otherwise
     */
    suspend fun isExtensionInstalled(extensionId: String): Boolean

    /**
     * Search for extensions
     *
     * @param query The search query
     * @return List of matching extensions
     */
    suspend fun searchExtensions(query: String): List<Extension>

    /**
     * Get extensions by language
     *
     * @param language The language code (e.g., "en", "ja")
     * @return List of extensions in the specified language
     */
    suspend fun getExtensionsByLanguage(language: String): List<Extension>

    /**
     * Enable or disable an extension
     *
     * @param extensionId The ID of the extension
     * @param enabled Whether the extension is enabled
     * @return The updated extension
     */
    suspend fun setExtensionEnabled(extensionId: String, enabled: Boolean): Extension

    /**
     * Install extension from a file
     *
     * @param filePath The path to the extension file
     * @return The installed extension
     */
    suspend fun installExtensionFromFile(filePath: String): Extension

    /**
     * Get extension settings
     *
     * @param extensionId The ID of the extension
     * @return Map of extension settings
     */
    suspend fun getExtensionSettings(extensionId: String): Map<String, Any>

    /**
     * Update extension settings
     *
     * @param extensionId The ID of the extension
     * @param settings Map of extension settings
     * @return True if settings were updated, false otherwise
     */
    suspend fun updateExtensionSettings(extensionId: String, settings: Map<String, Any>): Boolean
}