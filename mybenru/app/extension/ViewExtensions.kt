package com.mybenru.app.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.google.android.material.snackbar.Snackbar
import com.mybenru.app.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Set view visibility to VISIBLE
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Set view visibility to GONE
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Set view visibility to INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Toggle visibility (VISIBLE <-> GONE)
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Show snackbar with message
 */
fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action.invoke() }
    }
    snackbar.show()
}

/**
 * Show snackbar with message resource
 */
fun View.showSnackbar(
    @StringRes messageRes: Int,
    duration: Int = Snackbar.LENGTH_LONG,
    @StringRes actionTextRes: Int? = null,
    action: (() -> Unit)? = null
) {
    showSnackbar(
        message = context.getString(messageRes),
        duration = duration,
        actionText = actionTextRes?.let { context.getString(it) },
        action = action
    )
}

/**
 * Show toast with message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show toast with message resource
 */
fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(messageRes), duration).show()
}

/**
 * Show toast from fragment
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

/**
 * Show toast from fragment with message resource
 */
fun Fragment.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(messageRes, duration)
}

/**
 * Hide keyboard
 */
fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Show keyboard
 */
fun View.showKeyboard() {
    requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Load image with rounded corners
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.placeholder_cover,
    @DrawableRes error: Int = R.drawable.placeholder_cover,
    cornerRadius: Float = 8f,
    cacheable: Boolean = true
) {
    load(url) {
        crossfade(true)
        placeholder(placeholder)
        error(error)
        transformations(RoundedCornersTransformation(cornerRadius))

        if (!cacheable) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.DISABLED)
        }

        listener(
            onError = { _, throwable ->
                Timber.e(throwable, "Image loading failed: $url")
            }
        )
    }
}

/**
 * Get drawable from resources
 */
fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

/**
 * Format date
 */
fun Date.format(pattern: String = "yyyy-MM-dd"): String {
    return try {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.format(this)
    } catch (e: Exception) {
        Timber.e(e, "Error formatting date")
        ""
    }
}

/**
 * Observe LiveData once
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

/**
 * Safe API call with error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T,
    onError: (Exception) -> Unit
): T? {
    return try {
        apiCall.invoke()
    } catch (e: Exception) {
        Timber.e(e, "API call failed")
        onError(e)
        null
    }
}