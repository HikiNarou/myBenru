package com.mybenru.domain.usecase.settings

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.ReaderTheme
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to create a custom reader theme
 */
class CreateCustomReaderThemeUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<ReaderTheme, ReaderTheme> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The theme to create
     * @return The created theme
     */
    override suspend fun execute(parameters: ReaderTheme): ReaderTheme {
        // Validate theme colors
        validateThemeColors(parameters)

        // Create the theme with isCustom set to true
        val customTheme = parameters.copy(isCustom = true)
        return userPreferenceRepository.createReaderTheme(customTheme)
    }

    /**
     * Validate theme colors
     *
     * @param theme The theme to validate
     * @throws IllegalArgumentException if the theme colors are invalid
     */
    private fun validateThemeColors(theme: ReaderTheme) {
        // Check if colors are valid hex colors
        val colorRegex = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")

        if (!colorRegex.matches(theme.backgroundColor)) {
            throw IllegalArgumentException("Invalid background color format")
        }

        if (!colorRegex.matches(theme.textColor)) {
            throw IllegalArgumentException("Invalid text color format")
        }

        if (!colorRegex.matches(theme.selectionColor)) {
            throw IllegalArgumentException("Invalid selection color format")
        }

        if (theme.linkColor != null && !colorRegex.matches(theme.linkColor)) {
            throw IllegalArgumentException("Invalid link color format")
        }

        // Check if there's enough contrast between background and text colors
        // This is a simplified check, a real implementation would use proper color contrast algorithms
        val backgroundBrightness = calculateColorBrightness(theme.backgroundColor)
        val textBrightness = calculateColorBrightness(theme.textColor)

        val contrastRatio = if (backgroundBrightness > textBrightness) {
            (backgroundBrightness + 0.05) / (textBrightness + 0.05)
        } else {
            (textBrightness + 0.05) / (backgroundBrightness + 0.05)
        }

        if (contrastRatio < 4.5) {
            throw IllegalArgumentException("Insufficient contrast between background and text colors")
        }
    }

    /**
     * Calculate the brightness of a color
     *
     * @param color The color as a hex string
     * @return The brightness value (0.0-1.0)
     */
    private fun calculateColorBrightness(color: String): Double {
        // Extract RGB components
        val r = Integer.parseInt(color.substring(1, 3), 16)
        val g = Integer.parseInt(color.substring(3, 5), 16)
        val b = Integer.parseInt(color.substring(5, 7), 16)

        // Calculate relative luminance according to W3C formula
        val sR = r / 255.0
        val sG = g / 255.0
        val sB = b / 255.0

        val r1 = if (sR <= 0.03928) sR / 12.92 else Math.pow((sR + 0.055) / 1.055, 2.4)
        val g1 = if (sG <= 0.03928) sG / 12.92 else Math.pow((sG + 0.055) / 1.055, 2.4)
        val b1 = if (sB <= 0.03928) sB / 12.92 else Math.pow((sB + 0.055) / 1.055, 2.4)

        return 0.2126 * r1 + 0.7152 * g1 + 0.0722 * b1
    }
}