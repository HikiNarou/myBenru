package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to fetch novel details from a URL
 */
class FetchNovelFromUrlUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<FetchNovelFromUrlUseCase.Params, Novel> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The fetched novel
     */
    override suspend fun execute(parameters: Params): Novel {
        // Validate the URL format
        validateUrl(parameters.url)

        // Fetch novel from URL
        return novelRepository.fetchNovelFromUrl(
            url = parameters.url,
            sourceId = parameters.sourceId
        )
    }

    /**
     * Validate the URL format
     *
     * @param url The URL to validate
     * @throws IllegalArgumentException if the URL is invalid
     */
    private fun validateUrl(url: String) {
        // Check if URL is not empty
        if (url.isBlank()) {
            throw IllegalArgumentException("URL cannot be empty")
        }

        // Check if URL has proper format
        val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")
        if (!urlRegex.matches(url)) {
            throw IllegalArgumentException("Invalid URL format")
        }
    }

    /**
     * Parameters for [FetchNovelFromUrlUseCase]
     */
    data class Params(
        val url: String,
        val sourceId: String? = null
    )
}