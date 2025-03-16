package com.mybenru.domain.usecase.download

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.DownloadTask
import com.mybenru.domain.repository.DownloadRepository
import javax.inject.Inject

/**
 * Use case to cancel a download task
 */
class CancelDownloadTaskUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) : UseCase<String, DownloadTask> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the task to cancel
     * @return The updated download task
     */
    override suspend fun execute(parameters: String): DownloadTask {
        return downloadRepository.cancelDownloadTask(parameters)
    }
}