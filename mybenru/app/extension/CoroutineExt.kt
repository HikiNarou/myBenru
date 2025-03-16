package com.mybenru.app.extension

import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Execute a suspending function with a specific timeout
 */
suspend fun <T> withTimeout(timeout: Long, block: suspend CoroutineScope.() -> T): T? {
    return try {
        kotlinx.coroutines.withTimeout(timeout) {
            block()
        }
    } catch (e: TimeoutCancellationException) {
        Timber.w("Coroutine execution timed out after $timeout ms")
        null
    } catch (e: Exception) {
        Timber.e(e, "Error executing coroutine with timeout")
        null
    }
}

/**
 * Execute a suspending function with try-catch
 */
suspend fun <T> safeCoroutine(
    onError: ((Exception) -> T?)? = null,
    block: suspend CoroutineScope.() -> T
): T? {
    return try {
        block(CoroutineScope(EmptyCoroutineContext))
    } catch (e: CancellationException) {
        Timber.d("Coroutine was cancelled")
        throw e  // Don't catch cancellation exceptions
    } catch (e: Exception) {
        Timber.e(e, "Error in coroutine execution")
        onError?.invoke(e)
    }
}

/**
 * Execute a suspending function in a specific context
 */
suspend fun <T> withContextAndTimeout(
    context: CoroutineContext,
    timeout: Long,
    block: suspend CoroutineScope.() -> T
): T? {
    return withContext(context) {
        withTimeout(timeout) {
            block()
        }
    }
}

/**
 * Create a single-use Deferred that completes after a delay
 */
fun CoroutineScope.delayedDeferred(delayMillis: Long, block: suspend CoroutineScope.() -> Unit): Deferred<Unit> {
    return async {
        delay(delayMillis)
        block()
    }
}

/**
 * Execute a fire-and-forget coroutine with error handling
 */
fun CoroutineScope.launchSafely(
    context: CoroutineContext = EmptyCoroutineContext,
    errorHandler: (Throwable) -> Unit = { Timber.e(it, "Error in coroutine") },
    block: suspend CoroutineScope.() -> Unit
): Job {
    val handler = CoroutineExceptionHandler { _, exception ->
        errorHandler(exception)
    }

    return this.launch(context + handler) {
        block()
    }
}

/**
 * Retry a suspending operation with exponential backoff
 */
suspend fun <T> retryWithExponentialBackoff(
    times: Int,
    initialDelayMillis: Long = 100,
    maxDelayMillis: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMillis
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            Timber.w("Retry attempt ${attempt + 1}/$times failed, retrying in $currentDelay ms")
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
    }
    // Last attempt
    return block()
}

/**
 * Debounce a suspending function
 */
fun <T> debounce(
    delayMillis: Long,
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(delayMillis)
            action(param)
        }
    }
}

/**
 * Throttle a suspending function
 */
fun <T> throttle(
    intervalMillis: Long,
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit
): (T) -> Unit {
    var lastExecution = 0L
    var scheduledExecution: Job? = null
    var pendingParam: T? = null

    return { param: T ->
        val now = System.currentTimeMillis()

        if (now - lastExecution >= intervalMillis) {
            // Execute immediately
            lastExecution = now
            coroutineScope.launch { action(param) }
        } else {
            // Schedule for later execution
            pendingParam = param
            if (scheduledExecution == null) {
                scheduledExecution = coroutineScope.launch {
                    delay(intervalMillis - (now - lastExecution))
                    pendingParam?.let {
                        lastExecution = System.currentTimeMillis()
                        action(it)
                        pendingParam = null
                    }
                    scheduledExecution = null
                }
            }
        }
    }
}

/**
 * Execute multiple suspending functions in parallel and wait for all to complete
 */
suspend fun <T> CoroutineScope.executeParallel(
    dispatchers: CoroutineContext = EmptyCoroutineContext,
    vararg blocks: suspend CoroutineScope.() -> T
): List<T> {
    return coroutineScope {
        blocks.map { block ->
            async(dispatchers) { block() }
        }.awaitAll()
    }
}