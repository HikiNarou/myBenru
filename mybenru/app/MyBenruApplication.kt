package com.mybenru.app

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mybenru.app.work.LibraryUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class for initialization and global configuration
 */
@HiltAndroidApp
class MyBenruApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            // Enable strict mode in debug builds
            setupStrictMode()
        }

        // Initialize app settings
        initializeSettings()

        // Schedule background work
        scheduleBackgroundWork()

        // Run initialization tasks that don't need to block app startup
        GlobalScope.launch {
            // Perform other async initialization tasks
            initializeAsyncTasks()
        }
    }

    /**
     * Initialize app settings
     */
    private fun initializeSettings() {
        // Load theme from preferences
        val themeMode = getPreferences().getString("theme_mode", "system") ?: "system"

        // Apply theme
        val nightMode = when (themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * Get shared preferences
     */
    private fun getPreferences() = getSharedPreferences("app_settings", MODE_PRIVATE)

    /**
     * Schedule background work for library updates
     */
    private fun scheduleBackgroundWork() {
        val automaticUpdates = getPreferences().getBoolean("automatic_updates", true)

        if (automaticUpdates) {
            val updateRequest = PeriodicWorkRequestBuilder<LibraryUpdateWorker>(
                12, TimeUnit.HOURS, // Run every 12 hours
                1, TimeUnit.HOURS // Flex period of 1 hour
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "library_update_work",
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if present
                updateRequest
            )

            Timber.d("Scheduled library update work")
        } else {
            // Cancel any existing work if automatic updates are disabled
            WorkManager.getInstance(this).cancelUniqueWork("library_update_work")
            Timber.d("Cancelled library update work")
        }
    }

    /**
     * Set up strict mode for development
     */
    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectCleartextNetwork()
                .penaltyLog()
                .build()
        )
    }

    /**
     * Initialize tasks that can run asynchronously
     */
    private suspend fun initializeAsyncTasks() {
        // Example: Pre-warm caches, check for app updates, etc.
        Timber.d("Performing async initialization tasks")
    }

    /**
     * Provide WorkManager configuration with Hilt worker factory
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
            .build()
    }
}