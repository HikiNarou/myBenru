package com.mybenru.domain.usecase.search

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get search suggestions based on user input
 */
class GetSearchSuggestionsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<GetSearchSuggestionsUseCase.Params, List<Novel>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return List of suggested novels
     */
    override suspend fun execute(parameters: Params): List<Novel> {
        // If the query is empty, return recent novels
        if (parameters.query.isEmpty()) {
            return novelRepository.getRecentlyReadNovels(10)
        }

        // Search for novels that match the query
        val results = novelRepository.searchNovels(
            query = parameters.query,
            page = 1
        )

        // Return the results, limited to the specified maximum
        return results.novels.take(parameters.maxResults)
    }

    /**
     * Parameters for [GetSearchSuggestionsUseCase]
     */
    data class Params(
        val query: String,
        val maxResults: Int = 5
    )
}