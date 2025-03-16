package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get related novels for a given novel
 */
class GetRelatedNovelsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<GetRelatedNovelsUseCase.Params, List<Novel>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return List of related novels
     */
    override suspend fun execute(parameters: Params): List<Novel> {
        return novelRepository.getRelatedNovels(
            novelId = parameters.novelId,
            limit = parameters.limit
        )
    }

    /**
     * Parameters for [GetRelatedNovelsUseCase]
     */
    data class Params(
        val novelId: String,
        val limit: Int = 5
    )
}