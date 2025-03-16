package com.mybenru.domain.repository

import com.mybenru.domain.model.Novel
import com.mybenru.domain.model.SearchResult
import com.mybenru.domain.model.Filter
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for novel operations
 */
interface NovelRepository {
    /**
     * Get a novel by its ID
     *
     * @param novelId The ID of the novel to retrieve
     * @param forceRefresh Whether to force a refresh from remote source
     * @return The requested novel
     * @throws ResourceNotFoundException if the novel is not found
     */
    suspend fun getNovel(novelId: String, forceRefresh: Boolean = false): Novel

    /**
     * Get all novels in the library
     *
     * @return Flow of all novels in the library
     */
    fun getLibraryNovels(): Flow<List<Novel>>

    /**
     * Get recently read novels
     *
     * @param limit Maximum number of novels to return
     * @return List of recently read novels
     */
    suspend fun getRecentlyReadNovels(limit: Int = 10): List<Novel>

    /**
     * Get recently added novels
     *
     * @param limit Maximum number of novels to return
     * @return List of recently added novels
     */
    suspend fun getRecentlyAddedNovels(limit: Int = 10): List<Novel>

    /**
     * Search for novels
     *
     * @param query The search query
     * @param filters Optional filters to apply to the search
     * @param page The page number for paginated results
     * @return SearchResult containing the matching novels
     */
    suspend fun searchNovels(
        query: String,
        filters: List<Filter> = emptyList(),
        page: Int = 1
    ): SearchResult

    /**
     * Get popular novels
     *
     * @param sourceId Optional source ID to filter by
     * @param page The page number for paginated results
     * @return SearchResult containing popular novels
     */
    suspend fun getPopularNovels(sourceId: String? = null, page: Int = 1): SearchResult

    /**
     * Get latest updated novels
     *
     * @param sourceId Optional source ID to filter by
     * @param page The page number for paginated results
     * @return SearchResult containing latest novels
     */
    suspend fun getLatestNovels(sourceId: String? = null, page: Int = 1): SearchResult

    /**
     * Get related novels for a given novel
     *
     * @param novelId The ID of the novel to get related novels for
     * @param limit Maximum number of novels to return
     * @return List of related novels
     */
    suspend fun getRelatedNovels(novelId: String, limit: Int = 5): List<Novel>

    /**
     * Add a novel to the library
     *
     * @param novel The novel to add to the library
     * @return The updated novel
     */
    suspend fun addNovelToLibrary(novel: Novel): Novel

    /**
     * Remove a novel from the library
     *
     * @param novelId The ID of the novel to remove
     * @return True if the novel was removed, false otherwise
     */
    suspend fun removeNovelFromLibrary(novelId: String): Boolean

    /**
     * Update novel information from remote source
     *
     * @param novelId The ID of the novel to update
     * @return The updated novel
     */
    suspend fun refreshNovel(novelId: String): Novel

    /**
     * Get novels by category
     *
     * @param categoryId The ID of the category
     * @param page The page number for paginated results
     * @return SearchResult containing novels in the category
     */
    suspend fun getNovelsByCategory(categoryId: String, page: Int = 1): SearchResult

    /**
     * Get recommended novels based on user's reading history
     *
     * @param limit Maximum number of novels to return
     * @return List of recommended novels
     */
    suspend fun getRecommendedNovels(limit: Int = 10): List<Novel>

    /**
     * Fetch novel details from a URL
     *
     * @param url The URL of the novel
     * @param sourceId Optional source ID to use for parsing
     * @return The fetched novel
     */
    suspend fun fetchNovelFromUrl(url: String, sourceId: String? = null): Novel

    /**
     * Update the reading progress for a novel
     *
     * @param novelId The ID of the novel
     * @param progress The reading progress (0-100)
     * @param lastReadChapterId The ID of the last read chapter
     * @return The updated novel
     */
    suspend fun updateNovelReadingProgress(
        novelId: String,
        progress: Int,
        lastReadChapterId: String? = null
    ): Novel

    /**
     * Update novel categories
     *
     * @param novelId The ID of the novel
     * @param categoryIds The IDs of the categories to assign to the novel
     * @return The updated novel
     */
    suspend fun updateNovelCategories(novelId: String, categoryIds: List<String>): Novel
}