package com.mybenru.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.model.ChapterNavigationUiModel
import com.mybenru.app.model.ReaderSettingsUiModel
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.app.utils.Event
import com.mybenru.domain.model.ChapterNavigation
import com.mybenru.domain.usecase.BookmarkChapterUseCase
import com.mybenru.domain.usecase.GetChapterContentUseCase
import com.mybenru.domain.usecase.GetChapterNavigationUseCase
import com.mybenru.domain.usecase.GetReaderSettingsUseCase
import com.mybenru.domain.usecase.IsChapterBookmarkedUseCase
import com.mybenru.domain.usecase.MarkChapterReadUseCase
import com.mybenru.domain.usecase.SaveReaderSettingsUseCase
import com.mybenru.domain.usecase.SaveReadingProgressUseCase
import com.mybenru.domain.usecase.UnbookmarkChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel for the reader screen
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getChapterContentUseCase: GetChapterContentUseCase,
    private val getChapterNavigationUseCase: GetChapterNavigationUseCase,
    private val markChapterReadUseCase: MarkChapterReadUseCase,
    private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    private val bookmarkChapterUseCase: BookmarkChapterUseCase,
    private val unbookmarkChapterUseCase: UnbookmarkChapterUseCase,
    private val isChapterBookmarkedUseCase: IsChapterBookmarkedUseCase,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState

    // Chapter content (paragraphs)
    private val _chapterContent = MutableLiveData<List<String>>()
    val chapterContent: LiveData<List<String>> = _chapterContent

    // Raw chapter content (for TTS)
    private var rawChapterContent = ""

    // Chapter navigation
    private val _chapterNavigation = MutableLiveData<ChapterNavigationUiModel>()
    val chapterNavigation: LiveData<ChapterNavigationUiModel> = _chapterNavigation

    // Reader settings
    private val _readerSettings = MutableLiveData<ReaderSettingsUiModel>()
    val readerSettings: LiveData<ReaderSettingsUiModel> = _readerSettings

    // Bookmark status
    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // Navigation event
    private val _navigationEvent = MutableLiveData<Event<ChapterNavigationUiModel>>()
    val navigationEvent: LiveData<Event<ChapterNavigationUiModel>> = _navigationEvent

    // Current novel and chapter info
    private var currentNovelId = ""
    private var currentChapterId = ""

    // Reading progress tracking
    private var lastSavedPosition = 0
    private var lastSaveTime = 0L
    private val saveDebounceMs = 1000L // Save at most once per second

    // Chapter navigation
    private var previousChapterId: String? = null
    private var nextChapterId: String? = null

    /**
     * Load chapter content
     */
    fun loadChapterContent(novelId: String, chapterId: String) {
        _uiState.value = ReaderUiState.Loading
        currentNovelId = novelId
        currentChapterId = chapterId

        viewModelScope.launch(dispatchers.io) {
            try {
                // Load reader settings
                loadReaderSettings()

                // Check if chapter is bookmarked
                checkBookmarkStatus()

                // Load chapter content
                val params = GetChapterContentUseCase.Params(novelId, chapterId)
                val result = getChapterContentUseCase.execute(params)

                // Save raw content for TTS
                rawChapterContent = result.content

                // Process content into paragraphs
                val paragraphs = result.content
                    .split("\n")
                    .filter { it.isNotBlank() }

                // Update UI state and content
                _chapterContent.postValue(paragraphs)
                _uiState.value = ReaderUiState.Success(result.readingPosition ?: 0)

                // Mark chapter as read
                markChapterRead()

                // Load chapter navigation
                loadChapterNavigation()

            } catch (e: Exception) {
                Timber.e(e, "Failed to load chapter content")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load chapter")
                _uiState.value = ReaderUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Load chapter navigation (previous and next chapters)
     */
    private fun loadChapterNavigation() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val params = GetChapterNavigationUseCase.Params(currentNovelId, currentChapterId)
                val navigation = getChapterNavigationUseCase.execute(params)

                // Store chapter IDs for navigation
                previousChapterId = navigation.previousChapterId
                nextChapterId = navigation.nextChapterId

                // Map to UI model
                val navigationUiModel = ChapterNavigationUiModel(
                    hasPreviousChapter = navigation.previousChapterId != null,
                    hasNextChapter = navigation.nextChapterId != null,
                    previousChapterNumber = navigation.previousChapterNumber,
                    nextChapterNumber = navigation.nextChapterNumber
                )

                _chapterNavigation.postValue(navigationUiModel)

            } catch (e: Exception) {
                Timber.e(e, "Failed to load chapter navigation")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load navigation")
            }
        }
    }

    /**
     * Mark the current chapter as read
     */
    private fun markChapterRead() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val params = MarkChapterReadUseCase.Params(
                    novelId = currentNovelId,
                    chapterId = currentChapterId
                )
                markChapterReadUseCase.execute(params)
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark chapter as read")
            }
        }
    }

    /**
     * Update reading position
     */
    fun updateReadingPosition(position: Int) {
        // Debounce saving to avoid too many database operations
        val currentTime = System.currentTimeMillis()
        if (position != lastSavedPosition && currentTime - lastSaveTime > saveDebounceMs) {
            lastSavedPosition = position
            lastSaveTime = currentTime

            viewModelScope.launch(dispatchers.io) {
                try {
                    val params = SaveReadingProgressUseCase.Params(
                        novelId = currentNovelId,
                        chapterId = currentChapterId,
                        position = position
                    )
                    saveReadingProgressUseCase.execute(params)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to save reading position")
                }
            }
        }
    }

    /**
     * Load reader settings
     */
    private fun loadReaderSettings() {
        try {
            val domainSettings = getReaderSettingsUseCase.execute(Unit)

            // Map domain settings to UI model
            val settingsUiModel = ReaderSettingsUiModel(
                textSize = domainSettings.textSize,
                lineSpacing = domainSettings.lineSpacing,
                theme = ReaderSettingsUiModel.ReaderTheme.valueOf(domainSettings.theme),
                fontFamily = domainSettings.fontFamily,
                textAlignment = ReaderSettingsUiModel.TextAlignment.valueOf(domainSettings.textAlignment),
                isLandscapeMode = domainSettings.isLandscapeMode,
                isKeepScreenOn = domainSettings.isKeepScreenOn,
                isSystemBrightness = domainSettings.isSystemBrightness,
                brightness = domainSettings.brightness,
                marginHorizontal = domainSettings.marginHorizontal,
                marginVertical = domainSettings.marginVertical,
                paragraphSpacing = domainSettings.paragraphSpacing
            )

            _readerSettings.postValue(settingsUiModel)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load reader settings")
            // Load default settings
            _readerSettings.postValue(ReaderSettingsUiModel())
        }
    }

    /**
     * Save reader settings
     */
    private fun saveReaderSettings(settings: ReaderSettingsUiModel) {
        viewModelScope.launch(dispatchers.io) {
            try {
                // Map UI model to domain settings
                val domainSettings = com.mybenru.domain.model.ReaderSettings(
                    textSize = settings.textSize,
                    lineSpacing = settings.lineSpacing,
                    theme = settings.theme.name,
                    fontFamily = settings.fontFamily,
                    textAlignment = settings.textAlignment.name,
                    isLandscapeMode = settings.isLandscapeMode,
                    isKeepScreenOn = settings.isKeepScreenOn,
                    isSystemBrightness = settings.isSystemBrightness,
                    brightness = settings.brightness,
                    marginHorizontal = settings.marginHorizontal,
                    marginVertical = settings.marginVertical,
                    paragraphSpacing = settings.paragraphSpacing
                )

                saveReaderSettingsUseCase.execute(domainSettings)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save reader settings")
            }
        }
    }

    /**
     * Get current settings and update them
     */
    private fun updateSettings(updater: (ReaderSettingsUiModel) -> ReaderSettingsUiModel) {
        val currentSettings = _readerSettings.value ?: ReaderSettingsUiModel()
        val newSettings = updater(currentSettings)

        _readerSettings.value = newSettings
        saveReaderSettings(newSettings)
    }

    /**
     * Increase text size
     */
    fun increaseTextSize() {
        updateSettings { it.copy(textSize = (it.textSize + 1).coerceAtMost(32)) }
    }

    /**
     * Decrease text size
     */
    fun decreaseTextSize() {
        updateSettings { it.copy(textSize = (it.textSize - 1).coerceAtLeast(12)) }
    }

    /**
     * Increase line spacing
     */
    fun increaseLineSpacing() {
        updateSettings { it.copy(lineSpacing = (it.lineSpacing + 0.1f).coerceAtMost(2.5f)) }
    }

    /**
     * Decrease line spacing
     */
    fun decreaseLineSpacing() {
        updateSettings { it.copy(lineSpacing = (it.lineSpacing - 0.1f).coerceAtLeast(0.8f)) }
    }

    /**
     * Set theme
     */
    fun setTheme(theme: ReaderSettingsUiModel.ReaderTheme) {
        updateSettings { it.copy(theme = theme) }
    }

    /**
     * Set font family
     */
    fun setFontFamily(fontFamily: String) {
        updateSettings { it.copy(fontFamily = fontFamily) }
    }

    /**
     * Set text alignment
     */
    fun setTextAlignment(alignment: ReaderSettingsUiModel.TextAlignment) {
        updateSettings { it.copy(textAlignment = alignment) }
    }

    /**
     * Set keep screen on
     */
    fun setKeepScreenOn(keepScreenOn: Boolean) {
        updateSettings { it.copy(isKeepScreenOn = keepScreenOn) }
    }

    /**
     * Set use system brightness
     */
    fun setUseSystemBrightness(useSystemBrightness: Boolean) {
        updateSettings { it.copy(isSystemBrightness = useSystemBrightness) }
    }

    /**
     * Set brightness level
     */
    fun setBrightness(brightness: Int) {
        updateSettings { it.copy(brightness = brightness) }
    }

    /**
     * Navigate to previous chapter
     */
    fun navigateToPreviousChapter() {
        previousChapterId?.let { chapterId ->
            viewModelScope.launch(dispatchers.io) {
                try {
                    // Get chapter details
                    val params = GetChapterNavigationUseCase.Params(currentNovelId, chapterId)
                    val navigation = getChapterNavigationUseCase.execute(params)

                    val navigationUiModel = ChapterNavigationUiModel(
                        novelId = currentNovelId,
                        chapterId = chapterId,
                        chapterNumber = navigation.chapterNumber ?: 0,
                        chapterTitle = navigation.chapterTitle,
                        novelTitle = navigation.novelTitle ?: "",
                        hasPreviousChapter = navigation.previousChapterId != null,
                        hasNextChapter = true // Current chapter becomes next
                    )

                    // Trigger navigation event
                    _navigationEvent.postValue(Event(navigationUiModel))

                } catch (e: Exception) {
                    Timber.e(e, "Failed to navigate to previous chapter")
                    _errorEvent.postValue(e.localizedMessage ?: "Failed to navigate")
                }
            }
        } ?: run {
            _errorEvent.postValue("No previous chapter available")
        }
    }

    /**
     * Navigate to next chapter
     */
    fun navigateToNextChapter() {
        nextChapterId?.let { chapterId ->
            viewModelScope.launch(dispatchers.io) {
                try {
                    // Get chapter details
                    val params = GetChapterNavigationUseCase.Params(currentNovelId, chapterId)
                    val navigation = getChapterNavigationUseCase.execute(params)

                    val navigationUiModel = ChapterNavigationUiModel(
                        novelId = currentNovelId,
                        chapterId = chapterId,
                        chapterNumber = navigation.chapterNumber ?: 0,
                        chapterTitle = navigation.chapterTitle,
                        novelTitle = navigation.novelTitle ?: "",
                        hasPreviousChapter = true, // Current chapter becomes previous
                        hasNextChapter = navigation.nextChapterId != null
                    )

                    // Trigger navigation event
                    _navigationEvent.postValue(Event(navigationUiModel))

                } catch (e: Exception) {
                    Timber.e(e, "Failed to navigate to next chapter")
                    _errorEvent.postValue(e.localizedMessage ?: "Failed to navigate")
                }
            }
        } ?: run {
            _errorEvent.postValue("No next chapter available")
        }
    }

    /**
     * Check if current chapter is bookmarked
     */
    private fun checkBookmarkStatus() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val params = IsChapterBookmarkedUseCase.Params(currentNovelId, currentChapterId)
                val isBookmarked = isChapterBookmarkedUseCase.execute(params)
                _isBookmarked.postValue(isBookmarked)
            } catch (e: Exception) {
                Timber.e(e, "Failed to check bookmark status")
                _isBookmarked.postValue(false)
            }
        }
    }

    /**
     * Toggle bookmark status for the current chapter
     */
    fun toggleBookmark() {
        val currentStatus = _isBookmarked.value ?: false

        viewModelScope.launch(dispatchers.io) {
            try {
                if (currentStatus) {
                    unbookmarkChapterUseCase.execute(
                        UnbookmarkChapterUseCase.Params(currentNovelId, currentChapterId)
                    )
                } else {
                    bookmarkChapterUseCase.execute(
                        BookmarkChapterUseCase.Params(currentNovelId, currentChapterId)
                    )
                }

                // Update status
                _isBookmarked.postValue(!currentStatus)

            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle bookmark")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to toggle bookmark")
            }
        }
    }

    /**
     * Get chapter content for text-to-speech
     */
    fun getChapterContentForTts(): String {
        return rawChapterContent
    }

    /**
     * UI states for the reader screen
     */
    sealed class ReaderUiState {
        data object Loading : ReaderUiState()
        data class Success(val position: Int = 0) : ReaderUiState()
        data class Error(val message: String) : ReaderUiState()
    }
}