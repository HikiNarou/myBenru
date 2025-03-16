package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to get a novel by its ID
 */
class GetNovelUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<GetNovelUseCase.Params, Novel> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The requested novel
     */
    override suspend fun execute(parameters: Params): Novel {
        return novelRepository.getNovel(
            novelId = parameters.novelId,
            forceRefresh = parameters.forceRefresh
        )
    }

    /**
     * Parameters for [GetNovelUseCase]
     */
    data class Params(
        val novelId: String,
        val forceRefresh: Boolean = false
    )
}