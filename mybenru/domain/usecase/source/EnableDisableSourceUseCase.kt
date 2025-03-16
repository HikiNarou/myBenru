package com.mybenru.domain.usecase.source

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Source
import com.mybenru.domain.repository.SourceRepository
import javax.inject.Inject

/**
 * Use case to enable or disable a source
 */
class EnableDisableSourceUseCase @Inject constructor(
    private val sourceRepository: SourceRepository
) : UseCase<EnableDisableSourceUseCase.Params, Source> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The updated source
     */
    override suspend fun execute(parameters: Params): Source {
        return sourceRepository.setSourceEnabled(
            sourceId = parameters.sourceId,
            enabled = parameters.enabled
        )
    }

    /**
     * Parameters for [EnableDisableSourceUseCase]
     */
    data class Params(
        val sourceId: String,
        val enabled: Boolean
    )
}