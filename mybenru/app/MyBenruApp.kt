package com.mybenru.app

import android.app.Application
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.mybenru.app.utils.CrashReportingTree

@HiltAndroidApp
class MyBenruApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        // Initialize WorkManager for background tasks
        setupPeriodicWorkers()

        Timber.d("Application initialized")
    }

    private fun setupPeriodicWorkers() {
        // Schedule periodic sync for library updates
        val libraryUpdateWork = PeriodicWorkRequestBuilder<LibraryUpdateWorker>(
            repeatInterval = 12,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("library_update_worker")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "library_update_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            libraryUpdateWork
        )

        Timber.d("Periodic workers have been scheduled")
    }

    /**
     * WorkManager for updating library with new chapters
     */
    class LibraryUpdateWorker(
        context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {

        @Inject
        lateinit var checkLibraryUpdatesUseCase: CheckLibraryUpdatesUseCase

        override suspend fun doWork(): Result {
            return try {
                Timber.d("Starting library update check")
                checkLibraryUpdatesUseCase.execute(Unit)
                Timber.d("Library update check completed successfully")
                Result.success()
            } catch (e: Exception) {
                Timber.e(e, "Error checking library updates")
                Result.retry()
            }
        }
    }
}