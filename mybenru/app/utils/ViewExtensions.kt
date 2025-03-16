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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.snackbar.Snackbar
import com.mybenru.app.R
import java.text.SimpleDateFormat
import java.util.*
import timber.log.Timber

// =======================================================
// VIEW EXTENSIONS
// =======================================================

/**
 * Mengatur view agar terlihat (VISIBLE)
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Mengatur view agar tidak terlihat dan tidak mengambil ruang (GONE)
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Mengatur view agar tidak terlihat namun tetap mengambil ruang (INVISIBLE)
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Mengubah visibility view antara VISIBLE dan GONE
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Menyembunyikan keyboard
 */
fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Menampilkan keyboard
 */
fun View.showKeyboard() {
    requestFocus()
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

// =======================================================
// SNACKBAR EXTENSIONS
// =======================================================

/**
 * Menampilkan snackbar dengan pesan.
 * Dapat juga menyediakan teks aksi dan lambda untuk menangani aksi tersebut.
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
 * Menampilkan snackbar dengan pesan yang diambil dari resource.
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
 * Menampilkan snackbar dengan pesan dan aksi.
 * Versi lambda tanpa parameter.
 */
fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_LONG,
    action: () -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

/**
 * Menampilkan snackbar dengan pesan dan aksi.
 * Versi lambda dengan parameter View.
 */
fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_LONG,
    action: (View) -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText, action)
        .show()
}

// =======================================================
// TOAST EXTENSIONS
// =======================================================

/**
 * Menampilkan Toast dengan pesan
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Menampilkan Toast dengan pesan dari resource
 */
fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(messageRes), duration).show()
}

/**
 * Menampilkan Toast dari Fragment dengan pesan
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

/**
 * Menampilkan Toast dari Fragment dengan pesan dari resource
 */
fun Fragment.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(messageRes, duration)
}

// =======================================================
// IMAGEVIEW EXTENSIONS
// =======================================================

/**
 * Memuat gambar ke dalam ImageView menggunakan Coil dengan opsi rounded corners.
 */
fun ImageView.loadImageCoil(
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
 * Memuat gambar ke dalam ImageView menggunakan Glide dengan opsi rounded corners.
 */
fun ImageView.loadImageGlide(
    url: String,
    placeholder: Int = R.drawable.placeholder_cover,
    error: Int = R.drawable.error_cover,
    cornerRadius: Float = 0f
) {
    val requestBuilder = Glide.with(this)
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .transition(DrawableTransitionOptions.withCrossFade())

    if (cornerRadius > 0f) {
        requestBuilder.transform(RoundedCorners(cornerRadius.toInt()))
    }

    requestBuilder.into(this)
}

// =======================================================
// UTILITY EXTENSIONS DAN FUNGSIONALITAS LAIN
// =======================================================

/**
 * Mengambil drawable secara kompatibel dari resource.
 */
fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

/**
 * Memformat objek Date menjadi string dengan pola tertentu.
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
 * Mengamati LiveData hanya sekali.
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
 * Melakukan API call secara aman dengan penanganan error.
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
