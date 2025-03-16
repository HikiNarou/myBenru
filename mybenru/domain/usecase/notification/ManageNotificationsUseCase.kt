package com.mybenru.domain.usecase.notification

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to manage notifications settings
 */
class ManageNotificationsUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<Boolean, Unit> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Whether notifications should be enabled
     */
    override suspend fun execute(parameters: Boolean) {
        userPreferenceRepository.setNotificationsEnabled(parameters)
    }
}