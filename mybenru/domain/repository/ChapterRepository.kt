package com.mybenru.domain.repository

import com.mybenru.domain.model.Chapter
import com.mybenru.domain.model.ChapterDetail
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chapter operations
 */
interface ChapterRepository {
    /**
     * Get all chapters for a novel
     *
     * @param novelId The ID of the novel
     * @param forceRefresh Whether to force a refresh from remote source
     * @return List of chapters for the novel
     */
    suspend fun getChapters(novelId: String, forceRefresh: Boolean = false): List<Chapter>

    /**
     * Get a chapter by its ID
     *
     * @param chapterId The ID of the chapter
     * @param forceRefresh Whether to force a refresh from remote source
     * @return The requested chapter
     * @throws ResourceNotFoundException if the chapter is not found
     */
    suspend fun getChapter(chapterId: String, forceRefresh: Boolean = false): Chapter

    /**
     * Get chapter content
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param forceRefresh Whether to force a refresh from remote source
     * @return The chapter with content
     */
    suspend fun getChapterContent(chapterId: String, novelId: String, forceRefresh: Boolean = false): Chapter

    /**
     * Get chapter detail which includes the chapter and its novel
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return ChapterDetail containing the chapter and novel
     */
    suspend fun getChapterDetail(chapterId: String, novelId: String): ChapterDetail

    /**
     * Get the first chapter of a novel
     *
     * @param novelId The ID of the novel
     * @return The first chapter, or null if there are no chapters
     */
    suspend fun getFirstChapter(novelId: String): Chapter?

    /**
     * Get the last chapter of a novel
     *
     * @param novelId The ID of the novel
     * @return The last chapter, or null if there are no chapters
     */
    suspend fun getLastChapter(novelId: String): Chapter?

    /**
     * Get the next chapter
     *
     * @param chapterId The ID of the current chapter
     * @param novelId The ID of the novel
     * @return The next chapter, or null if this is the last chapter
     */
    suspend fun getNextChapter(chapterId: String, novelId: String): Chapter?

    /**
     * Get the previous chapter
     *
     * @param chapterId The ID of the current chapter
     * @param novelId The ID of the novel
     * @return The previous chapter, or null if this is the first chapter
     */
    suspend fun getPreviousChapter(chapterId: String, novelId: String): Chapter?

    /**
     * Mark a chapter as read
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param read Whether the chapter is read
     * @return The updated chapter
     */
    suspend fun markChapterRead(chapterId: String, novelId: String, read: Boolean = true): Chapter

    /**
     * Mark multiple chapters as read
     *
     * @param chapterIds The IDs of the chapters
     * @param novelId The ID of the novel
     * @param read Whether the chapters are read
     * @return The number of chapters updated
     */
    suspend fun markChaptersRead(chapterIds: List<String>, novelId: String, read: Boolean = true): Int

    /**
     * Mark all chapters up to a specific chapter as read
     *
     * @param chapterId The ID of the last chapter to mark
     * @param novelId The ID of the novel
     * @param read Whether the chapters are read
     * @return The number of chapters updated
     */
    suspend fun markChaptersReadUpTo(chapterId: String, novelId: String, read: Boolean = true): Int

    /**
     * Download a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return The downloaded chapter
     */
    suspend fun downloadChapter(chapterId: String, novelId: String): Chapter

    /**
     * Delete a downloaded chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return True if the chapter was deleted, false otherwise
     */
    suspend fun deleteDownloadedChapter(chapterId: String, novelId: String): Boolean

    /**
     * Get downloaded chapters for a novel
     *
     * @param novelId The ID of the novel
     * @return List of downloaded chapters
     */
    suspend fun getDownloadedChapters(novelId: String): List<Chapter>

    /**
     * Bookmark a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param bookmarked Whether the chapter is bookmarked
     * @return The updated chapter
     */
    suspend fun bookmarkChapter(chapterId: String, novelId: String, bookmarked: Boolean = true): Chapter

    /**
     * Get all bookmarked chapters
     *
     * @return Flow of bookmarked chapters
     */
    fun getBookmarkedChapters(): Flow<List<Chapter>>

    /**
     * Get all bookmarked chapters for a novel
     *
     * @param novelId The ID of the novel
     * @return List of bookmarked chapters
     */
    suspend fun getBookmarkedChaptersForNovel(novelId: String): List<Chapter>

    /**
     * Update the reading progress of a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param progress The reading progress (0.0-1.0)
     * @param position The current reading position in characters
     * @return The updated chapter
     */
    suspend fun updateChapterReadingProgress(
        chapterId: String,
        novelId: String,
        progress: Float,
        position: Int
    ): Chapter

    /**
     * Get recently read chapters
     *
     * @param limit Maximum number of chapters to return
     * @return List of recently read chapters
     */
    suspend fun getRecentlyReadChapters(limit: Int = 10): List<Chapter>

    /**
     * Get all unread chapters for a novel
     *
     * @param novelId The ID of the novel
     * @return List of unread chapters
     */
    suspend fun getUnreadChapters(novelId: String): List<Chapter>

    /**
     * Get all read chapters for a novel
     *
     * @param novelId The ID of the novel
     * @return List of read chapters
     */
    suspend fun getReadChapters(novelId: String): List<Chapter>

    /**
     * Get the most recent chapter for a novel
     *
     * @param novelId The ID of the novel
     * @return The most recent chapter, or null if there are no chapters
     */
    suspend fun getMostRecentChapter(novelId: String): Chapter?

    /**
     * Download multiple chapters
     *
     * @param chapterIds The IDs of the chapters to download
     * @param novelId The ID of the novel
     * @return The number of successfully downloaded chapters
     */
    suspend fun downloadChapters(chapterIds: List<String>, novelId: String): Int

    /**
     * Delete all downloaded chapters for a novel
     *
     * @param novelId The ID of the novel
     * @return The number of deleted chapters
     */
    suspend fun deleteDownloadedChapters(novelId: String): Int

    /**
     * Search for text in a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param query The search query
     * @return List of positions where the query was found
     */
    suspend fun searchInChapter(chapterId: String, novelId: String, query: String): List<Int>

    /**
     * Check if chapter content exists locally
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return True if content exists locally, false otherwise
     */
    suspend fun hasLocalContent(chapterId: String, novelId: String): Boolean

    /**
     * Get the word count for a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return The word count, or 0 if content is not available
     */
    suspend fun getWordCount(chapterId: String, novelId: String): Int

    /**
     * Get the estimated reading time for a chapter in minutes
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @param wordsPerMinute Average reading speed in words per minute
     * @return The estimated reading time in minutes
     */
    suspend fun getEstimatedReadingTime(chapterId: String, novelId: String, wordsPerMinute: Int = 250): Int
}