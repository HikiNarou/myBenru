package com.mybenru.domain.usecase.settings

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.ReaderSettings
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to update the user's reading preferences
 */
class UpdateReaderSettingsUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<ReaderSettings, Unit> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The updated reading preferences
     */
    override suspend fun execute(parameters: ReaderSettings) {
        userPreferenceRepository.updateReaderSettings(parameters)
    }
}