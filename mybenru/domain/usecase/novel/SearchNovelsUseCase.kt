package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Filter
import com.mybenru.domain.model.SearchResult
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to search for novels
 */
class SearchNovelsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<SearchNovelsUseCase.Params, SearchResult> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Search result containing matching novels
     */
    override suspend fun execute(parameters: Params): SearchResult {
        return novelRepository.searchNovels(
            query = parameters.query,
            filters = parameters.filters,
            page = parameters.page
        )
    }

    /**
     * Parameters for [SearchNovelsUseCase]
     */
    data class Params(
        val query: String,
        val filters: List<Filter> = emptyList(),
        val page: Int = 1
    )
}