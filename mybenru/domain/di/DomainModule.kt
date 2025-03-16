package com.mybenru.domain.di

import com.mybenru.domain.executor.UseCaseExecutor
import com.mybenru.domain.executor.UseCaseExecutorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Dagger module for domain layer dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    /**
     * Provide a [UseCaseExecutor] instance
     *
     * @return The [UseCaseExecutor] instance
     */
    @Provides
    @Singleton
    fun provideUseCaseExecutor(): UseCaseExecutor {
        return UseCaseExecutorImpl(Dispatchers.IO)
    }
}