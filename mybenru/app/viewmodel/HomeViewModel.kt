package com.mybenru.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.mapper.NovelUiMapper
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.domain.model.ChapterDetail
import com.mybenru.domain.usecase.AddNovelToLibraryUseCase
import com.mybenru.domain.usecase.GetChapterDetailUseCase
import com.mybenru.domain.usecase.GetContinueReadingNovelsUseCase
import com.mybenru.domain.usecase.GetFirstChapterUseCase
import com.mybenru.domain.usecase.GetPopularNovelsUseCase
import com.mybenru.domain.usecase.GetRecentNovelsUseCase
import com.mybenru.domain.usecase.GetRecommendedNovelsUseCase
import com.mybenru.domain.usecase.RemoveNovelFromLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel untuk Home screen, menampilkan rekomendasi novel dan progress membaca.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getContinueReadingNovelsUseCase: GetContinueReadingNovelsUseCase,
    private val getRecentNovelsUseCase: GetRecentNovelsUseCase,
    private val getPopularNovelsUseCase: GetPopularNovelsUseCase,
    private val getRecommendedNovelsUseCase: GetRecommendedNovelsUseCase,
    private val addNovelToLibraryUseCase: AddNovelToLibraryUseCase,
    private val removeNovelFromLibraryUseCase: RemoveNovelFromLibraryUseCase,
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
    private val getFirstChapterUseCase: GetFirstChapterUseCase,
    private val novelUiMapper: NovelUiMapper,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    companion object {
        const val CONTINUE_READING_LIST_TYPE = "continue_reading"
        const val RECENT_NOVELS_LIST_TYPE = "recent"
        const val POPULAR_NOVELS_LIST_TYPE = "popular"
        const val RECOMMENDED_NOVELS_LIST_TYPE = "recommended"
        private const val PREVIEW_ITEMS_COUNT = 10
    }

    // UI state
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // Continue reading novels
    private val _continueReadingNovels = MutableLiveData<List<NovelUiModel>>()
    val continueReadingNovels: LiveData<List<NovelUiModel>> = _continueReadingNovels

    // Recent novels
    private val _recentNovels = MutableLiveData<List<NovelUiModel>>()
    val recentNovels: LiveData<List<NovelUiModel>> = _recentNovels

    // Popular novels
    private val _popularNovels = MutableLiveData<List<NovelUiModel>>()
    val popularNovels: LiveData<List<NovelUiModel>> = _popularNovels

    // Recommended novels
    private val _recommendedNovels = MutableLiveData<List<NovelUiModel>>()
    val recommendedNovels: LiveData<List<NovelUiModel>> = _recommendedNovels

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    /**
     * Memuat seluruh data untuk Home screen secara paralel.
     */
    fun loadHomeData() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                val continueReadingDeferred = viewModelScope.launch { loadContinueReadingNovels() }
                val recentNovelsDeferred = viewModelScope.launch { loadRecentNovels() }
                val popularNovelsDeferred = viewModelScope.launch { loadPopularNovels() }
                val recommendedNovelsDeferred = viewModelScope.launch { loadRecommendedNovels() }

                // Menunggu seluruh proses selesai
                continueReadingDeferred.join()
                recentNovelsDeferred.join()
                popularNovelsDeferred.join()
                recommendedNovelsDeferred.join()

                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to load home data")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load home data")
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Memuat novel yang sedang dilanjutkan (continue reading).
     * Menggunakan Flow dengan operator catch dan collect.
     */
    private suspend fun loadContinueReadingNovels() {
        getContinueReadingNovelsUseCase.execute(PREVIEW_ITEMS_COUNT)
            .catch { e ->
                Timber.e(e, "Failed to load continue reading novels")
                _errorEvent.postValue("Failed to load reading history")
            }
            .collect { novels ->
                val mappedNovels = novelUiMapper.mapToUiModels(novels)
                _continueReadingNovels.postValue(mappedNovels)
            }
    }

    /**
     * Memuat novel terbaru (recent).
     */
    private suspend fun loadRecentNovels() {
        try {
            val novels = getRecentNovelsUseCase.execute(PREVIEW_ITEMS_COUNT)
            val mappedNovels = novelUiMapper.mapToUiModels(novels)
            _recentNovels.postValue(mappedNovels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load recent novels")
            _recentNovels.postValue(emptyList())
        }
    }

    /**
     * Memuat novel populer (popular).
     */
    private suspend fun loadPopularNovels() {
        try {
            val novels = getPopularNovelsUseCase.execute(PREVIEW_ITEMS_COUNT)
            val mappedNovels = novelUiMapper.mapToUiModels(novels)
            _popularNovels.postValue(mappedNovels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load popular novels")
            _popularNovels.postValue(emptyList())
        }
    }

    /**
     * Memuat novel rekomendasi (recommended).
     */
    private suspend fun loadRecommendedNovels() {
        try {
            val novels = getRecommendedNovelsUseCase.execute(PREVIEW_ITEMS_COUNT)
            val mappedNovels = novelUiMapper.mapToUiModels(novels)
            _recommendedNovels.postValue(mappedNovels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load recommended novels")
            _recommendedNovels.postValue(emptyList())
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

                // Perbarui status novel di UI
                updateNovelInLists(novel.copy(isInLibrary = true))
            } catch (e: Exception) {
                Timber.e(e, "Failed to add novel to library")
                _errorEvent.postValue("Failed to add novel to library")
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

                // Perbarui status novel di UI
                updateNovelInLists(novel.copy(isInLibrary = false))
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove novel from library")
                _errorEvent.postValue("Failed to remove novel from library")
            }
        }
    }

    /**
     * Mendapatkan detail bab untuk novel dan bab tertentu.
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
                _errorEvent.postValue("Failed to get chapter detail")
                callback(null)
            }
        }
    }

    /**
     * Mendapatkan bab pertama untuk suatu novel.
     */
    fun getFirstChapter(novelId: String, callback: (ChapterDetail?) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val firstChapter = getFirstChapterUseCase.execute(novelId)
                callback(firstChapter)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get first chapter")
                _errorEvent.postValue("Failed to get first chapter")
                callback(null)
            }
        }
    }

    /**
     * Memperbarui novel pada semua list yang ditampilkan.
     */
    private fun updateNovelInLists(updatedNovel: NovelUiModel) {
        _continueReadingNovels.value?.let { novels ->
            val updatedList = novels.map {
                if (it.id == updatedNovel.id) updatedNovel else it
            }
            _continueReadingNovels.postValue(updatedList)
        }

        _recentNovels.value?.let { novels ->
            val updatedList = novels.map {
                if (it.id == updatedNovel.id) updatedNovel else it
            }
            _recentNovels.postValue(updatedList)
        }

        _popularNovels.value?.let { novels ->
            val updatedList = novels.map {
                if (it.id == updatedNovel.id) updatedNovel else it
            }
            _popularNovels.postValue(updatedList)
        }

        _recommendedNovels.value?.let { novels ->
            val updatedList = novels.map {
                if (it.id == updatedNovel.id) updatedNovel else it
            }
            _recommendedNovels.postValue(updatedList)
        }
    }

    /**
     * UI states untuk Home screen.
     */
    sealed class HomeUiState {
        data object Loading : HomeUiState()
        data object Success : HomeUiState()
        data class Error(val message: String) : HomeUiState()
    }
}
