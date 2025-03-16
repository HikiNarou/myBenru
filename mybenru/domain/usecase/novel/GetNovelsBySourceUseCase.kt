package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Filter
import com.mybenru.domain.model.SearchResult
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get novels from a specific source
 */
class GetNovelsBySourceUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<GetNovelsBySourceUseCase.Params, SearchResult> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Search result containing novels from the source
     */
    override suspend fun execute(parameters: Params): SearchResult {
        // Create a source filter
        val sourceFilter = if (parameters.sourceId != null) {
            listOf(Filter.Custom("sourceId", parameters.sourceId)) + parameters.filters
        } else {
            parameters.filters
        }

        // Search novels with the given parameters
        return novelRepository.searchNovels(
            query = parameters.query,
            filters = sourceFilter,
            page = parameters.page
        )
    }

    /**
     * Parameters for [GetNovelsBySourceUseCase]
     */
    data class Params(
        val sourceId: String? = null,
        val query: String = "",
        val filters: List<Filter> = emptyList(),
        val page: Int = 1
    )
}