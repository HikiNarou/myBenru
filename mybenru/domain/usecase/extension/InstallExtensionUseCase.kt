package com.mybenru.domain.usecase.extension

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Extension
import com.mybenru.domain.repository.ExtensionRepository
import javax.inject.Inject

/**
 * Use case to install an extension
 */
class InstallExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) : UseCase<String, Extension> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the extension to install
     * @return The installed extension
     */
    override suspend fun execute(parameters: String): Extension {
        return extensionRepository.installExtension(parameters)
    }
}