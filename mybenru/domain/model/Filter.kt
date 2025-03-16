package com.mybenru.domain.model

/**
 * Domain model representing filters for novel queries
 */
sealed class Filter {
    abstract val name: String
    abstract val value: Any

    data class Title(override val value: String) : Filter() {
        override val name: String = "title"
    }

    data class Author(override val value: String) : Filter() {
        override val name: String = "author"
    }

    data class Status(override val value: NovelStatus) : Filter() {
        override val name: String = "status"
    }

    data class Genre(override val value: String) : Filter() {
        override val name: String = "genre"
    }

    data class Tag(override val value: String) : Filter() {
        override val name: String = "tag"
    }

    data class Sort(override val value: SortOption) : Filter() {
        override val name: String = "sort"
    }

    data class AgeRating(override val value: String) : Filter() {
        override val name: String = "age_rating"
    }

    data class Language(override val value: String) : Filter() {
        override val name: String = "language"
    }

    data class Custom(
        override val name: String,
        override val value: Any
    ) : Filter()
}

/**
 * Possible options for sorting novels
 */
enum class SortOption {
    LATEST_CHAPTER,
    POPULARITY,
    RATING,
    RECENTLY_ADDED,
    ALPHABETICAL,
    REVERSE_ALPHABETICAL
}