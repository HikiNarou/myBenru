package com.mybenru.domain.model

/**
 * Domain model representing reader settings
 */
data class ReaderSettings(
    val theme: ReaderTheme = ReaderThemes.LIGHT,
    val fontSize: Int = 16,
    val lineHeight: Float = 1.4f,
    val fontFamily: String = "sans-serif",
    val paragraphSpacing: Int = 16,
    val textAlignment: TextAlignment = TextAlignment.JUSTIFIED,
    val margins: Int = 16,
    val keepScreenOn: Boolean = false,
    val scrollBehavior: ScrollBehavior = ScrollBehavior.CONTINUOUS,
    val showBatteryPercent: Boolean = true,
    val showClockTime: Boolean = true,
    val showChapterTitle: Boolean = true,
    val fullscreen: Boolean = false,
    val orientation: ReaderOrientation = ReaderOrientation.AUTO,
    val autoScroll: Boolean = false,
    val autoScrollSpeed: Int = 50,
    val tapToScroll: Boolean = true,
    val volumeButtonsScroll: Boolean = true,
    val screenScrollPercentage: Int = 5
)

/**
 * Text alignment options for the reader
 */
enum class TextAlignment {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFIED
}

/**
 * Scroll behavior options for the reader
 */
enum class ScrollBehavior {
    CONTINUOUS,
    PAGED
}

/**
 * Orientation options for the reader
 */
enum class ReaderOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE,
    REVERSE_PORTRAIT,
    REVERSE_LANDSCAPE
}