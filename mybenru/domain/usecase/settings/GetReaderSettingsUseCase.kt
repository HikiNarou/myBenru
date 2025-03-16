package com.mybenru.domain.usecase.settings

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.ReaderSettings
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to get the user's reading preferences
 */
class GetReaderSettingsUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : NoParamsUseCase<ReaderSettings> {

    /**
     * Execute the use case without parameters
     *
     * @return The user's reading preferences
     */
    override suspend fun execute(): ReaderSettings {
        return userPreferenceRepository.getReaderSettings()
    }
}