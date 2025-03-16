package com.mybenru.domain.usecase.library

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.LibraryCategory
import com.mybenru.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case to add a novel to a library category
 */
class AddNovelToLibraryCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : UseCase<AddNovelToLibraryCategoryUseCase.Params, LibraryCategory> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return The updated library category
     */
    override suspend fun execute(parameters: Params): LibraryCategory {
        return categoryRepository.addNovelToLibraryCategory(
            novelId = parameters.novelId,
            categoryId = parameters.categoryId
        )
    }

    /**
     * Parameters for [AddNovelToLibraryCategoryUseCase]
     */
    data class Params(
        val novelId: String,
        val categoryId: String
    )
}