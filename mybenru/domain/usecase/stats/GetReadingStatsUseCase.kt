package com.mybenru.domain.usecase.stats

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.ReadingStats
import com.mybenru.domain.repository.ChapterRepository
import com.mybenru.domain.repository.NovelRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case to get reading statistics
 */
class GetReadingStatsUseCase @Inject constructor(
    private val novelRepository: NovelRepository,
    private val chapterRepository: ChapterRepository
) : NoParamsUseCase<ReadingStats> {

    /**
     * Execute the use case without parameters
     *
     * @return Reading statistics
     */
    override suspend fun execute(): ReadingStats {
        // Get all completed novels
        val libraryNovels = novelRepository.getLibraryNovels().first()
        val completedNovels = libraryNovels.count { it.readingProgress >= 100 }

        // Get all read chapters
        var totalReadChapters = 0
        var totalWordCount = 0
        var totalReadingTime = 0L

        // Process each novel in the library
        libraryNovels.forEach { novel ->
            // Get read chapters for this novel
            val readChapters = chapterRepository.getReadChapters(novel.id)

            // Update statistics
            totalReadChapters += readChapters.size

            // Sum up word counts for read chapters
            readChapters.forEach { chapter ->
                totalWordCount += chapter.wordCount

                // Estimate reading time based on word count
                // Assuming average reading speed of 250 words per minute
                val readingTimeMinutes = if (chapter.wordCount > 0) {
                    chapter.wordCount / 250
                } else {
                    // Default estimate if word count is not available
                    5
                }

                totalReadingTime += TimeUnit.MINUTES.toMillis(readingTimeMinutes.toLong())
            }
        }

        // Return reading statistics
        return ReadingStats(
            novelsInLibrary = libraryNovels.size,
            completedNovels = completedNovels,
            chaptersRead = totalReadChapters,
            totalWordCount = totalWordCount,
            totalReadingTimeMs = totalReadingTime,
            averageChaptersPerDay = calculateAverageChaptersPerDay(totalReadChapters, totalReadingTime)
        )
    }

    /**
     * Calculate average chapters read per day
     *
     * @param totalChapters Total number of chapters read
     * @param totalReadingTimeMs Total reading time in milliseconds
     * @return Average chapters read per day
     */
    private fun calculateAverageChaptersPerDay(totalChapters: Int, totalReadingTimeMs: Long): Float {
        // If there are no chapters or reading time, return 0
        if (totalChapters == 0 || totalReadingTimeMs == 0L) {
            return 0f
        }

        // Calculate average chapters per day based on total reading time
        val readingDays = TimeUnit.MILLISECONDS.toDays(totalReadingTimeMs).coerceAtLeast(1)
        return totalChapters.toFloat() / readingDays
    }
}