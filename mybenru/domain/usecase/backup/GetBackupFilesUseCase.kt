package com.mybenru.domain.usecase.backup

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.repository.BackupRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

/**
 * Use case to get all backup files
 */
class GetBackupFilesUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) : NoParamsUseCase<Flow<List<File>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of backup files
     */
    override suspend fun execute(): Flow<List<File>> {
        return backupRepository.getBackupFiles()
    }
}