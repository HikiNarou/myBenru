package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to add a novel to the library
 */
class AddNovelToLibraryUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<Novel, Novel> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The novel to add to the library
     * @return The updated novel
     */
    override suspend fun execute(parameters: Novel): Novel {
        return novelRepository.addNovelToLibrary(parameters)
    }
}