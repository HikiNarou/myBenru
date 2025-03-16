package com.mybenru.app.utils

import android.util.Log
import timber.log.Timber

/**
 * Timber Tree untuk melaporkan crash dan error di production environment
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        if (priority == Log.ERROR || priority == Log.WARN) {
            // TODO: Implement crash reporting service like Firebase Crashlytics here

            // Example code for when Firebase Crashlytics is integrated
            // val crashlyticsInstance = FirebaseCrashlytics.getInstance()
            // crashlyticsInstance.log(message)
            // if (t != null) {
            //     crashlyticsInstance.recordException(t)
            // }

            // For now, log to Android's native log
            Log.println(priority, "MyBenru-${tag ?: "App"}", message)

            if (t != null) {
                // Log exception stacktrace
                Log.e("MyBenru-${tag ?: "App"}", "Exception:", t)
            }
        }
    }
}