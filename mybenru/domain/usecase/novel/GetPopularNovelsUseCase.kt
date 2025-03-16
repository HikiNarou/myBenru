package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.SearchResult
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get popular novels
 */
class GetPopularNovelsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<GetPopularNovelsUseCase.Params, SearchResult> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Search result containing popular novels
     */
    override suspend fun execute(parameters: Params): SearchResult {
        return novelRepository.getPopularNovels(
            sourceId = parameters.sourceId,
            page = parameters.page
        )
    }

    /**
     * Parameters for [GetPopularNovelsUseCase]
     */
    data class Params(
        val sourceId: String? = null,
        val page: Int = 1
    )
}