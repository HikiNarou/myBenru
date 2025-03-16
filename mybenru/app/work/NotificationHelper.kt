package com.mybenru.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mybenru.app.R
import com.mybenru.app.ui.MainActivity
import timber.log.Timber

/**
 * Helper class for creating and showing notifications
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_UPDATES = "library_updates"
        private const val NOTIFICATION_ID_UPDATES = 1001
        private const val CHANNEL_ID_ERRORS = "errors"
        private const val NOTIFICATION_ID_ERRORS = 2001
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Updates channel
            val updatesChannel = NotificationChannel(
                CHANNEL_ID_UPDATES,
                context.getString(R.string.notification_channel_updates_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_updates_description)
            }

            // Errors channel
            val errorsChannel = NotificationChannel(
                CHANNEL_ID_ERRORS,
                context.getString(R.string.notification_channel_errors_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_errors_description)
            }

            // Register the channels
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannels(listOf(updatesChannel, errorsChannel))
        }
    }

    /**
     * Show notification for library updates.
     * This method checks for POST_NOTIFICATIONS permission on Android 13+.
     */
    fun showUpdateNotification(
        updatedNovelsCount: Int,
        totalNewChaptersCount: Int,
        updatedNovelTitles: List<String>
    ) {
        // Create intent for notification tap action
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openLibrary", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = context.getString(
            R.string.notification_update_title,
            updatedNovelsCount
        )

        val contentText = context.getString(
            R.string.notification_update_text,
            totalNewChaptersCount
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(contentTitle)
            .setSummaryText(contentText)

        updatedNovelTitles.take(5).forEach { title ->
            inboxStyle.addLine(title)
        }

        if (updatedNovelTitles.size > 5) {
            inboxStyle.addLine(context.getString(R.string.notification_more_items, updatedNovelTitles.size - 5))
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .build()

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.e("POST_NOTIFICATIONS permission not granted. Cannot show update notification.")
                return
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_UPDATES, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException when posting update notification")
        }
    }

    /**
     * Show notification for update errors.
     * This method checks for POST_NOTIFICATIONS permission on Android 13+.
     */
    fun showUpdateErrorNotification() {
        // Create intent for notification tap action
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ERRORS)
            .setSmallIcon(R.drawable.ic_notification_error)
            .setContentTitle(context.getString(R.string.notification_error_title))
            .setContentText(context.getString(R.string.notification_error_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.e("POST_NOTIFICATIONS permission not granted. Cannot show error notification.")
                return
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ERRORS, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException when posting error notification")
        }
    }
}
