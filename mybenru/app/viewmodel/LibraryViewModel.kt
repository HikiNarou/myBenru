package com.mybenru.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.mapper.NovelUiMapper
import com.mybenru.app.model.LibraryFilterUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.domain.model.ChapterDetail
import com.mybenru.domain.model.LibraryFilter
import com.mybenru.domain.model.NovelSort
import com.mybenru.domain.usecase.GetChapterDetailUseCase
import com.mybenru.domain.usecase.GetFirstChapterUseCase
import com.mybenru.domain.usecase.GetLibraryNovelsUseCase
import com.mybenru.domain.usecase.RemoveNovelFromLibraryUseCase
import com.mybenru.domain.usecase.UpdateLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the library screen, managing user's novel collection
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraryNovelsUseCase: GetLibraryNovelsUseCase,
    private val removeNovelFromLibraryUseCase: RemoveNovelFromLibraryUseCase,
    private val updateLibraryUseCase: UpdateLibraryUseCase,
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val getFirstChapterUseCase: GetFirstChapterUseCase,
    private val novelUiMapper: NovelUiMapper,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState

    // Library novels
    private val _novels = MutableLiveData<List<NovelUiModel>>()
    val novels: LiveData<List<NovelUiModel>> = _novels

    // All novels (unfiltered)
    private var allNovels = emptyList<NovelUiModel>()

    // Novel count
    private val _novelCount = MutableLiveData(0)
    val novelCount: LiveData<Int> = _novelCount

    // Filters
    private val _filters = MutableLiveData<List<LibraryFilterUiModel>>()
    val filters: LiveData<List<LibraryFilterUiModel>> = _filters

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // Current filter state
    private var activeFilters = mutableListOf<LibraryFilterUiModel>()
    private var currentFilterType = LibraryFilterUiModel.FilterType.ALL
    private var currentSortOption = 0 // 0: Alphabetical, 1: Last read, 2: Last updated, etc.

    // Search query
    private var currentSearchQuery = ""

    // Library metadata
    private var availableAuthors = emptyList<String>()
    private var availableGenres = emptyList<String>()
    private var selectedAuthor: String? = null
    private var selectedGenre: String? = null
    private var selectedStatus: String? = null

    /**
     * Load library novels
     */
    fun loadLibrary() {
        _uiState.value = LibraryUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                // Get library filters based on current state
                val libraryFilters = getLibraryFilters()

                // Get sort option
                val sortOption = getNovelSort()

                // Get novels from the use case
                val novels = getLibraryNovelsUseCase.execute(
                    GetLibraryNovelsUseCase.Params(libraryFilters, sortOption)
                )

                // Map to UI models
                val novelUiModels = novelUiMapper.mapToUiModels(novels)

                // Save all novels for filtering
                allNovels = novelUiModels

                // Apply search filter if needed
                val filteredNovels = if (currentSearchQuery.isBlank()) {
                    novelUiModels
                } else {
                    applySearchFilter(novelUiModels, currentSearchQuery)
                }

                // Extract metadata
                extractLibraryMetadata(novelUiModels)

                // Update UI state
                if (novelUiModels.isEmpty()) {
                    _uiState.value = LibraryUiState.Empty
                } else if (filteredNovels.isEmpty()) {
                    _uiState.value = LibraryUiState.NoResults
                } else {
                    _uiState.value = LibraryUiState.Success
                }

                // Update observables
                _novels.postValue(filteredNovels)
                _novelCount.postValue(filteredNovels.size)

            } catch (e: Exception) {
                Timber.e(e, "Failed to load library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load library")
                _uiState.value = LibraryUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Refresh library data from sources
     */
    fun refreshLibrary() {
        _uiState.value = LibraryUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                // Update library from sources
                val result = updateLibraryUseCase.execute(Unit)

                Timber.d("Library update result: ${result.updatedNovels.size} novels updated")

                // Reload library after update
                loadLibrary()

            } catch (e: Exception) {
                Timber.e(e, "Failed to update library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to update library")
                _uiState.value = LibraryUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Search novels in the library
     */
    fun searchLibrary(query: String) {
        currentSearchQuery = query.trim()

        // Apply search filter to all novels
        val filteredNovels = if (currentSearchQuery.isBlank()) {
            allNovels
        } else {
            applySearchFilter(allNovels, currentSearchQuery)
        }

        // Update UI state
        if (filteredNovels.isEmpty() && currentSearchQuery.isNotBlank()) {
            _uiState.value = LibraryUiState.NoResults
        } else if (filteredNovels.isEmpty()) {
            _uiState.value = LibraryUiState.Empty
        } else {
            _uiState.value = LibraryUiState.Success
        }

        // Update observables
        _novels.postValue(filteredNovels)
        _novelCount.postValue(filteredNovels.size)
    }

    /**
     * Apply search filter to novels
     */
    private fun applySearchFilter(novels: List<NovelUiModel>, query: String): List<NovelUiModel> {
        val lowerQuery = query.lowercase()
        return novels.filter { novel ->
            novel.title.lowercase().contains(lowerQuery) ||
                    novel.getFormattedAuthors().lowercase().contains(lowerQuery) ||
                    novel.getFormattedGenres().lowercase().contains(lowerQuery) ||
                    novel.description.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Set active filter type
     */
    fun setActiveFilter(filterType: LibraryFilterUiModel.FilterType) {
        currentFilterType = filterType

        // Reset special filters
        if (filterType != LibraryFilterUiModel.FilterType.BY_AUTHOR) selectedAuthor = null
        if (filterType != LibraryFilterUiModel.FilterType.BY_GENRE) selectedGenre = null
        if (filterType != LibraryFilterUiModel.FilterType.BY_STATUS) selectedStatus = null

        // Reload library with new filter
        loadLibrary()
    }

    /**
     * Get the current filter type
     */
    fun getCurrentFilterType(): LibraryFilterUiModel.FilterType {
        return currentFilterType
    }

    /**
     * Get current sort index
     */
    fun getCurrentSortIndex(): Int {
        return currentSortOption
    }

    /**
     * Set sort option
     */
    fun setSortOption(sortIndex: Int) {
        currentSortOption = sortIndex
        loadLibrary()
    }

    /**
     * Convert UI filter type to domain filter
     */
    private fun getLibraryFilters(): List<LibraryFilter> {
        val filters = mutableListOf<LibraryFilter>()

        when (currentFilterType) {
            LibraryFilterUiModel.FilterType.UNREAD -> {
                filters.add(LibraryFilter.UnreadFilter)
            }
            LibraryFilterUiModel.FilterType.COMPLETED -> {
                filters.add(LibraryFilter.CompletedFilter)
            }
            LibraryFilterUiModel.FilterType.DOWNLOADED -> {
                filters.add(LibraryFilter.DownloadedFilter)
            }
            LibraryFilterUiModel.FilterType.BY_AUTHOR -> {
                selectedAuthor?.let {
                    filters.add(LibraryFilter.AuthorFilter(it))
                }
            }
            LibraryFilterUiModel.FilterType.BY_GENRE -> {
                selectedGenre?.let {
                    filters.add(LibraryFilter.GenreFilter(it))
                }
            }
            LibraryFilterUiModel.FilterType.BY_STATUS -> {
                selectedStatus?.let {
                    filters.add(LibraryFilter.StatusFilter(it))
                }
            }
            LibraryFilterUiModel.FilterType.RECENTLY_READ -> {
                filters.add(LibraryFilter.RecentlyReadFilter)
            }
            LibraryFilterUiModel.FilterType.RECENTLY_ADDED -> {
                filters.add(LibraryFilter.RecentlyAddedFilter)
            }
            LibraryFilterUiModel.FilterType.ALL -> {
                // No filters for "All"
            }
        }

        // Add active custom filters if any
        activeFilters.forEach { filter ->
            when (filter.filterType) {
                // Already handled above
                LibraryFilterUiModel.FilterType.ALL,
                LibraryFilterUiModel.FilterType.UNREAD,
                LibraryFilterUiModel.FilterType.COMPLETED,
                LibraryFilterUiModel.FilterType.DOWNLOADED,
                LibraryFilterUiModel.FilterType.BY_AUTHOR,
                LibraryFilterUiModel.FilterType.BY_GENRE,
                LibraryFilterUiModel.FilterType.BY_STATUS,
                LibraryFilterUiModel.FilterType.RECENTLY_READ,
                LibraryFilterUiModel.FilterType.RECENTLY_ADDED -> {}
            }
        }

        return filters
    }

    /**
     * Get novel sort option
     */
    private fun getNovelSort(): NovelSort {
        return when (currentSortOption) {
            0 -> NovelSort.Alphabetical
            1 -> NovelSort.LastRead
            2 -> NovelSort.LastUpdated
            3 -> NovelSort.DateAdded
            4 -> NovelSort.UnreadCount
            else -> NovelSort.Alphabetical
        }
    }

    /**
     * Extract metadata from library novels for filtering
     */
    private fun extractLibraryMetadata(novels: List<NovelUiModel>) {
        // Extract unique authors
        val authorSet = mutableSetOf<String>()
        novels.forEach { novel ->
            novel.authors.forEach { author ->
                authorSet.add(author)
            }
        }
        availableAuthors = authorSet.toList().sorted()

        // Extract unique genres
        val genreSet = mutableSetOf<String>()
        novels.forEach { novel ->
            novel.genres.forEach { genre ->
                genreSet.add(genre)
            }
        }
        availableGenres = genreSet.toList().sorted()
    }

    /**
     * Toggle a filter
     */
    fun toggleFilter(filter: LibraryFilterUiModel) {
        val index = activeFilters.indexOfFirst { it.filterType == filter.filterType }
        if (index != -1) {
            activeFilters.removeAt(index)
        } else {
            activeFilters.add(filter)
        }

        updateFiltersList()
        loadLibrary()
    }

    /**
     * Update the filters list
     */
    private fun updateFiltersList() {
        _filters.postValue(activeFilters)
    }

    /**
     * Remove a novel from the library
     */
    fun removeNovelFromLibrary(novel: NovelUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val domainNovel = novelUiMapper.mapToDomainModel(novel)
                removeNovelFromLibraryUseCase.execute(domainNovel)

                // Refresh library after removal
                loadLibrary()

            } catch (e: Exception) {
                Timber.e(e, "Failed to remove novel from library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to remove novel from library")
            }
        }
    }

    /**
     * Get chapter detail for a specific novel and chapter
     */
    fun getChapterDetail(
        novelId: String,
        chapterId: String,
        callback: (ChapterDetail?) -> Unit
    ) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val params = GetChapterDetailUseCase.Params(novelId, chapterId)
                val chapterDetail = getChapterDetailUseCase.execute(params)
                callback(chapterDetail)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get chapter detail")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to get chapter detail")
                callback(null)
            }
        }
    }

    /**
     * Get the first chapter for a novel
     */
    fun getFirstChapter(novelId: String, callback: (ChapterDetail?) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val firstChapter = getFirstChapterUseCase.execute(novelId)
                callback(firstChapter)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get first chapter")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to get first chapter")
                callback(null)
            }
        }
    }

    /**
     * Get available authors for filtering
     */
    fun getAvailableAuthors(): List<String> {
        return availableAuthors
    }

    /**
     * Get available genres for filtering
     */
    fun getAvailableGenres(): List<String> {
        return availableGenres
    }

    /**
     * Apply author filter
     */
    fun applyAuthorFilter(author: String) {
        selectedAuthor = author
        loadLibrary()
    }

    /**
     * Apply genre filter
     */
    fun applyGenreFilter(genre: String) {
        selectedGenre = genre
        loadLibrary()
    }

    /**
     * Apply status filter
     */
    fun applyStatusFilter(status: String) {
        selectedStatus = status
        loadLibrary()
    }

    /**
     * UI states for the library screen
     */
    sealed class LibraryUiState {
        data object Loading : LibraryUiState()
        data object Success : LibraryUiState()
        data object Empty : LibraryUiState()
        data object NoResults : LibraryUiState()
        data class Error(val message: String) : LibraryUiState()
    }
}