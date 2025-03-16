package com.mybenru.app.utils

import android.content.Context
import android.text.format.DateUtils
import com.mybenru.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Utility functions for date and time operations
 */
object DateUtils {

    private val DEFAULT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val DEFAULT_TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val DEFAULT_DATETIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val ISO8601_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

    init {
        ISO8601_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Format timestamp to readable date string
     */
    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date(timestamp))
    }

    /**
     * Format timestamp to readable time string
     */
    fun formatTime(timestamp: Long, pattern: String = "HH:mm:ss"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date(timestamp))
    }

    /**
     * Format timestamp to readable date and time string
     */
    fun formatDateTime(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date(timestamp))
    }

    /**
     * Parse date string to timestamp
     */
    fun parseDate(dateString: String, pattern: String = "yyyy-MM-dd"): Long {
        return try {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            format.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Parse ISO8601 date string to timestamp
     */
    fun parseIso8601(dateString: String): Long {
        return try {
            ISO8601_FORMAT.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Format timestamp to relative time span (e.g., "5 minutes ago")
     */
    fun getRelativeTimeSpan(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < DateUtils.MINUTE_IN_MILLIS -> {
                context.getString(R.string.just_now)
            }
            diff < DateUtils.HOUR_IN_MILLIS -> {
                val minutes = (diff / DateUtils.MINUTE_IN_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.minutes_ago, minutes, minutes)
            }
            diff < DateUtils.DAY_IN_MILLIS -> {
                val hours = (diff / DateUtils.HOUR_IN_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.hours_ago, hours, hours)
            }
            diff < 7 * DateUtils.DAY_IN_MILLIS -> {
                val days = (diff / DateUtils.DAY_IN_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.days_ago, days, days)
            }
            diff < 4 * 7 * DateUtils.DAY_IN_MILLIS -> {
                val weeks = (diff / (7 * DateUtils.DAY_IN_MILLIS)).toInt()
                context.resources.getQuantityString(R.plurals.weeks_ago, weeks, weeks)
            }
            diff < 12 * 4 * 7 * DateUtils.DAY_IN_MILLIS -> {
                val months = (diff / (30 * DateUtils.DAY_IN_MILLIS)).toInt()
                context.resources.getQuantityString(R.plurals.months_ago, months, months)
            }
            else -> {
                val years = (diff / (365 * DateUtils.DAY_IN_MILLIS)).toInt()
                context.resources.getQuantityString(R.plurals.years_ago, years, years)
            }
        }
    }

    /**
     * Convert duration in seconds to formatted string (e.g., "1h 30m")
     */
    fun formatDuration(durationSeconds: Long): String {
        if (durationSeconds <= 0) {
            return "0m"
        }

        val hours = TimeUnit.SECONDS.toHours(durationSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(durationSeconds) % 60
        val seconds = durationSeconds % 60

        return buildString {
            if (hours > 0) {
                append("${hours}h ")
            }
            if (minutes > 0 || hours > 0) {
                append("${minutes}m")
            }
            if (hours == 0L && minutes == 0L && seconds > 0) {
                append("${seconds}s")
            }
        }
    }

    /**
     * Get start of day timestamp
     */
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of day timestamp
     */
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get start of week timestamp
     */
    fun getStartOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of week timestamp
     */
    fun getEndOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }

    /**
     * Get start of month timestamp
     */
    fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of month timestamp
     */
    fun getEndOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Calculate reading time in minutes based on word count
     */
    fun calculateReadingTimeMinutes(wordCount: Int, wordsPerMinute: Int = 200): Int {
        if (wordCount <= 0) return 0
        return (wordCount / wordsPerMinute) + if (wordCount % wordsPerMinute > 0) 1 else 0
    }

    /**
     * Format reading time in minutes to readable string
     */
    fun formatReadingTime(context: Context, minutes: Int): String {
        return when {
            minutes < 1 -> context.getString(R.string.reading_time_less_than_minute)
            minutes == 1 -> context.getString(R.string.reading_time_one_minute)
            minutes < 60 -> context.getString(R.string.reading_time_minutes, minutes)
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60

                if (remainingMinutes == 0) {
                    context.resources.getQuantityString(R.plurals.reading_time_hours, hours, hours)
                } else {
                    context.getString(
                        R.string.reading_time_hours_minutes,
                        hours,
                        remainingMinutes
                    )
                }
            }
        }
    }
}