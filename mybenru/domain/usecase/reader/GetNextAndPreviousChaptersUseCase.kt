package com.mybenru.domain.usecase.reader

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to get the next and previous chapters
 */
class GetNextAndPreviousChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<GetNextAndPreviousChaptersUseCase.Params, GetNextAndPreviousChaptersUseCase.Result> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Result containing the next and previous chapters
     */
    override suspend fun execute(parameters: Params): Result {
        // Get the next chapter
        val nextChapter = chapterRepository.getNextChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId
        )

        // Get the previous chapter
        val previousChapter = chapterRepository.getPreviousChapter(
            chapterId = parameters.chapterId,
            novelId = parameters.novelId
        )

        // Return the result
        return Result(
            nextChapter = nextChapter,
            previousChapter = previousChapter
        )
    }

    /**
     * Parameters for [GetNextAndPreviousChaptersUseCase]
     */
    data class Params(
        val chapterId: String,
        val novelId: String
    )

    /**
     * Result of [GetNextAndPreviousChaptersUseCase]
     */
    data class Result(
        val nextChapter: Chapter?,
        val previousChapter: Chapter?
    )
}