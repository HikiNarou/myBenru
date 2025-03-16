package com.mybenru.domain.usecase.download

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to delete a downloaded chapter
 */
class DeleteDownloadedChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<DeleteDownloadedChapterUseCase.Params, Boolean> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return True if the chapter was deleted, false otherwise
     */
    override suspend fun execute(parameters: Params): Boolean {
        return chapterRepository.deleteDownloadedChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId
        )
    }

    /**
     * Parameters for [DeleteDownloadedChapterUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String
    )
}