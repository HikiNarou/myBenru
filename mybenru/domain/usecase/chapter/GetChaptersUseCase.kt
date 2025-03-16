package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to get all chapters for a novel
 */
class GetChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<GetChaptersUseCase.Params, List<Chapter>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return List of chapters for the novel
     */
    override suspend fun execute(parameters: Params): List<Chapter> {
        return chapterRepository.getChapters(
            novelId = parameters.novelId,
            forceRefresh = parameters.forceRefresh
        )
    }

    /**
     * Parameters for [GetChaptersUseCase]
     */
    data class Params(
        val novelId: String,
        val forceRefresh: Boolean = false
    )
}