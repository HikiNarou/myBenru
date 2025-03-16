package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to mark a chapter as read
 */
class MarkChapterReadUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<MarkChapterReadUseCase.Params, Chapter> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The updated chapter
     */
    override suspend fun execute(parameters: Params): Chapter {
        return chapterRepository.markChapterRead(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId,
            read = parameters.read
        )
    }

    /**
     * Parameters for [MarkChapterReadUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String,
        val read: Boolean = true
    )
}