package com.mybenru.app.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * Extension function for observing LiveData with lifecycle awareness
 */
fun <T> LiveData<T>.observeWithLifecycle(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { value ->
        value?.let {
            observer(it)
        }
    }
}

/**
 * Extension function for observing LiveData once with lifecycle awareness
 */
fun <T> LiveData<T>.observeOnceWithLifecycle(owner: LifecycleOwner, observer: (T) -> Unit) {
    var firstTime = true
    observe(owner) { value ->
        if (firstTime && value != null) {
            firstTime = false
            observer(value)
        }
    }
}