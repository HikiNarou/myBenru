package com.mybenru.domain.usecase.source

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Source
import com.mybenru.domain.repository.SourceRepository
import javax.inject.Inject

/**
 * Use case to add a source from URL
 */
class AddSourceFromUrlUseCase @Inject constructor(
    private val sourceRepository: SourceRepository
) : UseCase<String, Source> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The URL of the source
     * @return The added source
     */
    override suspend fun execute(parameters: String): Source {
        // Validate the URL format
        validateUrl(parameters)

        // Add the source from URL
        return sourceRepository.addSourceFromUrl(parameters)
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
}