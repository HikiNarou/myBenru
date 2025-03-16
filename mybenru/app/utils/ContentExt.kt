package com.mybenru.app.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * Check if device has internet connection
 */
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }
}

/**
 * Check if device is in dark mode
 */
fun Context.isInDarkMode(): Boolean {
    return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}

/**
 * Get color from resources
 */
@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

/**
 * Convert DP to pixels
 */
fun Context.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}

/**
 * Convert SP to pixels
 */
fun Context.spToPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}

/**
 * Get color from theme attribute
 */
@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

/**
 * Open app settings screen
 */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivitySafely(intent)
}

/**
 * Start an activity safely (with try-catch)
 */
fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e, "Activity not found for intent: $intent")
        showToast("No app found to handle this action")
    } catch (e: Exception) {
        Timber.e(e, "Error starting activity")
        showToast("Error opening: ${e.localizedMessage}")
    }
}

/**
 * Share text content
 */
fun Context.shareText(text: String, subject: String? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    startActivitySafely(Intent.createChooser(intent, "Share via"))
}

/**
 * Open URL in browser
 */
fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivitySafely(intent)
    } catch (e: Exception) {
        Timber.e(e, "Error opening URL: $url")
        showToast("Error opening URL")
    }
}

/**
 * Is device in landscape orientation
 */
fun Context.isInLandscape() =
    resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Get status bar height
 */
fun Context.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

/**
 * Get navigation bar height
 */
fun Context.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

/**
 * Parse color from hex string
 */
fun Context.parseColor(colorString: String?): Int {
    return try {
        if (colorString != null && colorString.isNotEmpty()) {
            if (colorString[0] == '#') {
                Color.parseColor(colorString)
            } else {
                Color.parseColor("#$colorString")
            }
        } else {
            Color.BLACK
        }
    } catch (e: Exception) {
        Timber.e(e, "Error parsing color: $colorString")
        Color.BLACK
    }
}

/**
 * Is this context an Activity that hasn't been destroyed
 */
fun Context.isValidActivity(): Boolean {
    if (this !is Activity) return false
    return !isFinishing && !isDestroyed
}