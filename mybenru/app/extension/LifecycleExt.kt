package com.mybenru.app.extension

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Observes LiveData within a lifecycle-aware coroutine scope
 */
inline fun <T> Flow<T>.collectIn(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect { action(it) }
        }
    }
}

/**
 * Launch a coroutine in the lifecycle scope
 */
fun LifecycleOwner.launchWhenStarted(block: suspend CoroutineScope.() -> Unit): Job {
    return lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }
}

/**
 * Launch a coroutine in the lifecycle scope
 */
fun LifecycleOwner.launchWhenResumed(block: suspend CoroutineScope.() -> Unit): Job {
    return lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED, block)
    }
}

/**
 * Launch a coroutine in the lifecycle scope
 */
fun LifecycleOwner.launchWhenCreated(block: suspend CoroutineScope.() -> Unit): Job {
    return lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED, block)
    }
}

/**
 * Extension function to automatically dispose when the lifecycle is destroyed
 */
fun <T> LiveData<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    observer: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            this@observeWithLifecycle.observe(lifecycleOwner, Observer { observer(it) })
        }
    }
}