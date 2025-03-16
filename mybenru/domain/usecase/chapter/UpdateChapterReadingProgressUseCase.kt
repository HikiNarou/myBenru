package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to update the reading progress of a chapter
 */
class UpdateChapterReadingProgressUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<UpdateChapterReadingProgressUseCase.Params, Chapter> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The updated chapter
     */
    override suspend fun execute(parameters: Params): Chapter {
        return chapterRepository.updateChapterReadingProgress(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId,
            progress = parameters.progress,
            position = parameters.position
        )
    }

    /**
     * Parameters for [UpdateChapterReadingProgressUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String,
        val progress: Float,
        val position: Int
    )
}