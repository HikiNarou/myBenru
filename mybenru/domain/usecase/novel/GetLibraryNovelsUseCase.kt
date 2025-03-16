package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all novels in the library
 */
class GetLibraryNovelsUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : NoParamsUseCase<Flow<List<Novel>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of all novels in the library
     */
    override suspend fun execute(): Flow<List<Novel>> {
        return novelRepository.getLibraryNovels()
    }
}