package com.mybenru.app.model

/**
 * UI model for a library filter
 */
data class LibraryFilterUiModel(
    val filterType: FilterType,
    val isActive: Boolean = false,
    val displayName: String = "",
    val filterValue: String? = null
) {
    /**
     * Get display name for the filter.
     * Jika `displayName` tidak kosong, maka nilai tersebut yang akan ditampilkan.
     * Jika filter merupakan BY_AUTHOR, BY_GENRE, atau BY_STATUS dan `filterValue` tersedia,
     * maka akan ditampilkan dengan format khusus, misalnya "Author: {filterValue}".
     * Jika tidak, akan dikembalikan nilai default sesuai dengan tipe filter.
     */
    fun getDisplayName(): String {
        return when {
            displayName.isNotEmpty() -> displayName
            filterType == FilterType.BY_AUTHOR && !filterValue.isNullOrEmpty() -> "Author: $filterValue"
            filterType == FilterType.BY_GENRE && !filterValue.isNullOrEmpty() -> "Genre: $filterValue"
            filterType == FilterType.BY_STATUS && !filterValue.isNullOrEmpty() -> "Status: $filterValue"
            filterType == FilterType.ALL -> "All"
            filterType == FilterType.UNREAD -> "Unread"
            filterType == FilterType.COMPLETED -> "Completed"
            filterType == FilterType.DOWNLOADED -> "Downloaded"
            filterType == FilterType.RECENTLY_READ -> "Recently Read"
            filterType == FilterType.RECENTLY_ADDED -> "Recently Added"
            else -> filterType.toString()
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Filter types for the library.
     */
    enum class FilterType {
        ALL,
        UNREAD,
        COMPLETED,
        DOWNLOADED,
        BY_AUTHOR,
        BY_GENRE,
        BY_STATUS,
        RECENTLY_READ,
        RECENTLY_ADDED
    }
}
