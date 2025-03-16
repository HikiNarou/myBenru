package com.mybenru.domain.usecase.source

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Source
import com.mybenru.domain.repository.SourceRepository
import javax.inject.Inject

/**
 * Use case to add a new source
 */
class AddSourceUseCase @Inject constructor(
    private val sourceRepository: SourceRepository
) : UseCase<Source, Source> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The source to add
     * @return The added source
     */
    override suspend fun execute(parameters: Source): Source {
        return sourceRepository.addSource(parameters)
    }
}