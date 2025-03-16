package com.mybenru.domain.usecase.extension

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.Extension
import com.mybenru.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all available extensions
 */
class GetAllExtensionsUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) : NoParamsUseCase<Flow<List<Extension>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of all extensions
     */
    override suspend fun execute(): Flow<List<Extension>> {
        return extensionRepository.getAllExtensions()
    }
}