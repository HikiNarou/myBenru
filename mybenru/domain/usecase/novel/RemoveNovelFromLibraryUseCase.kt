package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.repository.NovelRepository
import javax.inject.Inject

/**
 * Use case to remove a novel from the library
 */
class RemoveNovelFromLibraryUseCase @Inject constructor(
    private val novelRepository: NovelRepository
) : UseCase<String, Boolean> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the novel to remove
     * @return True if the novel was removed, false otherwise
     */
    override suspend fun execute(parameters: String): Boolean {
        return novelRepository.removeNovelFromLibrary(parameters)
    }
}