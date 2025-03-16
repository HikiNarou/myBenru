package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to get chapter content
 */
class GetChapterContentUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<GetChapterContentUseCase.Params, Chapter> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The chapter with content
     */
    override suspend fun execute(parameters: Params): Chapter {
        return chapterRepository.getChapterContent(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId,
            forceRefresh = parameters.forceRefresh
        )
    }

    /**
     * Parameters for [GetChapterContentUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String,
        val forceRefresh: Boolean = false
    )
}