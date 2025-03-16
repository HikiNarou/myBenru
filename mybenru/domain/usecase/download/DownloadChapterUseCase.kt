package com.mybenru.domain.usecase.download

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to download a chapter
 */
class DownloadChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<DownloadChapterUseCase.Params, Chapter> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The downloaded chapter
     */
    override suspend fun execute(parameters: Params): Chapter {
        return chapterRepository.downloadChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId
        )
    }

    /**
     * Parameters for [DownloadChapterUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String
    )
}