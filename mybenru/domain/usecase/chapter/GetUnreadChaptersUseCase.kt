package com.mybenru.domain.usecase.chapter

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import javax.inject.Inject

/**
 * Use case to get all unread chapters for a novel
 */
class GetUnreadChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : UseCase<String, List<Chapter>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the novel
     * @return List of unread chapters for the novel
     */
    override suspend fun execute(parameters: String): List<Chapter> {
        return chapterRepository.getUnreadChapters(parameters)
    }
}