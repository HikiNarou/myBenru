package com.mybenru.domain.usecase.category

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Category
import com.mybenru.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all categories
 */
class GetAllCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : UseCase<GetAllCategoriesUseCase.Params, Flow<List<Category>>> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters Parameters for the use case
     * @return Flow of all categories
     */
    override suspend fun execute(parameters: Params): Flow<List<Category>> {
        return categoryRepository.getAllCategories(parameters.sourceId)
    }

    /**
     * Parameters for [GetAllCategoriesUseCase]
     */
    data class Params(
        val sourceId: String? = null
    )
}