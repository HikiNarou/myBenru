package com.mybenru.domain.usecase.backup

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.BackupRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case to create a backup of the user's data
 */
class CreateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) : UseCase<CreateBackupUseCase.Params, File> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The created backup file
     */
    override suspend fun execute(parameters: Params): File {
        return backupRepository.createBackup(
            includeNovels = parameters.includeNovels,
            includeChapters = parameters.includeChapters,
            includeDownloads = parameters.includeDownloads,
            includePreferences = parameters.includePreferences,
            password = parameters.password
        )
    }

    /**
     * Parameters for [CreateBackupUseCase]
     */
    data class Params(
        val includeNovels: Boolean = true,
        val includeChapters: Boolean = true,
        val includeDownloads: Boolean = false,
        val includePreferences: Boolean = true,
        val password: String? = null
    )
}