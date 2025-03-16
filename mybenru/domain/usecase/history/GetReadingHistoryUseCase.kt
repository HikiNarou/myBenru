package com.mybenru.domain.usecase.history

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to get reading history (recently read chapters)
 */
class GetReadingHistoryUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<Int, List<Chapter>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Maximum number of chapters to return
     * @return List of recently read chapters
     */
    override suspend fun execute(parameters: Int): List<Chapter> {
        return chapterRepository.getRecentlyReadChapters(parameters)
    }
}