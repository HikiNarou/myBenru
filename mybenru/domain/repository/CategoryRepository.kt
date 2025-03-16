package com.mybenru.domain.repository

import com.mybenru.domain.model.Category
import com.mybenru.domain.model.LibraryCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category operations
 */
interface CategoryRepository {
    /**
     * Get all categories
     *
     * @param sourceId Optional source ID to filter by
     * @return Flow of all categories
     */
    fun getAllCategories(sourceId: String? = null): Flow<List<Category>>

    /**
     * Get a category by its ID
     *
     * @param categoryId The ID of the category
     * @return The requested category, or null if not found
     */
    suspend fun getCategory(categoryId: String): Category?

    /**
     * Create a new category
     *
     * @param category The category to create
     * @return The created category
     */
    suspend fun createCategory(category: Category): Category

    /**
     * Update an existing category
     *
     * @param category The category to update
     * @return The updated category
     */
    suspend fun updateCategory(category: Category): Category

    /**
     * Delete a category
     *
     * @param categoryId The ID of the category to delete
     * @return True if the category was deleted, false otherwise
     */
    suspend fun deleteCategory(categoryId: String): Boolean

    /**
     * Get all library categories
     *
     * @return Flow of all library categories
     */
    fun getAllLibraryCategories(): Flow<List<LibraryCategory>>

    /**
     * Get a library category by its ID
     *
     * @param categoryId The ID of the library category
     * @return The requested library category, or null if not found
     */
    suspend fun getLibraryCategory(categoryId: String): LibraryCategory?

    /**
     * Create a new library category
     *
     * @param category The library category to create
     * @return The created library category
     */
    suspend fun createLibraryCategory(category: LibraryCategory): LibraryCategory

    /**
     * Update an existing library category
     *
     * @param category The library category to update
     * @return The updated library category
     */
    suspend fun updateLibraryCategory(category: LibraryCategory): LibraryCategory

    /**
     * Delete a library category
     *
     * @param categoryId The ID of the library category to delete
     * @return True if the library category was deleted, false otherwise
     */
    suspend fun deleteLibraryCategory(categoryId: String): Boolean

    /**
     * Add a novel to a library category
     *
     * @param novelId The ID of the novel
     * @param categoryId The ID of the library category
     * @return The updated library category
     */
    suspend fun addNovelToLibraryCategory(novelId: String, categoryId: String): LibraryCategory

    /**
     * Remove a novel from a library category
     *
     * @param novelId The ID of the novel
     * @param categoryId The ID of the library category
     * @return The updated library category
     */
    suspend fun removeNovelFromLibraryCategory(novelId: String, categoryId: String): LibraryCategory

    /**
     * Get all library categories for a novel
     *
     * @param novelId The ID of the novel
     * @return List of library categories for the novel
     */
    suspend fun getLibraryCategoriesForNovel(novelId: String): List<LibraryCategory>

    /**
     * Get all novels in a library category
     *
     * @param categoryId The ID of the library category
     * @return The IDs of novels in the category
     */
    suspend fun getNovelsInLibraryCategory(categoryId: String): List<String>

    /**
     * Reorder library categories
     *
     * @param categoryIds The IDs of the categories in the new order
     * @return True if the categories were reordered, false otherwise
     */
    suspend fun reorderLibraryCategories(categoryIds: List<String>): Boolean
}