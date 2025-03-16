package com.mybenru.domain.usecase.library

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.LibraryUpdateFrequency
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to update the library update frequency
 */
class UpdateLibraryFrequencyUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<LibraryUpdateFrequency, Unit> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The library update frequency
     */
    override suspend fun execute(parameters: LibraryUpdateFrequency) {
        userPreferenceRepository.setLibraryUpdateFrequency(parameters)
    }
}