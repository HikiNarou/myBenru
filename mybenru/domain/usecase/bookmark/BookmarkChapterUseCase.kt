package com.mybenru.domain.usecase.bookmark

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to bookmark a chapter
 */
class BookmarkChapterUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<BookmarkChapterUseCase.Params, Chapter> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The updated chapter
     */
    override suspend fun execute(parameters: Params): Chapter {
        return chapterRepository.bookmarkChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId,
            bookmarked = parameters.bookmarked
        )
    }

    /**
     * Parameters for [BookmarkChapterUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String,
        val bookmarked: Boolean = true
    )
}