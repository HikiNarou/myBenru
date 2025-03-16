package com.mybenru.domain.usecase.download

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.DownloadTask
import com.mybenru.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all download tasks
 */
class GetDownloadTasksUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) : NoParamsUseCase<Flow<List<DownloadTask>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of all download tasks
     */
    override suspend fun execute(): Flow<List<DownloadTask>> {
        return downloadRepository.getAllDownloadTasks()
    }
}