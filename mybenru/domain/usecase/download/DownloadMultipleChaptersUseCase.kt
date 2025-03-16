package com.mybenru.domain.usecase.download

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to download multiple chapters
 */
class DownloadMultipleChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<DownloadMultipleChaptersUseCase.Params, Int> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The number of successfully downloaded chapters
     */
    override suspend fun execute(parameters: Params): Int {
        return chapterRepository.downloadChapters(
            chapterIds = parameters.chapterIds,
            novelId = parameters.novelId
        )
    }

    /**
     * Parameters for [DownloadMultipleChaptersUseCase]
     */
    data class Params(
        val chapterIds: List<String>,
        val novelId: String
    )
}