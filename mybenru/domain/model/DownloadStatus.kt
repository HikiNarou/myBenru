package com.mybenru.domain.model

/**
 * Domain model representing the download status of a chapter
 */
enum class DownloadStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    PAUSED,
    CANCELLED
}

/**
 * Domain model representing a download task
 */
data class DownloadTask(
    val id: String,
    val novelId: String,
    val chapterId: String,
    val status: DownloadStatus,
    val progress: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)