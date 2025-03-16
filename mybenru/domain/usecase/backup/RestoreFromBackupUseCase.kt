package com.mybenru.domain.usecase.backup

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.BackupRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case to restore from a backup file
 */
class RestoreFromBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) : UseCase<RestoreFromBackupUseCase.Params, Int> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The number of items restored
     */
    override suspend fun execute(parameters: Params): Int {
        return backupRepository.restoreFromBackup(
            backupFile = parameters.backupFile,
            password = parameters.password,
            restoreNovels = parameters.restoreNovels,
            restoreChapters = parameters.restoreChapters,
            restoreDownloads = parameters.restoreDownloads,
            restorePreferences = parameters.restorePreferences
        )
    }

    /**
     * Parameters for [RestoreFromBackupUseCase]
     */
    data class Params(
        val backupFile: File,
        val password: String? = null,
        val restoreNovels: Boolean = true,
        val restoreChapters: Boolean = true,
        val restoreDownloads: Boolean = true,
        val restorePreferences: Boolean = true
    )
}