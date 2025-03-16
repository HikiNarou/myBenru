package com.mybenru.app.di

import android.content.Context
import com.mybenru.app.utils.AppCoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Module for providing app-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides application context
     */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    /**
     * Provides coroutine dispatchers
     */
    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): AppCoroutineDispatchers {
        return AppCoroutineDispatchers()
    }
}