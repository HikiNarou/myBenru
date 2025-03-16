package com.mybenru.domain.model

/**
 * Domain model representing theme settings for the reader
 */
data class ReaderTheme(
    val id: String,
    val name: String,
    val backgroundColor: String,
    val textColor: String,
    val selectionColor: String,
    val isCustom: Boolean = false,
    val linkColor: String? = null,
    val isDefault: Boolean = false
)

/**
 * Predefined reader themes
 */
object ReaderThemes {
    val LIGHT = ReaderTheme(
        id = "light",
        name = "Light",
        backgroundColor = "#FFFFFF",
        textColor = "#202020",
        selectionColor = "#B3E5FC",
        isDefault = true
    )

    val DARK = ReaderTheme(
        id = "dark",
        name = "Dark",
        backgroundColor = "#303030",
        textColor = "#E0E0E0",
        selectionColor = "#5D4037"
    )

    val BLACK = ReaderTheme(
        id = "black",
        name = "Black",
        backgroundColor = "#000000",
        textColor = "#B0B0B0",
        selectionColor = "#424242"
    )

    val SEPIA = ReaderTheme(
        id = "sepia",
        name = "Sepia",
        backgroundColor = "#FBF0D9",
        textColor = "#5B4636",
        selectionColor = "#FFE082"
    )

    fun getAll(): List<ReaderTheme> = listOf(LIGHT, DARK, BLACK, SEPIA)
}