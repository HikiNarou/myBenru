package com.mybenru.domain.model

/**
 * Domain model representing user data
 */
data class UserData(
    val id: String,
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val isLoggedIn: Boolean = false,
    val lastLoginAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val settings: UserSettings = UserSettings()
)

/**
 * Domain model representing user settings
 */
data class UserSettings(
    val libraryUpdateFrequency: LibraryUpdateFrequency = LibraryUpdateFrequency.DAILY,
    val defaultReaderSettings: ReaderSettings = ReaderSettings(),
    val notificationsEnabled: Boolean = true,
    val autoDownloadChapters: Boolean = false,
    val downloadOnlyOverWifi: Boolean = true,
    val maxConcurrentDownloads: Int = 3,
    val downloadLocation: String? = null,
    val autoBackupEnabled: Boolean = true,
    val backupFrequency: BackupFrequency = BackupFrequency.WEEKLY,
    val backupLocation: String? = null,
    val incognitoMode: Boolean = false,
    val dateFormat: String = "yyyy-MM-dd",
    val timeFormat: String = "HH:mm"
)

/**
 * Library update frequency options
 */
enum class LibraryUpdateFrequency {
    MANUAL,
    HOURLY,
    EVERY_SIX_HOURS,
    DAILY,
    EVERY_TWO_DAYS,
    WEEKLY
}

/**
 * Backup frequency options
 */
enum class BackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}