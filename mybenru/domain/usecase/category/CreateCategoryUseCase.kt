package com.mybenru.domain.usecase.category

import com.mybenru.domain.executor.UseCase
import com.mybenru.domain.model.Category
import com.mybenru.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case to create a new category
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : UseCase<Category, Category> {

    /**
     * Execute the use case with the given parameters
     *
     * @param parameters The category to create
     * @return The created category
     */
    override suspend fun execute(parameters: Category): Category {
        return categoryRepository.createCategory(parameters)
    }
}