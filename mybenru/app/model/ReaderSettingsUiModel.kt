package com.mybenru.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI Model for Reader Settings
 */
@Parcelize
data class ReaderSettingsUiModel(
    val textSize: Int = 16,
    val lineSpacing: Float = 1.5f,
    val paragraphSpacing: Int = 16,
    val fontFamily: String = "sans-serif",
    val textAlignment: TextAlignment = TextAlignment.JUSTIFIED,
    val theme: ReaderTheme = ReaderTheme.LIGHT,
    val brightness: Int = 50,
    val isSystemBrightness: Boolean = true,
    val isFullscreen: Boolean = false,
    val isKeepScreenOn: Boolean = true,
    val isVerticalScrolling: Boolean = true,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16
) : Parcelable {

    /**
     * Text alignment options
     */
    enum class TextAlignment {
        LEFT, CENTER, RIGHT, JUSTIFIED
    }

    /**
     * Reader theme options
     */
    enum class ReaderTheme {
        LIGHT, DARK, SEPIA, BLACK, SYSTEM
    }
}