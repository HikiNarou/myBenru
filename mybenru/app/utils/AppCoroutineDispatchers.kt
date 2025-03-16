package com.mybenru.app.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coroutine dispatchers for the application
 * This helps standardize dispatcher usage and makes testing easier
 */
@Singleton
class AppCoroutineDispatchers @Inject constructor() {
    /**
     * Main dispatcher for UI operations
     */
    val main: CoroutineDispatcher = Dispatchers.Main

    /**
     * IO dispatcher for IO operations (disk, network, etc)
     */
    val io: CoroutineDispatcher = Dispatchers.IO

    /**
     * Default dispatcher for CPU-intensive operations
     */
    val default: CoroutineDispatcher = Dispatchers.Default

    /**
     * Unconfined dispatcher - runs in the current call frame
     */
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}