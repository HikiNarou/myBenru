package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to search for text in a chapter
 */
class SearchChapterContentUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<SearchChapterContentUseCase.Params, List<Int>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return List of positions where the query was found
     */
    override suspend fun execute(parameters: Params): List<Int> {
        // Validate search query
        if (parameters.query.isBlank()) {
            return emptyList()
        }

        return chapterRepository.searchInChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId,
            query = parameters.query
        )
    }

    /**
     * Parameters for [SearchChapterContentUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String,
        val query: String
    )
}