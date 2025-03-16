package com.mybenru.domain.usecase.settings

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to manage app theme settings
 */
class ManageAppThemeUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<String, Unit> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The app theme preference
     */
    override suspend fun execute(parameters: String) {
        // Validate theme
        validateTheme(parameters)

        // Set app theme
        userPreferenceRepository.setAppTheme(parameters)
    }

    /**
     * Validate the theme
     *
     * @param theme The theme to validate
     * @throws IllegalArgumentException if the theme is invalid
     */
    private fun validateTheme(theme: String) {
        val validThemes = listOf("light", "dark", "system", "auto_battery")

        if (theme !in validThemes) {
            throw IllegalArgumentException("Invalid theme: $theme. Valid themes are: ${validThemes.joinToString()}")
        }
    }
}