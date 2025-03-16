package com.mybenru.domain.executor

/**
 * Base interface for use cases that take parameters
 *
 * @param P Type of parameters
 * @param R Type of result
 */
interface UseCase<in P, out R> {
    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Result of the use case execution
     */
    suspend fun execute(parameters: P): R
}

/**
 * Base interface for use cases that don't take parameters
 *
 * @param R Type of result
 */
interface NoParamsUseCase<out R> {
    /**
     * Execute the use case without parameters
     *
     * @return Result of the use case execution
     */
    suspend fun execute(): R
}

/**
 * Base interface for use cases that don't return results
 *
 * @param P Type of parameters
 */
interface CompletableUseCase<in P> {
    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     */
    suspend fun execute(parameters: P)
}

/**
 * Base interface for use cases that don't take parameters and don't return results
 */
interface CompletableNoParamsUseCase {
    /**
     * Execute the use case without parameters
     */
    suspend fun execute()
}