package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get recently read novels
 */
class GetRecentlyReadNovelsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<Int, List<Novel>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Maximum number of novels to return
     * @return List of recently read novels
     */
    override suspend fun execute(parameters: Int): List<Novel> {
        return novelRepository.getRecentlyReadNovels(parameters)
    }
}