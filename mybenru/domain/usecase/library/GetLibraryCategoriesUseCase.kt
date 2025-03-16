package com.mybenru.domain.usecase.library

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.LibraryCategory
import com.mybenru.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all library categories
 */
class GetLibraryCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : NoParamsUseCase<Flow<List<LibraryCategory>>> {

    /**
     * Execute the use case without parameters
     *
     * @return Flow of all library categories
     */
    override suspend fun execute(): Flow<List<LibraryCategory>> {
        return categoryRepository.getAllLibraryCategories()
    }
}