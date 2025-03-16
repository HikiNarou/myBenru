package com.mybenru.domain.repository

import com.mybenru.domain.model.BackupFrequency
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Date

/**
 * Repository interface for backup operations
 */
interface BackupRepository {
    /**
     * Create a backup of the user's data
     *
     * @param includeNovels Whether to include novels in the backup
     * @param includeChapters Whether to include chapters in the backup
     * @param includeDownloads Whether to include downloaded content in the backup
     * @param includePreferences Whether to include user preferences in the backup
     * @param password Optional password to encrypt the backup
     * @return The created backup file
     */
    suspend fun createBackup(
        includeNovels: Boolean = true,
        includeChapters: Boolean = true,
        includeDownloads: Boolean = false,
        includePreferences: Boolean = true,
        password: String? = null
    ): File

    /**
     * Restore from a backup file
     *
     * @param backupFile The backup file to restore from
     * @param password Optional password to decrypt the backup
     * @param restoreNovels Whether to restore novels
     * @param restoreChapters Whether to restore chapters
     * @param restoreDownloads Whether to restore downloaded content
     * @param restorePreferences Whether to restore user preferences
     * @return The number of items restored
     */
    suspend fun restoreFromBackup(
        backupFile: File,
        password: String? = null,
        restoreNovels: Boolean = true,
        restoreChapters: Boolean = true,
        restoreDownloads: Boolean = true,
        restorePreferences: Boolean = true
    ): Int

    /**
     * Get all backup files
     *
     * @return Flow of backup files
     */
    fun getBackupFiles(): Flow<List<File>>

    /**
     * Delete a backup file
     *
     * @param backupFile The backup file to delete
     * @return True if the file was deleted, false otherwise
     */
    suspend fun deleteBackup(backupFile: File): Boolean

    /**
     * Get the creation date of a backup file
     *
     * @param backupFile The backup file
     * @return The creation date of the backup file
     */
    suspend fun getBackupCreationDate(backupFile: File): Date

    /**
     * Check if automatic backups are enabled
     *
     * @return True if automatic backups are enabled, false otherwise
     */
    suspend fun isAutoBackupEnabled(): Boolean

    /**
     * Set automatic backup enabled state
     *
     * @param enabled Whether automatic backups are enabled
     */
    suspend fun setAutoBackupEnabled(enabled: Boolean)

    /**
     * Get the automatic backup frequency
     *
     * @return The automatic backup frequency
     */
    suspend fun getAutoBackupFrequency(): BackupFrequency

    /**
     * Set the automatic backup frequency
     *
     * @param frequency The automatic backup frequency
     */
    suspend fun setAutoBackupFrequency(frequency: BackupFrequency)

    /**
     * Get the date of the next automatic backup
     *
     * @return The date of the next automatic backup, or null if automatic backup is disabled
     */
    suspend fun getNextAutoBackupDate(): Date?

    /**
     * Export novels to a text file
     *
     * @param novelIds The IDs of the novels to export
     * @param format The format to export in (e.g., "txt", "html", "epub")
     * @return The exported file
     */
    suspend fun exportNovels(novelIds: List<String>, format: String): File

    /**
     * Validate a backup file
     *
     * @param backupFile The backup file to validate
     * @param password Optional password to decrypt the backup
     * @return True if the backup file is valid, false otherwise
     */
    suspend fun validateBackupFile(backupFile: File, password: String? = null): Boolean
}