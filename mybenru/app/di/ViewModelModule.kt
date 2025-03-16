package com.mybenru.app.di

import com.mybenru.domain.usecase.*
import com.mybenru.app.viewmodel.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideHomeViewModel(
        getRecentNovelsUseCase: GetRecentNovelsUseCase,
        getPopularNovelsUseCase: GetPopularNovelsUseCase,
        getRecommendedNovelsUseCase: GetRecommendedNovelsUseCase,
        dispatchers: AppCoroutineDispatchers
    ): HomeViewModel {
        return HomeViewModel(
            getRecentNovelsUseCase,
            getPopularNovelsUseCase,
            getRecommendedNovelsUseCase,
            dispatchers
        )
    }

    @ViewModelScoped
    @Provides
    fun provideLibraryViewModel(
        getLibraryNovelsUseCase: GetLibraryNovelsUseCase,
        updateLibraryUseCase: UpdateLibraryUseCase,
        removeFromLibraryUseCase: RemoveFromLibraryUseCase,
        dispatchers: AppCoroutineDispatchers
    ): LibraryViewModel {
        return LibraryViewModel(
            getLibraryNovelsUseCase,
            updateLibraryUseCase,
            removeFromLibraryUseCase,
            dispatchers
        )
    }

    @ViewModelScoped
    @Provides
    fun provideExploreViewModel(
        searchNovelUseCase: SearchNovelUseCase,
        getNovelCategoriesUseCase: GetNovelCategoriesUseCase,
        getCategoryNovelsUseCase: GetCategoryNovelsUseCase,
        dispatchers: AppCoroutineDispatchers
    ): ExploreViewModel {
        return ExploreViewModel(
            searchNovelUseCase,
            getNovelCategoriesUseCase,
            getCategoryNovelsUseCase,
            dispatchers
        )
    }

    @ViewModelScoped
    @Provides
    fun provideNovelDetailViewModel(
        getNovelDetailUseCase: GetNovelDetailUseCase,
        getChapterListUseCase: GetChapterListUseCase,
        addToLibraryUseCase: AddToLibraryUseCase,
        isInLibraryUseCase: IsInLibraryUseCase,
        dispatchers: AppCoroutineDispatchers
    ): NovelDetailViewModel {
        return NovelDetailViewModel(
            getNovelDetailUseCase,
            getChapterListUseCase,
            addToLibraryUseCase,
            isInLibraryUseCase,
            dispatchers
        )
    }

    @ViewModelScoped
    @Provides
    fun provideReaderViewModel(
        getChapterContentUseCase: GetChapterContentUseCase,
        saveReadingProgressUseCase: SaveReadingProgressUseCase,
        getReadingProgressUseCase: GetReadingProgressUseCase,
        getNextChapterUseCase: GetNextChapterUseCase,
        getPreviousChapterUseCase: GetPreviousChapterUseCase,
        getReaderSettingsUseCase: GetReaderSettingsUseCase,
        saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
        dispatchers: AppCoroutineDispatchers
    ): ReaderViewModel {
        return ReaderViewModel(
            getChapterContentUseCase,
            saveReadingProgressUseCase,
            getReadingProgressUseCase,
            getNextChapterUseCase,
            getPreviousChapterUseCase,
            getReaderSettingsUseCase,
            saveReaderSettingsUseCase,
            dispatchers
        )
    }

    @ViewModelScoped
    @Provides
    fun provideSettingsViewModel(
        getAppSettingsUseCase: GetAppSettingsUseCase,
        saveAppSettingsUseCase: SaveAppSettingsUseCase,
        clearCacheUseCase: ClearCacheUseCase,
        backupDataUseCase: BackupDataUseCase,
        restoreDataUseCase: RestoreDataUseCase,
        dispatchers: AppCoroutineDispatchers
    ): SettingsViewModel {
        return SettingsViewModel(
            getAppSettingsUseCase,
            saveAppSettingsUseCase,
            clearCacheUseCase,
            backupDataUseCase,
            restoreDataUseCase,
            dispatchers
        )
    }

    /**
     * Module for providing ViewModels with Hilt
     */
    @Module
    @InstallIn(ActivityRetainedComponent::class)
    abstract class ViewModelModule {

        @Binds
        @ActivityRetainedScoped
        abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

        @Binds
        @IntoMap
        @ViewModelKey(HomeViewModel::class)
        abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(LibraryViewModel::class)
        abstract fun bindLibraryViewModel(viewModel: LibraryViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(ExploreViewModel::class)
        abstract fun bindExploreViewModel(viewModel: ExploreViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(NovelDetailViewModel::class)
        abstract fun bindNovelDetailViewModel(viewModel: NovelDetailViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(ReaderViewModel::class)
        abstract fun bindReaderViewModel(viewModel: ReaderViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(SettingsViewModel::class)
        abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
    }
}
