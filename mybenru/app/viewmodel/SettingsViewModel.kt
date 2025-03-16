package com.mybenru.app.viewmodel

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.domain.usecase.BackupDataUseCase
import com.mybenru.domain.usecase.ClearCacheUseCase
import com.mybenru.domain.usecase.GetAppLanguageUseCase
import com.mybenru.domain.usecase.GetAppThemeUseCase
import com.mybenru.domain.usecase.GetAppVersionUseCase
import com.mybenru.domain.usecase.GetCacheSizeUseCase
import com.mybenru.domain.usecase.GetNotificationSettingsUseCase
import com.mybenru.domain.usecase.GetUpdateCheckSettingsUseCase
import com.mybenru.domain.usecase.RestoreDataUseCase
import com.mybenru.domain.usecase.SetAppLanguageUseCase
import com.mybenru.domain.usecase.SetAppThemeUseCase
import com.mybenru.domain.usecase.SetNotificationSettingsUseCase
import com.mybenru.domain.usecase.SetUpdateCheckSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppThemeUseCase: GetAppThemeUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase,
    private val setNotificationSettingsUseCase: SetNotificationSettingsUseCase,
    private val getUpdateCheckSettingsUseCase: GetUpdateCheckSettingsUseCase,
    private val setUpdateCheckSettingsUseCase: SetUpdateCheckSettingsUseCase,
    private val getCacheSizeUseCase: GetCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val getAppVersionUseCase: GetAppVersionUseCase,
    private val backupDataUseCase: BackupDataUseCase,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState

    // Theme setting
    private val _currentTheme = MutableLiveData<String>()
    val currentTheme: LiveData<String> = _currentTheme

    // Language setting
    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String> = _currentLanguage

    // Notifications setting
    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    // Automatic updates setting
    private val _automaticUpdatesEnabled = MutableLiveData<Boolean>()
    val automaticUpdatesEnabled: LiveData<Boolean> = _automaticUpdatesEnabled

    // Cache size
    private val _cacheSize = MutableLiveData<String>()
    val cacheSize: LiveData<String> = _cacheSize

    // App version
    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    // Backup/restore progress
    private val _backupRestoreProgress = MutableLiveData<Int>()
    val backupRestoreProgress: LiveData<Int> = _backupRestoreProgress

    // Backup/restore status
    private val _backupRestoreStatus = MutableLiveData<String>()
    val backupRestoreStatus: LiveData<String> = _backupRestoreStatus

    // Backup/restore in progress flag
    private val _isBackupRestoreInProgress = MutableLiveData<Boolean>()
    val isBackupRestoreInProgress: LiveData<Boolean> = _isBackupRestoreInProgress

    // Error event
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // Backup/restore cancel flag
    private var backupRestoreCancelled = false

    /**
     * Load all settings
     */
    fun loadSettings() {
        _uiState.value = SettingsUiState.Loading

        viewModelScope.launch(dispatchers.io) {
            try {
                // Load settings in parallel for better performance
                val themeDeferred = viewModelScope.launch { loadThemeSetting() }
                val languageDeferred = viewModelScope.launch { loadLanguageSetting() }
                val notificationsDeferred = viewModelScope.launch { loadNotificationsSetting() }
                val automaticUpdatesDeferred = viewModelScope.launch { loadAutomaticUpdatesSetting() }
                val cacheSizeDeferred = viewModelScope.launch { loadCacheSize() }
                val appVersionDeferred = viewModelScope.launch { loadAppVersion() }

                // Wait for all to complete
                themeDeferred.join()
                languageDeferred.join()
                notificationsDeferred.join()
                automaticUpdatesDeferred.join()
                cacheSizeDeferred.join()
                appVersionDeferred.join()

                // Update UI state to success
                _uiState.value = SettingsUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to load settings")
                _uiState.value = SettingsUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Load theme setting
     */
    private suspend fun loadThemeSetting() {
        try {
            val theme = getAppThemeUseCase.execute(Unit)
            _currentTheme.postValue(theme)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load theme setting")
            _currentTheme.postValue("System Default") // Default value
        }
    }

    /**
     * Load language setting
     */
    private suspend fun loadLanguageSetting() {
        try {
            val language = getAppLanguageUseCase.execute(Unit)
            _currentLanguage.postValue(language)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load language setting")
            _currentLanguage.postValue("English") // Default value
        }
    }

    /**
     * Load notifications setting
     */
    private suspend fun loadNotificationsSetting() {
        try {
            val enabled = getNotificationSettingsUseCase.execute(Unit)
            _notificationsEnabled.postValue(enabled)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load notifications setting")
            _notificationsEnabled.postValue(true) // Default value
        }
    }

    /**
     * Load automatic updates setting
     */
    private suspend fun loadAutomaticUpdatesSetting() {
        try {
            val enabled = getUpdateCheckSettingsUseCase.execute(Unit)
            _automaticUpdatesEnabled.postValue(enabled)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load automatic updates setting")
            _automaticUpdatesEnabled.postValue(true) // Default value
        }
    }

    /**
     * Load cache size
     */
    private suspend fun loadCacheSize() {
        try {
            val size = getCacheSizeUseCase.execute(Unit)
            _cacheSize.postValue(formatSize(size))
        } catch (e: Exception) {
            Timber.e(e, "Failed to load cache size")
            _cacheSize.postValue("Unknown")
        }
    }

    /**
     * Load app version
     */
    private suspend fun loadAppVersion() {
        try {
            val version = getAppVersionUseCase.execute(Unit)
            _appVersion.postValue("Version: ${version.versionName} (Build ${version.versionCode})")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load app version")
            _appVersion.postValue("Version: Unknown")
        }
    }

    /**
     * Set theme
     */
    fun setTheme(theme: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                setAppThemeUseCase.execute(theme)
                _currentTheme.postValue(theme)
            } catch (e: Exception) {
                Timber.e(e, "Failed to set theme")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to set theme")
            }
        }
    }

    /**
     * Set language
     */
    fun setLanguage(language: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                setAppLanguageUseCase.execute(language)
                _currentLanguage.postValue(language)
            } catch (e: Exception) {
                Timber.e(e, "Failed to set language")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to set language")
            }
        }
    }

    /**
     * Set notifications enabled
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            try {
                setNotificationSettingsUseCase.execute(enabled)
                _notificationsEnabled.postValue(enabled)
            } catch (e: Exception) {
                Timber.e(e, "Failed to set notifications enabled")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to set notifications")
            }
        }
    }

    /**
     * Set automatic updates enabled
     */
    fun setAutomaticUpdatesEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            try {
                setUpdateCheckSettingsUseCase.execute(enabled)
                _automaticUpdatesEnabled.postValue(enabled)
            } catch (e: Exception) {
                Timber.e(e, "Failed to set automatic updates enabled")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to set automatic updates")
            }
        }
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        viewModelScope.launch(dispatchers.io) {
            try {
                _uiState.value = SettingsUiState.Loading

                // Clear cache
                clearCacheUseCase.execute(Unit)

                // Reload cache size
                loadCacheSize()

                _uiState.value = SettingsUiState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear cache")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to clear cache")
                _uiState.value = SettingsUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    /**
     * Backup data
     */
    fun backupData() {
        viewModelScope.launch(dispatchers.io) {
            try {
                // Reset cancel flag
                backupRestoreCancelled = false

                // Show progress UI
                _isBackupRestoreInProgress.postValue(true)
                _backupRestoreStatus.postValue("Backing up data...")

                // Perform backup operation
                backupDataUseCase.execute { progress ->
                    // Check if operation was cancelled
                    if (backupRestoreCancelled) {
                        return@execute false
                    }

                    // Update progress
                    _backupRestoreProgress.postValue(progress)

                    // Return true to continue, false to cancel
                    true
                }

                // Update status based on completion or cancellation
                if (!backupRestoreCancelled) {
                    _backupRestoreStatus.postValue("Backup completed successfully")
                } else {
                    _backupRestoreStatus.postValue("Backup cancelled")
                }

                // Hide progress UI after a delay
                kotlinx.coroutines.delay(1000)
                _isBackupRestoreInProgress.postValue(false)

            } catch (e: Exception) {
                Timber.e(e, "Failed to backup data")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to backup data")
                _backupRestoreStatus.postValue("Backup failed")

                // Hide progress UI after a delay
                kotlinx.coroutines.delay(1000)
                _isBackupRestoreInProgress.postValue(false)
            }
        }
    }

    /**
     * Restore data
     */
    fun restoreData() {
        viewModelScope.launch(dispatchers.io) {
            try {
                // Reset cancel flag
                backupRestoreCancelled = false

                // Show progress UI
                _isBackupRestoreInProgress.postValue(true)
                _backupRestoreStatus.postValue("Restoring data...")

                // Perform restore operation
                restoreDataUseCase.execute { progress ->
                    // Check if operation was cancelled
                    if (backupRestoreCancelled) {
                        return@execute false
                    }

                    // Update progress
                    _backupRestoreProgress.postValue(progress)

                    // Return true to continue, false to cancel
                    true
                }

                // Update status based on completion or cancellation
                if (!backupRestoreCancelled) {
                    _backupRestoreStatus.postValue("Restore completed successfully")

                    // Reload settings after restore
                    loadSettings()
                } else {
                    _backupRestoreStatus.postValue("Restore cancelled")
                }

                // Hide progress UI after a delay
                kotlinx.coroutines.delay(1000)
                _isBackupRestoreInProgress.postValue(false)

            } catch (e: Exception) {
                Timber.e(e, "Failed to restore data")
                _errorEvent.postValue(e.localizedMessage ?: "Failed to restore data")
                _backupRestoreStatus.postValue("Restore failed")

                // Hide progress UI after a delay
                kotlinx.coroutines.delay(1000)
                _isBackupRestoreInProgress.postValue(false)
            }
        }
    }

    /**
     * Cancel backup/restore operation
     */
    fun cancelBackupRestore() {
        backupRestoreCancelled = true
        _backupRestoreStatus.postValue("Cancelling...")
    }

    /**
     * Format file size in bytes to human-readable format
     */
    private fun formatSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> String.format("%.2f KB", sizeInBytes / 1024f)
            sizeInBytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", sizeInBytes / (1024f * 1024f))
            else -> String.format("%.2f GB", sizeInBytes / (1024f * 1024f * 1024f))
        }
    }

    /**
     * UI states for the settings screen
     */
    sealed class SettingsUiState {
        data object Loading : SettingsUiState()
        data object Success : SettingsUiState()
        data class Error(val message: String) : SettingsUiState()
    }
}