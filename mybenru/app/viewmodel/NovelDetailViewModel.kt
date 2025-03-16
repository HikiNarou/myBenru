package com.mybenru.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.mapper.NovelUiMapper
import com.mybenru.app.model.ChapterUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.app.utils.Event
import com.mybenru.domain.model.ChapterDetail
import com.mybenru.domain.usecase.AddNovelToLibraryUseCase
import com.mybenru.domain.usecase.BookmarkChapterUseCase
import com.mybenru.domain.usecase.DeleteChapterUseCase
import com.mybenru.domain.usecase.DownloadChapterUseCase
import com.mybenru.domain.usecase.GetChaptersUseCase
import com.mybenru.domain.usecase.GetFirstChapterUseCase
import com.mybenru.domain.usecase.GetLastReadChapterUseCase
import com.mybenru.domain.usecase.GetNovelDetailUseCase
import com.mybenru.domain.usecase.GetRelatedNovelsUseCase
import com.mybenru.domain.usecase.RemoveNovelFromLibraryUseCase
import com.mybenru.domain.usecase.UnbookmarkChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the novel detail screen
 */
@HiltViewModel
class NovelDetailViewModel @Inject constructor(
    private val getNovelDetailUseCase: GetNovelDetailUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val getRelatedNovelsUseCase: GetRelatedNovelsUseCase,
    private val getLastReadChapterUseCase: GetLastReadChapterUseCase,
    private val getFirstChapterUseCase: GetFirstChapterUseCase,
    private val addNovelToLibraryUseCase: AddNovelToLibraryUseCase,
    private val removeNovelFromLibraryUseCase: RemoveNovelFromLibraryUseCase,
    private val bookmarkChapterUseCase: BookmarkChapterUseCase,
    private val unbookmarkChapterUseCase: UnbookmarkChapterUseCase,
    private val downloadChapterUseCase: DownloadChapterUseCase,
    private val deleteChapterUseCase: DeleteChapterUseCase,
    private val novelUiMapper: NovelUiMapper,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<NovelDetailUiState>(NovelDetailUiState.Loading)
    val uiState: StateFlow<NovelDetailUiState> = _uiState

    // Novel detail
    private val _novelDetail = MutableLiveData<NovelUiModel>()
    val novelDetail: LiveData<NovelUiModel> = _novelDetail

    // Chapters
    private val _chapters = MutableLiveData<List<ChapterUiModel>>()
    val chapters: LiveData<List<ChapterUiModel>> = _chapters

    // Related novels
    private val _relatedNovels = MutableLiveData<List<NovelUiModel>>()
    val relatedNovels: LiveData<List<NovelUiModel>> = _relatedNovels

    // Last read chapter
    private val _lastReadChapter = MutableLiveData<ChapterUiModel?>()
    val lastReadChapter: LiveData<ChapterUiModel?> = _lastReadChapter

    // Download progress
    private val _downloadProgress = MutableLiveData<DownloadProgress>()
    val downloadProgress: LiveData<DownloadProgress> = _downloadProgress

    // Download complete event
    private val _downloadCompleteEvent = MutableLiveData<Event<Int>>()
    val downloadCompleteEvent: LiveData<Event<Int>> = _downloadCompleteEvent

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // Current novel ID and source ID
    private var currentNovelId = ""
    private var currentSourceId = ""

    /**
     * Load novel detail, chapters, related novels, and last read chapter
     */
    fun loadNovelDetail(novelId: String, sourceId: String, forceRefresh: Boolean = false) {
        _uiState.value = NovelDetailUiState.Loading

        currentNovelId = novelId
        currentSourceId = sourceId

        viewModelScope.launch(dispatchers.io) {
            try {
                // Load data in parallel for better performance
                val novelDetailDeferred = viewModelScope.launch { loadNovelDetail(forceRefresh) }
                val chaptersDeferred = viewModelScope.launch { loadChapters(forceRefresh) }
                val relatedNovelsDeferred = viewModelScope.launch { loadRelatedNovels() }
                val lastReadChapterDeferred = viewModelScope.launch { loadLastReadChapter() }

                // Wait for all to complete
                novelDetailDeferred.join()
                chaptersDeferred.join()
                relatedNovelsDeferred.join()
                lastReadChapterDeferred.join()

                // Update UI state to success
                _uiState.value = NovelDetailUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to load novel detail")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load novel detail")
                _uiState.value = NovelDetailUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Load novel detail
     */
    private suspend fun loadNovelDetail(forceRefresh: Boolean) {
        try {
            val params = GetNovelDetailUseCase.Params(currentNovelId, currentSourceId, forceRefresh)
            val novel = getNovelDetailUseCase.execute(params)
            val novelUiModel = novelUiMapper.mapToUiModel(novel)
            _novelDetail.postValue(novelUiModel)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load novel detail")
            throw e
        }
    }

    /**
     * Load chapters
     */
    private suspend fun loadChapters(forceRefresh: Boolean) {
        try {
            val params = GetChaptersUseCase.Params(currentNovelId, forceRefresh)
            val domainChapters = getChaptersUseCase.execute(params)

            // Map domain chapters to UI chapters
            val chapterUiModels = domainChapters.map { chapter ->
                ChapterUiModel(
                    id = chapter.id,
                    url = chapter.url,
                    title = chapter.title,
                    number = chapter.number,
                    novelId = chapter.novelId,
                    uploadDate = chapter.uploadDate,
                    wordCount = chapter.wordCount,
                    isRead = chapter.isRead,
                    isBookmarked = chapter.isBookmarked,
                    isDownloaded = chapter.isDownloaded,
                    readingProgress = chapter.readingProgress,
                    hasContent = chapter.hasContent
                )
            }

            _chapters.postValue(chapterUiModels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load chapters")
            throw e
        }
    }

    /**
     * Load related novels
     */
    private suspend fun loadRelatedNovels() {
        try {
            val novels = getRelatedNovelsUseCase.execute(currentNovelId)
            val novelUiModels = novelUiMapper.mapToUiModels(novels)
            _relatedNovels.postValue(novelUiModels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load related novels")
            // Don't throw, related novels are not critical
            _relatedNovels.postValue(emptyList())
        }
    }

    /**
     * Load last read chapter
     */
    private suspend fun loadLastReadChapter() {
        try {
            val lastReadChapter = getLastReadChapterUseCase.execute(currentNovelId)

            if (lastReadChapter != null) {
                // Map to UI model
                val chapterUiModel = ChapterUiModel(
                    id = lastReadChapter.id,
                    url = lastReadChapter.url,
                    title = lastReadChapter.title,
                    number = lastReadChapter.number,
                    novelId = lastReadChapter.novelId,
                    uploadDate = lastReadChapter.uploadDate,
                    wordCount = lastReadChapter.wordCount,
                    isRead = lastReadChapter.isRead,
                    isBookmarked = lastReadChapter.isBookmarked,
                    isDownloaded = lastReadChapter.isDownloaded,
                    readingProgress = lastReadChapter.readingProgress,
                    hasContent = lastReadChapter.hasContent
                )

                _lastReadChapter.postValue(chapterUiModel)
            } else {
                _lastReadChapter.postValue(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load last read chapter")
            // Don't throw, last read chapter is not critical
            _lastReadChapter.postValue(null)
        }
    }

    /**
     * Add the current novel to the library
     */
    fun addToLibrary() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val novel = _novelDetail.value
                if (novel != null) {
                    val domainNovel = novelUiMapper.mapToDomainModel(novel)
                    addNovelToLibraryUseCase.execute(domainNovel)

                    // Update the novel in the UI
                    _novelDetail.postValue(novel.copy(isInLibrary = true))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to add novel to library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to add novel to library")
            }
        }
    }

    /**
     * Remove the current novel from the library
     */
    fun removeFromLibrary() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val novel = _novelDetail.value
                if (novel != null) {
                    val domainNovel = novelUiMapper.mapToDomainModel(novel)
                    removeNovelFromLibraryUseCase.execute(domainNovel)

                    // Update the novel in the UI
                    _novelDetail.postValue(novel.copy(isInLibrary = false))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove novel from library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to remove novel from library")
            }
        }
    }

    /**
     * Add a related novel to the library
     */
    fun addRelatedNovelToLibrary(novel: NovelUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val domainNovel = novelUiMapper.mapToDomainModel(novel)
                addNovelToLibraryUseCase.execute(domainNovel)

                // Update the novel in the UI
                updateRelatedNovelInList(novel.copy(isInLibrary = true))

            } catch (e: Exception) {
                Timber.e(e, "Failed to add related novel to library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to add novel to library")
            }
        }
    }

    /**
     * Remove a related novel from the library
     */
    fun removeRelatedNovelFromLibrary(novel: NovelUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val domainNovel = novelUiMapper.mapToDomainModel(novel)
                removeNovelFromLibraryUseCase.execute(domainNovel)

                // Update the novel in the UI
                updateRelatedNovelInList(novel.copy(isInLibrary = false))

            } catch (e: Exception) {
                Timber.e(e, "Failed to remove related novel from library")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to remove novel from library")
            }
        }
    }

    /**
     * Update a related novel in the list
     */
    private fun updateRelatedNovelInList(updatedNovel: NovelUiModel) {
        _relatedNovels.value?.let { novels ->
            val updatedList = novels.map {
                if (it.id == updatedNovel.id) updatedNovel else it
            }
            _relatedNovels.postValue(updatedList)
        }
    }

    /**
     * Toggle bookmark status for a chapter
     */
    fun toggleChapterBookmark(chapter: ChapterUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                if (chapter.isBookmarked) {
                    // Unbookmark
                    val params = UnbookmarkChapterUseCase.Params(
                        novelId = currentNovelId,
                        chapterId = chapter.id
                    )
                    unbookmarkChapterUseCase.execute(params)
                } else {
                    // Bookmark
                    val params = BookmarkChapterUseCase.Params(
                        novelId = currentNovelId,
                        chapterId = chapter.id
                    )
                    bookmarkChapterUseCase.execute(params)
                }

                // Update the chapter in the UI
                updateChapterInList(chapter.copy(isBookmarked = !chapter.isBookmarked))

            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle bookmark")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to toggle bookmark")
            }
        }
    }

    /**
     * Update a chapter in the list
     */
    private fun updateChapterInList(updatedChapter: ChapterUiModel) {
        _chapters.value?.let { chapters ->
            val updatedList = chapters.map {
                if (it.id == updatedChapter.id) updatedChapter else it
            }
            _chapters.postValue(updatedList)
        }
    }

    /**
     * Download a chapter
     */
    fun downloadChapter(chapterId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                // Show download progress
                _downloadProgress.postValue(DownloadProgress(true, 0, 1))

                val params = DownloadChapterUseCase.Params(
                    novelId = currentNovelId,
                    chapterId = chapterId
                )

                downloadChapterUseCase.execute(params)

                // Update the chapter in the UI
                _chapters.value?.find { it.id == chapterId }?.let { chapter ->
                    updateChapterInList(chapter.copy(isDownloaded = true))
                }

                // Hide download progress
                _downloadProgress.postValue(DownloadProgress(false, 0, 0))

                // Show download complete message
                _downloadCompleteEvent.postValue(Event(1))

            } catch (e: Exception) {
                Timber.e(e, "Failed to download chapter")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to download chapter")
                // Hide download progress
                _downloadProgress.postValue(DownloadProgress(false, 0, 0))
            }
        }
    }

    /**
     * Download multiple chapters
     */
    fun downloadChapters(chapterIds: List<String>) {
        if (chapterIds.isEmpty()) return

        viewModelScope.launch(dispatchers.io) {
            try {
                // Show download progress
                val total = chapterIds.size
                _downloadProgress.postValue(DownloadProgress(true, 0, total))

                // Download chapters sequentially to avoid overloading
                for ((index, chapterId) in chapterIds.withIndex()) {
                    try {
                        val params = DownloadChapterUseCase.Params(
                            novelId = currentNovelId,
                            chapterId = chapterId
                        )

                        downloadChapterUseCase.execute(params)

                        // Update progress
                        _downloadProgress.postValue(
                            DownloadProgress(true, index + 1, total)
                        )

                        // Update chapter in the UI
                        _chapters.value?.find { it.id == chapterId }?.let { chapter ->
                            updateChapterInList(chapter.copy(isDownloaded = true))
                        }

                    } catch (e: Exception) {
                        Timber.e(e, "Failed to download chapter $chapterId")
                        // Continue with next chapter
                    }
                }

                // Hide download progress
                _downloadProgress.postValue(DownloadProgress(false, 0, 0))

                // Show download complete message
                _downloadCompleteEvent.postValue(Event(chapterIds.size))

            } catch (e: Exception) {
                Timber.e(e, "Failed to download chapters")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to download chapters")
                // Hide download progress
                _downloadProgress.postValue(DownloadProgress(false, 0, 0))
            }
        }
    }

    /**
     * Delete a downloaded chapter
     */
    fun deleteChapter(chapterId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val params = DeleteChapterUseCase.Params(
                    novelId = currentNovelId,
                    chapterId = chapterId
                )

                deleteChapterUseCase.execute(params)

                // Update the chapter in the UI
                _chapters.value?.find { it.id == chapterId }?.let { chapter ->
                    updateChapterInList(chapter.copy(isDownloaded = false))
                }

            } catch (e: Exception) {
                Timber.e(e, "Failed to delete chapter")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to delete chapter")
            }
        }
    }

    /**
     * Start reading from the last read chapter or the first chapter
     */
    fun startReading(callback: (ChapterDetail?) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                // First try to get the last read chapter
                var chapter = getLastReadChapterUseCase.execute(currentNovelId)

                // If no last read chapter, get the first chapter
                if (chapter == null) {
                    chapter = getFirstChapterUseCase.execute(currentNovelId)
                }

                callback(chapter)

            } catch (e: Exception) {
                Timber.e(e, "Failed to start reading")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to start reading")
                callback(null)
            }
        }
    }

    /**
     * Get the first chapter for a novel
     */
    fun getFirstChapterForNovel(novelId: String, callback: (ChapterDetail?) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val chapter = getFirstChapterUseCase.execute(novelId)
                callback(chapter)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get first chapter")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to get first chapter")
                callback(null)
            }
        }
    }

    /**
     * Class representing download progress
     */
    data class DownloadProgress(
        val isActive: Boolean,
        val current: Int,
        val total: Int
    )

    /**
     * UI states for the novel detail screen
     */
    sealed class NovelDetailUiState {
        data object Loading : NovelDetailUiState()
        data object Success : NovelDetailUiState()
        data class Error(val message: String) : NovelDetailUiState()
    }
}