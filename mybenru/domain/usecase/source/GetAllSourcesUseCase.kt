package com.mybenru.domain.usecase.source

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.Source
import com.mybenru.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all available sources
 */
class GetAllSourcesUseCase @Inject constructor(
    private val sourceRepository: SourceRepository
) : NoParamsUseCase<Flow<List<Source>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of all sources
     */
    override suspend fun execute(): Flow<List<Source>> {
        return sourceRepository.getAllSources()
    }
}