package com.mybenru.domain.repository

import com.mybenru.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences
 */
interface UserPreferenceRepository {
    /**
     * Get the user's data
     *
     * @return Flow of user data
     */
    fun getUserData(): Flow<UserData>

    /**
     * Update the user's data
     *
     * @param userData The updated user data
     * @return The updated user data
     */
    suspend fun updateUserData(userData: UserData): UserData

    /**
     * Get the user's reading preferences
     *
     * @return The user's reading preferences
     */
    suspend fun getReaderSettings(): ReaderSettings

    /**
     * Update the user's reading preferences
     *
     * @param readerSettings The updated reading preferences
     */
    suspend fun updateReaderSettings(readerSettings: ReaderSettings)

    /**
     * Get all reader themes
     *
     * @return List of reader themes
     */
    suspend fun getReaderThemes(): List<ReaderTheme>

    /**
     * Get a reader theme by ID
     *
     * @param themeId The ID of the theme
     * @return The requested theme, or null if not found
     */
    suspend fun getReaderTheme(themeId: String): ReaderTheme?

    /**
     * Create a custom reader theme
     *
     * @param theme The theme to create
     * @return The created theme
     */
    suspend fun createReaderTheme(theme: ReaderTheme): ReaderTheme

    /**
     * Update a reader theme
     *
     * @param theme The theme to update
     * @return The updated theme
     */
    suspend fun updateReaderTheme(theme: ReaderTheme): ReaderTheme

    /**
     * Delete a reader theme
     *
     * @param themeId The ID of the theme to delete
     * @return True if the theme was deleted, false otherwise
     */
    suspend fun deleteReaderTheme(themeId: String): Boolean

    /**
     * Get the user's app theme preference
     *
     * @return The app theme preference
     */
    suspend fun getAppTheme(): String

    /**
     * Set the user's app theme preference
     *
     * @param theme The app theme preference
     */
    suspend fun setAppTheme(theme: String)

    /**
     * Get the user's language preference
     *
     * @return The language preference
     */
    suspend fun getLanguage(): String

    /**
     * Set the user's language preference
     *
     * @param language The language preference
     */
    suspend fun setLanguage(language: String)

    /**
     * Check if notifications are enabled
     *
     * @return True if notifications are enabled, false otherwise
     */
    suspend fun isNotificationsEnabled(): Boolean

    /**
     * Set notifications enabled state
     *
     * @param enabled Whether notifications are enabled
     */
    suspend fun setNotificationsEnabled(enabled: Boolean)

    /**
     * Get the library update frequency
     *
     * @return The library update frequency
     */
    suspend fun getLibraryUpdateFrequency(): LibraryUpdateFrequency

    /**
     * Set the library update frequency
     *
     * @param frequency The library update frequency
     */
    suspend fun setLibraryUpdateFrequency(frequency: LibraryUpdateFrequency)

    /**
     * Check if automatic downloads are enabled
     *
     * @return True if automatic downloads are enabled, false otherwise
     */
    suspend fun isAutoDownloadEnabled(): Boolean

    /**
     * Set automatic downloads enabled state
     *
     * @param enabled Whether automatic downloads are enabled
     */
    suspend fun setAutoDownloadEnabled(enabled: Boolean)

    /**
     * Check if downloads are restricted to WiFi
     *
     * @return True if downloads are restricted to WiFi, false otherwise
     */
    suspend fun isDownloadOnlyOverWifi(): Boolean

    /**
     * Set downloads restricted to WiFi
     *
     * @param wifiOnly Whether downloads are restricted to WiFi
     */
    suspend fun setDownloadOnlyOverWifi(wifiOnly: Boolean)

    /**
     * Get the maximum concurrent downloads
     *
     * @return The maximum concurrent downloads
     */
    suspend fun getMaxConcurrentDownloads(): Int

    /**
     * Set the maximum concurrent downloads
     *
     * @param max The maximum concurrent downloads
     */
    suspend fun setMaxConcurrentDownloads(max: Int)

    /**
     * Get the download location
     *
     * @return The download location
     */
    suspend fun getDownloadLocation(): String?

    /**
     * Set the download location
     *
     * @param location The download location
     */
    suspend fun setDownloadLocation(location: String?)

    /**
     * Check if incognito mode is enabled
     *
     * @return True if incognito mode is enabled, false otherwise
     */
    suspend fun isIncognitoModeEnabled(): Boolean

    /**
     * Set incognito mode enabled state
     *
     * @param enabled Whether incognito mode is enabled
     */
    suspend fun setIncognitoModeEnabled(enabled: Boolean)

    /**
     * Get the date format preference
     *
     * @return The date format preference
     */
    suspend fun getDateFormat(): String

    /**
     * Set the date format preference
     *
     * @param format The date format preference
     */
    suspend fun setDateFormat(format: String)

    /**
     * Get the time format preference
     *
     * @return The time format preference
     */
    suspend fun getTimeFormat(): String

    /**
     * Set the time format preference
     *
     * @param format The time format preference
     */
    suspend fun setTimeFormat(format: String)

    /**
     * Get the default reading position for new chapters
     *
     * @return The default reading position (0.0-1.0)
     */
    suspend fun getDefaultReadingPosition(): Float

    /**
     * Set the default reading position for new chapters
     *
     * @param position The default reading position (0.0-1.0)
     */
    suspend fun setDefaultReadingPosition(position: Float)

    /**
     * Get the default font size for the reader
     *
     * @return The default font size
     */
    suspend fun getDefaultFontSize(): Int

    /**
     * Set the default font size for the reader
     *
     * @param size The default font size
     */
    suspend fun setDefaultFontSize(size: Int)

    /**
     * Get the default line height for the reader
     *
     * @return The default line height
     */
    suspend fun getDefaultLineHeight(): Float

    /**
     * Set the default line height for the reader
     *
     * @param height The default line height
     */
    suspend fun setDefaultLineHeight(height: Float)

    /**
     * Get the default text alignment for the reader
     *
     * @return The default text alignment
     */
    suspend fun getDefaultTextAlignment(): TextAlignment

    /**
     * Set the default text alignment for the reader
     *
     * @param alignment The default text alignment
     */
    suspend fun setDefaultTextAlignment(alignment: TextAlignment)

    /**
     * Check if the app should keep the screen on during reading
     *
     * @return True if the screen should be kept on during reading, false otherwise
     */
    suspend fun isKeepScreenOnDuringReading(): Boolean

    /**
     * Set whether the app should keep the screen on during reading
     *
     * @param keepOn Whether the screen should be kept on during reading
     */
    suspend fun setKeepScreenOnDuringReading(keepOn: Boolean)

    /**
     * Clear all preferences
     *
     * @return True if preferences were cleared, false otherwise
     */
    suspend fun clearAllPreferences(): Boolean

    /**
     * Reset preferences to default values
     *
     * @return True if preferences were reset, false otherwise
     */
    suspend fun resetPreferencesToDefaults(): Boolean
}