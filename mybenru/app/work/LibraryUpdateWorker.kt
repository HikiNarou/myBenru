package com.mybenru.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mybenru.app.R
import com.mybenru.domain.usecase.UpdateLibraryUseCase
import com.mybenru.domain.model.LibraryUpdateResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Worker for updating library novels in the background
 */
@HiltWorker
class LibraryUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateLibraryUseCase: UpdateLibraryUseCase
) : CoroutineWorker(context, workerParams) {

    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("Library update worker started")

        try {
            // Execute library update use case
            val result = updateLibraryUseCase.execute(Unit)

            // Process result
            processUpdateResult(result)

            // Return success
            Timber.d("Library update completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error updating library")
            notificationHelper.showUpdateErrorNotification()
            Result.failure()
        }
    }

    /**
     * Process library update result and show notifications
     */
    private fun processUpdateResult(result: LibraryUpdateResult) {
        // Check if any updates were found
        if (result.updatedNovels.isEmpty()) {
            Timber.d("No updates found")
            return
        }

        // Show notification for updates
        val updatedNovelsCount = result.updatedNovels.size
        val totalNewChaptersCount = result.updatedNovels.sumOf { it.newChapters }

        Timber.d(
            "Found $updatedNovelsCount updated novels with $totalNewChaptersCount total new chapters"
        )

        // Show notification
        notificationHelper.showUpdateNotification(
            updatedNovelsCount,
            totalNewChaptersCount,
            result.updatedNovels.map { it.novelTitle }
        )
    }
}