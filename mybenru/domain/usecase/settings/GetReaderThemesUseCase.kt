package com.mybenru.domain.usecase.settings

import com.mybenru.domain.executor.NoParamsUseCase
import com.mybenru.domain.model.ReaderTheme
import com.mybenru.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/**
 * Use case to get all reader themes
 */
class GetReaderThemesUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : NoParamsUseCase<List<ReaderTheme>> {

    /**
     * Execute the use case without parameters
     *
     * @return List of reader themes
     */
    override suspend fun execute(): List<ReaderTheme> {
        return userPreferenceRepository.getReaderThemes()
    }
}