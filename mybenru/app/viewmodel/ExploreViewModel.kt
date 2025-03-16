package com.mybenru.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.mapper.NovelUiMapper
import com.mybenru.app.model.CategoryUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.domain.usecase.AddNovelToLibraryUseCase
import com.mybenru.domain.usecase.GetCategoriesUseCase
import com.mybenru.domain.usecase.GetNovelsByCategoryUseCase
import com.mybenru.domain.usecase.RemoveNovelFromLibraryUseCase
import com.mybenru.domain.usecase.SearchNovelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Combined ViewModel untuk Explore/Discovery screen.
 * Mengelola pemuatan kategori, novel berdasarkan kategori, pencarian novel (dengan debouncing),
 * serta penambahan dan penghapusan novel dari library.
 */
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getNovelsByCategoryUseCase: GetNovelsByCategoryUseCase,
    private val searchNovelsUseCase: SearchNovelsUseCase,
    private val addNovelToLibraryUseCase: AddNovelToLibraryUseCase,
    private val removeNovelFromLibraryUseCase: RemoveNovelFromLibraryUseCase,
    private val novelUiMapper: NovelUiMapper,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    // UI state (menggabungkan status dari ketiga versi)
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState

    // List kategori
    private val _categories = MutableLiveData<List<CategoryUiModel>>()
    val categories: LiveData<List<CategoryUiModel>> = _categories

    // Kategori yang terpilih (dipakai untuk memuat novel)
    private val _selectedCategory = MutableLiveData<CategoryUiModel?>()
    val selectedCategory: LiveData<CategoryUiModel?> = _selectedCategory

    // List novel untuk kategori yang dipilih
    private val _categoryNovels = MutableLiveData<List<NovelUiModel>>()
    val categoryNovels: LiveData<List<NovelUiModel>> = _categoryNovels

    // Hasil pencarian novel
    private val _searchResults = MutableLiveData<List<NovelUiModel>>()
    val searchResults: LiveData<List<NovelUiModel>> = _searchResults

    // Query pencarian
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // Loading state tambahan
    private val _isCategoriesLoading = MutableLiveData(false)
    val isCategoriesLoading: LiveData<Boolean> = _isCategoriesLoading

    private val _isCategoryNovelsLoading = MutableLiveData(false)
    val isCategoryNovelsLoading: LiveData<Boolean> = _isCategoryNovelsLoading

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching

    // Search state dengan debouncing
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    private var searchJob: Job? = null
    private val searchDebounceTime = 500L // Milliseconds

    init {
        loadCategories()
    }

    /**
     * Memuat kategori novel menggunakan getCategoriesUseCase.
     */
    fun loadCategories() {
        _isCategoriesLoading.value = true
        _uiState.value = ExploreUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                val domainCategories = getCategoriesUseCase.execute(Unit)
                val categoryUiModels = domainCategories.map { category ->
                    CategoryUiModel(
                        id = category.id,
                        name = category.name,
                        description = category.description,
                        coverUrl = category.coverUrl,
                        novelCount = category.novelCount
                    )
                }
                _categories.postValue(categoryUiModels)
                _isCategoriesLoading.postValue(false)
                _uiState.value = ExploreUiState.CategoriesLoaded

                // Jika ada kategori, pilih kategori pertama secara default
                categoryUiModels.firstOrNull()?.let { firstCategory ->
                    selectCategory(firstCategory)
                } ?: run {
                    _uiState.value = ExploreUiState.Empty
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load categories")
                _isCategoriesLoading.postValue(false)
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Unknown error")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load categories")
            }
        }
    }

    /**
     * Memilih kategori dengan menggunakan objek CategoryUiModel.
     */
    fun selectCategory(category: CategoryUiModel) {
        _selectedCategory.value = category
        loadNovelsByCategory(category.id)
    }

    /**
     * Overload: Memilih kategori berdasarkan ID.
     */
    fun selectCategory(categoryId: String) {
        _categories.value?.find { it.id == categoryId }?.let {
            selectCategory(it)
        }
    }

    /**
     * Memuat novel untuk kategori tertentu menggunakan getNovelsByCategoryUseCase.
     */
    fun loadNovelsByCategory(categoryId: String) {
        _isCategoryNovelsLoading.value = true
        _uiState.value = ExploreUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                val novelsDomain = getNovelsByCategoryUseCase.execute(categoryId)
                val novelUiModels = novelUiMapper.mapToUiModels(novelsDomain)
                _categoryNovels.postValue(novelUiModels)
                _isCategoryNovelsLoading.postValue(false)
                _uiState.value = if (novelUiModels.isEmpty()) ExploreUiState.Empty else ExploreUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to load novels for category: $categoryId")
                _isCategoryNovelsLoading.postValue(false)
                _uiState.value = ExploreUiState.Error(e.localizedMessage ?: "Unknown error")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load novels")
            }
        }
    }

    /**
     * Melakukan pencarian novel dengan debouncing.
     */
    fun searchNovels(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchState.value = SearchState.Initial
            _searchResults.postValue(emptyList())
            return
        }

        _searchState.value = SearchState.InProgress
        _isSearching.value = true

        viewModelScope.launch {
            // Batalkan job pencarian sebelumnya jika masih aktif
            searchJob?.cancelAndJoin()
            searchJob = viewModelScope.launch(dispatchers.io) {
                delay(searchDebounceTime)
                try {
                    val novelDomains = searchNovelsUseCase.execute(query)
                    val novelUiModels = novelUiMapper.mapToUiModels(novelDomains)
                    processSearchResults(novelUiModels)
                    _isSearching.postValue(false)
                    _searchState.value = if (novelUiModels.isEmpty()) SearchState.NoResults else SearchState.HasResults
                } catch (e: Exception) {
                    Timber.e(e, "Error searching novels")
                    _isSearching.postValue(false)
                    _searchState.value = SearchState.Error(e.localizedMessage ?: "Search failed")
                    _errorEvent.postValue(e.localizedMessage ?: "Search failed")
                }
            }
        }
    }

    /**
     * Memproses hasil pencarian novel dan mengorganisasikannya berdasarkan relevansi.
     */
    private fun processSearchResults(novels: List<NovelUiModel>) {
        viewModelScope.launch(dispatchers.computation) {
            val organizedResults = organizeSearchResults(novels)
            _searchResults.postValue(organizedResults)
        }
    }

    /**
     * Mengorganisasikan hasil pencarian berdasarkan relevansi:
     * - Judul yang sama persis memiliki prioritas tertinggi,
     * - Judul yang diawali query, mengandung query, kemudian pencocokan penulis, genre, dan deskripsi.
     */
    private fun organizeSearchResults(novels: List<NovelUiModel>): List<NovelUiModel> {
        val query = _searchQuery.value ?: ""
        if (query.isBlank()) return novels

        return novels.sortedWith(compareBy<NovelUiModel> { novel ->
            when {
                novel.title.equals(query, ignoreCase = true) -> 0
                novel.title.startsWith(query, ignoreCase = true) -> 1
                novel.title.contains(query, ignoreCase = true) -> 2
                novel.getFormattedAuthors().contains(query, ignoreCase = true) -> 3
                novel.getFormattedGenres().contains(query, ignoreCase = true) -> 4
                novel.description.contains(query, ignoreCase = true) -> 5
                else -> 6
            }
        }.thenBy { it.title })
    }

    /**
     * Menghapus query pencarian dan mereset status pencarian.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.postValue(emptyList())
        _searchState.value = SearchState.Initial
    }

    /**
     * Menyegarkan data kategori dan novel untuk kategori yang terpilih.
     */
    fun refreshData() {
        loadCategories()
        _selectedCategory.value?.let { category ->
            loadNovelsByCategory(category.id)
        }
    }

    /**
     * Menambahkan novel ke library.
     */
    fun addNovelToLibrary(novel: NovelUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val domainNovel = novelUiMapper.mapToDomainModel(novel)
                addNovelToLibraryUseCase.execute(domainNovel)
                updateNovelInLists(novel.copy(isInLibrary = true))
            } catch (e: Exception) {
                Timber.e(e, "Failed to add novel to library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to add novel to library")
            }
        }
    }

    /**
     * Menghapus novel dari library.
     */
    fun removeNovelFromLibrary(novel: NovelUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val domainNovel = novelUiMapper.mapToDomainModel(novel)
                removeNovelFromLibraryUseCase.execute(domainNovel)
                updateNovelInLists(novel.copy(isInLibrary = false))
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove novel from library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to remove novel from library")
            }
        }
    }

    /**
     * Memperbarui novel di list kategori dan hasil pencarian.
     */
    private fun updateNovelInLists(updatedNovel: NovelUiModel) {
        _categoryNovels.value?.let { novels ->
            val updatedList = novels.map { if (it.id == updatedNovel.id) updatedNovel else it }
            _categoryNovels.postValue(updatedList)
        }
        _searchResults.value?.let { novels ->
            val updatedList = novels.map { if (it.id == updatedNovel.id) updatedNovel else it }
            _searchResults.postValue(updatedList)
        }
    }

    /**
     * UI states untuk Explore screen.
     */
    sealed class ExploreUiState {
        object Loading : ExploreUiState()
        object CategoriesLoaded : ExploreUiState()
        object Success : ExploreUiState()
        object Empty : ExploreUiState()
        data class Error(val message: String) : ExploreUiState()
    }

    /**
     * Search state untuk pencarian novel.
     */
    sealed class SearchState {
        object Initial : SearchState()
        object InProgress : SearchState()
        object HasResults : SearchState()
        object NoResults : SearchState()
        data class Error(val message: String) : SearchState()
    }
}
