package com.mybenru.domain.preference

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Class holding coroutine dispatchers for different types of tasks
 */
data class AppCoroutineDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher
)