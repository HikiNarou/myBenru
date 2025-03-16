package com.mybenru.domain.usecase.bookmark

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.Chapter
import com.mybenru.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all bookmarked chapters
 */
class GetBookmarkedChaptersUseCase @Inject constructor(
    private val chapterRepository: ChapterRepository
) : NoParamsUseCase<Flow<List<Chapter>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of bookmarked chapters
     */
    override suspend fun execute(): Flow<List<Chapter>> {
        return chapterRepository.getBookmarkedChapters()
    }
}