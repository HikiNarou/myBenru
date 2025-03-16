package com.mybenru.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybenru.app.utils.AppCoroutineDispatchers
import com.mybenru.domain.usecase.InitializeAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the main activity
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    /**
     * Initialize the app
     */
    fun initializeApp() {
        viewModelScope.launch(dispatchers.io) {
            try {
                initializeAppUseCase.execute()
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize app")
            }
        }
    }

    /**
     * Handle deep link
     */
    fun handleDeepLink(uri: Uri) {
        Timber.d("Deep link: $uri")
        // Format: https://mybenru.com/novel/{novelId}

        val path = uri.path ?: return

        if (path.startsWith("/novel/")) {
            val novelId = path.substringAfter("/novel/")
            if (novelId.isNotEmpty()) {
                // TODO: Navigate to novel details
                Timber.d("Deep link to novel: $novelId")
            }
        }
    }
}