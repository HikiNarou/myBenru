package com.mybenru.domain.usecase.reader

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.ChapterRepository
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get the reading progress for a novel
 */
class GetReadingProgressUseCase @Inject constructor(
    private val novelRepository: NovelRepository,
    private val chapterRepository: ChapterRepository
) : UseCase<String, Float> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the novel
     * @return The reading progress (0.0-1.0)
     */
    override suspend fun execute(parameters: String): Float {
        // Get all chapters for the novel
        val chapters = chapterRepository.getChapters(parameters)

        // If there are no chapters, return 0
        if (chapters.isEmpty()) {
            return 0f
        }

        // Count read chapters
        val readChapters = chapters.count { it.isRead }

        // Calculate progress
        return readChapters.toFloat() / chapters.size
    }
}