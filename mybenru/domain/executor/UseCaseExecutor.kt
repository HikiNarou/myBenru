package com.mybenru.domain.executor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Executor for use cases
 */
interface UseCaseExecutor {
    /**
     * Execute a use case with parameters
     *
     * @param P Type of parameters
     * @param R Type of result
     * @param useCase Use case to execute
     * @param parameters Parameters for the use case
     * @return Result of the use case execution
     */
    suspend fun <P, R> execute(
        useCase: UseCase<P, R>,
        parameters: P
    ): R

    /**
     * Execute a use case without parameters
     *
     * @param R Type of result
     * @param useCase Use case to execute
     * @return Result of the use case execution
     */
    suspend fun <R> execute(
        useCase: NoParamsUseCase<R>
    ): R
}

/**
 * Implementation of [UseCaseExecutor]
 */
class UseCaseExecutorImpl(
    private val dispatcher: CoroutineDispatcher
) : UseCaseExecutor {
    /**
     * Execute a use case with parameters
     *
     * @param P Type of parameters
     * @param R Type of result
     * @param useCase Use case to execute
     * @param parameters Parameters for the use case
     * @return Result of the use case execution
     */
    override suspend fun <P, R> execute(
        useCase: UseCase<P, R>,
        parameters: P
    ): R = withContext(dispatcher) {
        useCase.execute(parameters)
    }

    /**
     * Execute a use case without parameters
     *
     * @param R Type of result
     * @param useCase Use case to execute
     * @return Result of the use case execution
     */
    override suspend fun <R> execute(
        useCase: NoParamsUseCase<R>
    ): R = withContext(dispatcher) {
        useCase.execute()
    }
}