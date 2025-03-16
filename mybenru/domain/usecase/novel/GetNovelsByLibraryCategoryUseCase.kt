package com.mybenru.domain.usecase.novel

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Novel
import com.mybenru.domain.repository.CategoryRepository
import com.mybenru.domain.repository.NovelRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to get all novels in a library category
 */
class GetNovelsByLibraryCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val novelRepository: NovelRepository
) : UseCase<String, List<Novel>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The ID of the library category
     * @return List of novels in the category
     */
    override suspend fun execute(parameters: String): List<Novel> {
        // Get all novels in the library
        val allNovels = novelRepository.getLibraryNovels().first()

        // Get novel IDs in the category
        val novelIds = categoryRepository.getNovelsInLibraryCategory(parameters)

        // Return only novels that are in the category
        return allNovels.filter { it.id in novelIds }
    }
}