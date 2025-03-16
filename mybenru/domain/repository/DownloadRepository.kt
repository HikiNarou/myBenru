package com.mybenru.domain.repository

import com.mybenru.domain.model.DownloadTask
import com.mybenru.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for download operations
 */
interface DownloadRepository {
    /**
     * Get all download tasks
     *
     * @return Flow of all download tasks
     */
    fun getAllDownloadTasks(): Flow<List<DownloadTask>>

    /**
     * Get a download task by its ID
     *
     * @param taskId The ID of the task
     * @return The requested download task, or null if not found
     */
    suspend fun getDownloadTask(taskId: String): DownloadTask?

    /**
     * Create a new download task
     *
     * @param novelId The ID of the novel
     * @param chapterId The ID of the chapter
     * @return The created download task
     */
    suspend fun createDownloadTask(novelId: String, chapterId: String): DownloadTask

    /**
     * Update a download task
     *
     * @param task The task to update
     * @return The updated download task
     */
    suspend fun updateDownloadTask(task: DownloadTask): DownloadTask

    /**
     * Delete a download task
     *
     * @param taskId The ID of the task to delete
     * @return True if the task was deleted, false otherwise
     */
    suspend fun deleteDownloadTask(taskId: String): Boolean

    /**
     * Get download tasks by status
     *
     * @param status The status to filter by
     * @return List of download tasks with the specified status
     */
    suspend fun getDownloadTasksByStatus(status: DownloadStatus): List<DownloadTask>

    /**
     * Get download tasks for a novel
     *
     * @param novelId The ID of the novel
     * @return List of download tasks for the novel
     */
    suspend fun getDownloadTasksForNovel(novelId: String): List<DownloadTask>

    /**
     * Get the download task for a chapter
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return The download task for the chapter, or null if not found
     */
    suspend fun getDownloadTaskForChapter(chapterId: String, novelId: String): DownloadTask?

    /**
     * Pause a download task
     *
     * @param taskId The ID of the task
     * @return The updated download task
     */
    suspend fun pauseDownloadTask(taskId: String): DownloadTask

    /**
     * Resume a paused download task
     *
     * @param taskId The ID of the task
     * @return The updated download task
     */
    suspend fun resumeDownloadTask(taskId: String): DownloadTask

    /**
     * Cancel a download task
     *
     * @param taskId The ID of the task
     * @return The updated download task
     */
    suspend fun cancelDownloadTask(taskId: String): DownloadTask

    /**
     * Pause all download tasks
     *
     * @return The number of paused tasks
     */
    suspend fun pauseAllDownloadTasks(): Int

    /**
     * Resume all paused download tasks
     *
     * @return The number of resumed tasks
     */
    suspend fun resumeAllDownloadTasks(): Int

    /**
     * Cancel all download tasks
     *
     * @return The number of cancelled tasks
     */
    suspend fun cancelAllDownloadTasks(): Int

    /**
     * Get the download progress for a novel
     *
     * @param novelId The ID of the novel
     * @return The download progress (0.0-1.0)
     */
    suspend fun getNovelDownloadProgress(novelId: String): Float

    /**
     * Check if a chapter is being downloaded
     *
     * @param chapterId The ID of the chapter
     * @param novelId The ID of the novel
     * @return True if the chapter is being downloaded, false otherwise
     */
    suspend fun isChapterDownloading(chapterId: String, novelId: String): Boolean

    /**
     * Delete all download tasks for a novel
     *
     * @param novelId The ID of the novel
     * @return The number of deleted tasks
     */
    suspend fun deleteDownloadTasksForNovel(novelId: String): Int
}