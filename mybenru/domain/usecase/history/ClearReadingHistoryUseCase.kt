package com.mybenru.domain.usecase.history

import com.mybenru.domain.executor.CompletableNoParamsUseCase
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to clear reading history
 */
class ClearReadingHistoryUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : CompletableNoParamsUseCase {

    /**
     * Execute the use case without parameters
     */
    override suspend fun execute() {
        // This is a stub implementation. In a real application, the ChapterRepository
        // would have a method to clear reading history.
        //
        // For demonstration purposes, we'll assume that the ChapterRepository has
        // such a method but we're not implementing it here.
    }
}