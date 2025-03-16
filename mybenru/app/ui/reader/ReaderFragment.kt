package com.mybenru.app.ui.reader

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybenru.app.R
import com.mybenru.app.databinding.FragmentReaderBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.model.ReaderSettingsUiModel
import com.mybenru.app.utils.hide
import com.mybenru.app.utils.show
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.ReaderViewModel
import com.mybenru.app.widget.ReaderView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * Fragment for reading novel chapters
 */
@AndroidEntryPoint
class ReaderFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReaderViewModel by viewModels()
    private val args: ReaderFragmentArgs by navArgs()

    // UI State
    private var isSystemUiVisible = true
    private var isSettingsSheetVisible = false
    private var isSearchSheetVisible = false
    private var isTtsActive = false

    // Bottom sheets behaviors
    private lateinit var settingsSheetBehavior: BottomSheetBehavior<View>
    private lateinit var searchSheetBehavior: BottomSheetBehavior<View>

    // Text-to-speech
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false

    // Auto-scroll
    private var autoScrollJob: Job? = null
    private var autoScrollSpeed = 50 // Pixels per second
    private var isAutoScrolling = false

    // UI update job
    private var uiUpdateJob: Job? = null

    // Progress saving
    private var lastSavedPosition = 0
    private val saveProgressInterval = 5000L // Save every 5 seconds

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBottomSheets()
        setupReaderView()
        setupSettingsControls()
        setupSearchControls()
        setupNavigationControls()
        setupMenuProvider()
        observeViewModel()

        // Initialize text-to-speech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Load chapter content
        viewModel.loadChapter(args.chapterId, args.novelId, args.sourceId)

        // Start periodic progress saving
        startProgressSavingJob()
    }

    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        // Set novel and chapter titles
        binding.txtNovelTitle.text = args.novelTitle
        binding.txtChapterTitle.text = args.chapterTitle

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheets() {
        // Settings sheet
        settingsSheetBehavior = BottomSheetBehavior.from(binding.layoutSettingsSheet)
        settingsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        settingsSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                isSettingsSheetVisible = newState != BottomSheetBehavior.STATE_HIDDEN

                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Save settings when bottom sheet is hidden
                    saveReaderSettings()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Not needed
            }
        })

        // Search sheet
        searchSheetBehavior = BottomSheetBehavior.from(binding.layoutSearchSheet)
        searchSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        searchSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                isSearchSheetVisible = newState != BottomSheetBehavior.STATE_HIDDEN

                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Clear search highlights when sheet is hidden
                    binding.readerView.clearHighlights()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Not needed
            }
        })
    }

    private fun setupReaderView() {
        binding.readerView.setOnTapListener { region ->
            when (region) {
                ReaderView.TapRegion.LEFT -> {
                    // Left tap - previous page or show UI
                    if (isSystemUiVisible) {
                        viewModel.navigateToPreviousChapter()
                    } else {
                        toggleSystemUi()
                    }
                }
                ReaderView.TapRegion.CENTER -> {
                    // Center tap - toggle UI
                    toggleSystemUi()
                }
                ReaderView.TapRegion.RIGHT -> {
                    // Right tap - next page or show UI
                    if (isSystemUiVisible) {
                        viewModel.navigateToNextChapter()
                    } else {
                        toggleSystemUi()
                    }
                }
            }
        }

        binding.readerView.setOnScrollListener { scrollY, oldScrollY ->
            // Save reading position when user scrolls
            val currentPosition = scrollY
            if (currentPosition != lastSavedPosition) {
                viewModel.saveReadingProgress(currentPosition)
                lastSavedPosition = currentPosition
            }

            // Hide UI when scrolling down
            if (isSystemUiVisible && scrollY > oldScrollY && scrollY > 100) {
                hideSystemUi()
            }
        }
    }

    private fun setupSettingsControls() {
        // Text size controls
        binding.btnDecreaseTextSize.setOnClickListener {
            val currentSize = binding.txtTextSizeValue.text.toString().toInt()
            if (currentSize > 10) {
                updateTextSize(currentSize - 1)
            }
        }

        binding.btnIncreaseTextSize.setOnClickListener {
            val currentSize = binding.txtTextSizeValue.text.toString().toInt()
            if (currentSize < 30) {
                updateTextSize(currentSize + 1)
            }
        }

        // Line spacing controls
        binding.btnDecreaseLineSpacing.setOnClickListener {
            val currentSpacing = viewModel.readerSettings.value?.lineSpacing ?: 1.5f
            if (currentSpacing > 1.0f) {
                updateLineSpacing(currentSpacing - 0.1f)
            }
        }

        binding.btnIncreaseLineSpacing.setOnClickListener {
            val currentSpacing = viewModel.readerSettings.value?.lineSpacing ?: 1.5f
            if (currentSpacing < 3.0f) {
                updateLineSpacing(currentSpacing + 0.1f)
            }
        }

        // Theme radio buttons
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radioLight -> ReaderSettingsUiModel.ReaderTheme.LIGHT
                R.id.radioDark -> ReaderSettingsUiModel.ReaderTheme.DARK
                R.id.radioSepia -> ReaderSettingsUiModel.ReaderTheme.SEPIA
                R.id.radioBlack -> ReaderSettingsUiModel.ReaderTheme.BLACK
                else -> ReaderSettingsUiModel.ReaderTheme.LIGHT
            }

            updateReaderTheme(theme)
        }

        // Font family spinner
        binding.spinnerFontFamily.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val fontFamily = parent.getItemAtPosition(position).toString()
                updateFontFamily(fontFamily)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // Not needed
            }
        })

        // Text alignment radio buttons
        binding.radioGroupAlignment.setOnCheckedChangeListener { _, checkedId ->
            val alignment = when (checkedId) {
                R.id.radioLeft -> ReaderSettingsUiModel.TextAlignment.LEFT
                R.id.radioCenter -> ReaderSettingsUiModel.TextAlignment.CENTER
                R.id.radioRight -> ReaderSettingsUiModel.TextAlignment.RIGHT
                R.id.radioJustified -> ReaderSettingsUiModel.TextAlignment.JUSTIFIED
                else -> ReaderSettingsUiModel.TextAlignment.LEFT
            }

            updateTextAlignment(alignment)
        }

        // Screen orientation controls
        binding.switchOrientation.setOnCheckedChangeListener { _, isChecked ->
            setScreenOrientation(isChecked)
        }

        // Keep screen on switch
        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            updateKeepScreenOn(isChecked)
        }

        // Brightness controls
        binding.switchSystemBrightness.setOnCheckedChangeListener { _, isChecked ->
            binding.seekBarBrightness.isEnabled = !isChecked
            updateSystemBrightness(isChecked)
        }

        binding.seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateBrightness(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })

        // Auto-scroll controls
        binding.switchAutoScroll.setOnCheckedChangeListener { _, isChecked ->
            binding.seekBarScrollSpeed.isEnabled = isChecked
            toggleAutoScroll(isChecked)
        }

        binding.seekBarScrollSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateAutoScrollSpeed(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })

        // Settings button
        binding.btnSettings.setOnClickListener {
            toggleSettingsSheet()
        }
    }

    private fun setupSearchControls() {
        // Search button
        binding.btnSearch.setOnClickListener {
            toggleSearchSheet()
        }

        // Search input
        binding.btnSearchGo.setOnClickListener {
            performSearch(binding.editSearch.text.toString())
        }

        // Next result button
        binding.btnNextResult.setOnClickListener {
            navigateToNextSearchResult()
        }

        // Previous result button
        binding.btnPreviousResult.setOnClickListener {
            navigateToPreviousSearchResult()
        }

        // Close search button
        binding.btnCloseSearch.setOnClickListener {
            closeSearch()
        }
    }

    private fun setupNavigationControls() {
        // Previous chapter button
        binding.btnPreviousChapter.setOnClickListener {
            viewModel.navigateToPreviousChapter()
        }

        // Next chapter button
        binding.btnNextChapter.setOnClickListener {
            viewModel.navigateToNextChapter()
        }

        // Text-to-speech button
        binding.btnTts.setOnClickListener {
            toggleTextToSpeech()
        }

        // Bookmark button
        binding.btnBookmark.setOnClickListener {
            toggleBookmark()
        }
    }

    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_reader, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_toc -> {
                        navigateToTableOfContents()
                        true
                    }
                    R.id.action_details -> {
                        navigateToNovelDetails()
                        true
                    }
                    R.id.action_text_to_speech -> {
                        toggleTextToSpeech()
                        true
                    }
                    R.id.action_share -> {
                        shareChapter()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ReaderViewModel.ReaderUiState.Loading -> {
                        showLoading(true)
                    }
                    is ReaderViewModel.ReaderUiState.Success -> {
                        showLoading(false)
                        updateChapterNavigation()
                    }
                    is ReaderViewModel.ReaderUiState.Error -> {
                        showLoading(false)
                        binding.root.showSnackbar(state.message, actionText = "Retry") {
                            viewModel.refreshChapter()
                        }
                    }
                }
            }
        }

        // Observe chapter content
        viewModel.chapterParagraphs.observeWithLifecycle(viewLifecycleOwner) { paragraphs ->
            binding.readerView.setContent(paragraphs)
        }

        // Observe current chapter
        viewModel.currentChapter.observeWithLifecycle(viewLifecycleOwner) { chapter ->
            binding.txtChapterTitle.text = chapter.title
        }

        // Observe reading progress
        viewModel.readingProgress.observeWithLifecycle(viewLifecycleOwner) { position ->
            if (position > 0) {
                // Restore reading position after content is loaded
                binding.readerView.post {
                    binding.readerView.scrollTo(0, position)
                    lastSavedPosition = position
                }
            }
        }

        // Observe reader settings
        viewModel.readerSettings.observeWithLifecycle(viewLifecycleOwner) { settings ->
            updateSettingsUI(settings)
            binding.readerView.applySettings(settings)
        }

        // Observe next/previous chapter availability
        viewModel.nextChapter.observeWithLifecycle(viewLifecycleOwner) { nextChapter ->
            binding.btnNextChapter.isEnabled = nextChapter != null
            binding.btnNextChapter.alpha = if (nextChapter != null) 1.0f else 0.5f
        }

        viewModel.previousChapter.observeWithLifecycle(viewLifecycleOwner) { previousChapter ->
            binding.btnPreviousChapter.isEnabled = previousChapter != null
            binding.btnPreviousChapter.alpha = if (previousChapter != null) 1.0f else 0.5f
        }

        // Observe TTS state
        viewModel.isTtsPlaying.observeWithLifecycle(viewLifecycleOwner) { isPlaying ->
            isTtsActive = isPlaying
            updateTtsButton(isPlaying)
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                binding.root.showSnackbar(error)
                Timber.e("Error in ReaderFragment: $error")
            }
        }
    }

    private fun updateSettingsUI(settings: ReaderSettingsUiModel) {
        // Update text size value
        binding.txtTextSizeValue.text = settings.textSize.toString()

        // Update line spacing value
        binding.txtLineSpacingValue.text = String.format("%.1f", settings.lineSpacing)

        // Update theme radio buttons
        val themeRadioId = when (settings.theme) {
            ReaderSettingsUiModel.ReaderTheme.LIGHT -> R.id.radioLight
            ReaderSettingsUiModel.ReaderTheme.DARK -> R.id.radioDark
            ReaderSettingsUiModel.ReaderTheme.SEPIA -> R.id.radioSepia
            ReaderSettingsUiModel.ReaderTheme.BLACK -> R.id.radioBlack
            else -> R.id.radioLight
        }
        binding.radioGroupTheme.check(themeRadioId)

        // Update text alignment radio buttons
        val alignmentRadioId = when (settings.textAlignment) {
            ReaderSettingsUiModel.TextAlignment.LEFT -> R.id.radioLeft
            ReaderSettingsUiModel.TextAlignment.CENTER -> R.id.radioCenter
            ReaderSettingsUiModel.TextAlignment.RIGHT -> R.id.radioRight
            ReaderSettingsUiModel.TextAlignment.JUSTIFIED -> R.id.radioJustified
        }
        binding.radioGroupAlignment.check(alignmentRadioId)

        // Update font family spinner
        val fontFamilyAdapter = binding.spinnerFontFamily.adapter
        for (i in 0 until fontFamilyAdapter.count) {
            if (fontFamilyAdapter.getItem(i) == settings.fontFamily) {
                binding.spinnerFontFamily.setSelection(i)
                break
            }
        }

        // Update switches
        binding.switchKeepScreenOn.isChecked = settings.isKeepScreenOn
        binding.switchSystemBrightness.isChecked = settings.isSystemBrightness

        // Update brightness seek bar
        binding.seekBarBrightness.progress = settings.brightness
        binding.seekBarBrightness.isEnabled = !settings.isSystemBrightness

        // Apply screen orientation
        binding.switchOrientation.isChecked = requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Apply keep screen on setting
        requireActivity().window.addFlags(
            if (settings.isKeepScreenOn) WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            else 0
        )
    }

    private fun updateTextSize(size: Int) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(textSize = size)

        binding.txtTextSizeValue.text = size.toString()
        binding.readerView.applySettings(updatedSettings)

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateLineSpacing(spacing: Float) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val formattedSpacing = String.format("%.1f", spacing).toFloat()
        val updatedSettings = currentSettings.copy(lineSpacing = formattedSpacing)

        binding.txtLineSpacingValue.text = String.format("%.1f", formattedSpacing)
        binding.readerView.applySettings(updatedSettings)

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateReaderTheme(theme: ReaderSettingsUiModel.ReaderTheme) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(theme = theme)

        binding.readerView.applySettings(updatedSettings)

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateFontFamily(fontFamily: String) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(fontFamily = fontFamily)

        binding.readerView.applySettings(updatedSettings)

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateTextAlignment(alignment: ReaderSettingsUiModel.TextAlignment) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(textAlignment = alignment)

        binding.readerView.applySettings(updatedSettings)

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateKeepScreenOn(keepScreenOn: Boolean) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(isKeepScreenOn = keepScreenOn)

        if (keepScreenOn) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateSystemBrightness(useSystem: Boolean) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(isSystemBrightness = useSystem)

        binding.seekBarBrightness.isEnabled = !useSystem

        if (useSystem) {
            // Reset to system brightness
            val lp = requireActivity().window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            requireActivity().window.attributes = lp
        } else {
            // Use custom brightness
            updateBrightness(updatedSettings.brightness)
        }

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun updateBrightness(brightnessValue: Int) {
        val currentSettings = viewModel.readerSettings.value ?: return
        val updatedSettings = currentSettings.copy(brightness = brightnessValue)

        // Apply brightness (0-100 to 0.0-1.0)
        val brightnessFloat = brightnessValue / 100f
        val lp = requireActivity().window.attributes
        lp.screenBrightness = brightnessFloat
        requireActivity().window.attributes = lp

        // Do not save settings yet, wait for bottom sheet close
    }

    private fun setScreenOrientation(landscape: Boolean) {
        val orientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        requireActivity().requestedOrientation = orientation
    }

    private fun toggleSettingsSheet() {
        if (isSettingsSheetVisible) {
            settingsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            if (isSearchSheetVisible) {
                searchSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            settingsSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun toggleSearchSheet() {
        if (isSearchSheetVisible) {
            searchSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            if (isSettingsSheetVisible) {
                settingsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            searchSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.editSearch.requestFocus()
        }
    }

    private fun toggleSystemUi() {
        if (isSystemUiVisible) {
            hideSystemUi()
        } else {
            showSystemUi()
        }
    }

    private fun hideSystemUi() {
        isSystemUiVisible = false

        // Hide the system bars
        val windowInsetsController =
            WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)

        // Configure the behavior of the hidden system bars
        windowInsetsController.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Hide the toolbar and controls
        binding.appBarLayout.animate()
            .translationY(-binding.appBarLayout.height.toFloat())
            .setDuration(200)
            .start()

        binding.bottomNavigation.animate()
            .translationY(binding.bottomNavigation.height.toFloat())
            .setDuration(200)
            .start()
    }

    private fun showSystemUi() {
        isSystemUiVisible = true

        // Show the system bars
        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())

        // Show the toolbar and controls
        binding.appBarLayout.animate()
            .translationY(0f)
            .setDuration(200)
            .start()

        binding.bottomNavigation.animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            binding.root.showSnackbar(getString(R.string.search_query_empty))
            return
        }

        val resultCount = binding.readerView.highlightSearchResults(query)

        if (resultCount > 0) {
            binding.txtSearchResults.text = getString(R.string.search_results_count, resultCount)
            binding.readerView.navigateToNextSearchResult()
        } else {
            binding.txtSearchResults.text = getString(R.string.search_no_results)
        }
    }

    private fun navigateToNextSearchResult() {
        if (!binding.readerView.navigateToNextSearchResult()) {
            binding.root.showSnackbar(getString(R.string.search_end_reached))
        }
    }

    private fun navigateToPreviousSearchResult() {
        if (!binding.readerView.navigateToPreviousSearchResult()) {
            binding.root.showSnackbar(getString(R.string.search_beginning_reached))
        }
    }

    private fun closeSearch() {
        binding.editSearch.text?.clear()
        binding.readerView.clearHighlights()
        searchSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun toggleAutoScroll(enabled: Boolean) {
        isAutoScrolling = enabled

        if (enabled) {
            autoScrollSpeed = binding.seekBarScrollSpeed.progress + 10
            startAutoScroll()
        } else {
            stopAutoScroll()
        }
    }

    private fun updateAutoScrollSpeed(speed: Int) {
        autoScrollSpeed = speed + 10 // minimum speed of 10

        if (isAutoScrolling) {
            // Restart auto-scroll with new speed
            stopAutoScroll()
            startAutoScroll()
        }
    }

    private fun startAutoScroll() {
        stopAutoScroll()

        autoScrollJob = lifecycleScope.launch {
            while (isActive) {
                binding.readerView.scrollBy(0, 1)
                delay(1000 / autoScrollSpeed.toLong())
            }
        }
    }

    private fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    private fun toggleTextToSpeech() {
        if (!ttsInitialized) {
            binding.root.showSnackbar(getString(R.string.tts_not_initialized))
            return
        }

        if (isTtsActive) {
            stopTextToSpeech()
        } else {
            startTextToSpeech()
        }
    }

    private fun startTextToSpeech() {
        if (textToSpeech == null || !ttsInitialized) return

        val chapterContent = viewModel.chapterContent.value ?: return

        if (chapterContent.isBlank()) {
            binding.root.showSnackbar(getString(R.string.tts_no_content))
            return
        }

        // Split content into manageable chunks for TTS
        // TTS has a limit on utterance length
        val maxUtteranceLength = 4000
        val chunks = mutableListOf<String>()

        var start = 0
        while (start < chapterContent.length) {
            val end = minOf(start + maxUtteranceLength, chapterContent.length)

            // Try to find a good breaking point (end of sentence or paragraph)
            var breakPoint = end
            if (end < chapterContent.length) {
                // Look for a sentence end (., !, ?) followed by a space
                for (i in end downTo start + 100) {
                    if (i < chapterContent.length &&
                        (chapterContent[i] == '.' || chapterContent[i] == '!' || chapterContent[i] == '?') &&
                        (i + 1 == chapterContent.length || chapterContent[i + 1].isWhitespace())
                    ) {
                        breakPoint = i + 1
                        break
                    }
                }
            }

            chunks.add(chapterContent.substring(start, breakPoint))
            start = breakPoint
        }

        // Queue all chunks for speaking
        textToSpeech?.apply {
            stop()
            setSpeechRate(0.9f) // Slightly slower than normal
            setPitch(1.0f) // Normal pitch

            chunks.forEachIndexed { index, chunk ->
                speak(
                    chunk,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "ChunkID_$index"
                )
            }

            // Update UI to show TTS is active
            viewModel.toggleTextToSpeech()
        }
    }

    private fun stopTextToSpeech() {
        textToSpeech?.stop()
        viewModel.stopTextToSpeech()
    }

    private fun updateTtsButton(isPlaying: Boolean) {
        binding.btnTts.setIconResource(
            if (isPlaying) R.drawable.ic_stop
            else R.drawable.ic_text_to_speech
        )

        binding.btnTts.contentDescription =
            if (isPlaying) getString(R.string.stop_text_to_speech)
            else getString(R.string.start_text_to_speech)
    }

    private fun toggleBookmark() {
        val chapter = viewModel.currentChapter.value ?: return

        // Toggle bookmark status
        val bookmarked = !chapter.isBookmarked
        viewModel.toggleChapterBookmark(chapter.id, bookmarked)

        // Update UI
        val message = if (bookmarked) {
            getString(R.string.chapter_bookmarked)
        } else {
            getString(R.string.chapter_bookmark_removed)
        }
        requireContext().showToast(message)
    }

    private fun updateChapterNavigation() {
        val nextChapter = viewModel.nextChapter.value
        val previousChapter = viewModel.previousChapter.value

        // Update navigation button visibility
        binding.btnPreviousChapter.isEnabled = previousChapter != null
        binding.btnNextChapter.isEnabled = nextChapter != null

        // Update toolbar subtitle with chapter navigation info
        val chapterNumber = args.chapterNumber
        val chapterInfo = buildString {
            append("Chapter $chapterNumber")
            if (nextChapter != null || previousChapter != null) {
                append(" | ")
                if (previousChapter != null) {
                    append("Prev: ${previousChapter.number}")
                }
                if (previousChapter != null && nextChapter != null) {
                    append(" | ")
                }
                if (nextChapter != null) {
                    append("Next: ${nextChapter.number}")
                }
            }
        }
        binding.txtChapterNumber.text = chapterInfo
    }

    private fun navigateToTableOfContents() {
        val action = ReaderFragmentDirections.actionReaderFragmentToNovelDetailFragment(
            novelId = args.novelId,
            sourceId = args.sourceId,
            title = args.novelTitle
        )
        findNavController().navigate(action)
    }

    private fun navigateToNovelDetails() {
        val action = ReaderFragmentDirections.actionReaderFragmentToNovelDetailFragment(
            novelId = args.novelId,
            sourceId = args.sourceId,
            title = args.novelTitle
        )
        findNavController().navigate(action)
    }

    private fun shareChapter() {
        val chapter = viewModel.currentChapter.value ?: return

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "${args.novelTitle} - ${chapter.title}")
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "I'm reading ${chapter.title} of ${args.novelTitle}!"
            )
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_chapter)))
    }

    private fun saveReaderSettings() {
        val currentSettings = viewModel.readerSettings.value ?: return
        viewModel.saveReaderSettings(currentSettings)
    }

    private fun startProgressSavingJob() {
        uiUpdateJob = lifecycleScope.launch {
            while (isActive) {
                delay(saveProgressInterval)

                val currentPosition = binding.readerView.scrollY
                if (currentPosition != lastSavedPosition) {
                    viewModel.saveReadingProgress(currentPosition)
                    lastSavedPosition = currentPosition
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.show()
            binding.readerView.hide()
        } else {
            binding.progressBar.hide()
            binding.readerView.show()
        }
    }

    // TextToSpeech initialization listener
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language for TTS
            val result = textToSpeech?.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.e("Language not supported for TTS")
                binding.root.showSnackbar(getString(R.string.tts_language_not_supported))
                ttsInitialized = false
            } else {
                ttsInitialized = true

                // Add utterance progress listener
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            // Not needed
                        }

                        override fun onDone(utteranceId: String?) {
                            // Check if this was the last chunk
                            if (utteranceId?.startsWith("ChunkID_") == true) {
                                val chunkId = utteranceId.substringAfter("ChunkID_").toIntOrNull()
                                val chunksCount = viewModel.chapterParagraphs.value?.size ?: 0

                                if (chunkId != null && chunkId == chunksCount - 1) {
                                    // Last chunk completed, stop TTS
                                    lifecycleScope.launch {
                                        viewModel.stopTextToSpeech()
                                    }
                                }
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            lifecycleScope.launch {
                                binding.root.showSnackbar(getString(R.string.tts_error))
                                viewModel.stopTextToSpeech()
                            }
                        }
                    })
                }
            }
        } else {
            Timber.e("Failed to initialize TTS")
            binding.root.showSnackbar(getString(R.string.tts_initialization_failed))
            ttsInitialized = false
        }
    }

    override fun onPause() {
        super.onPause()

        // Save current progress
        val currentPosition = binding.readerView.scrollY
        if (currentPosition != lastSavedPosition) {
            viewModel.saveReadingProgress(currentPosition)
            lastSavedPosition = currentPosition
        }

        // Stop text-to-speech if active
        if (isTtsActive) {
            stopTextToSpeech()
        }

        // Stop auto-scroll if active
        if (isAutoScrolling) {
            stopAutoScroll()
        }
    }

    override fun onDestroy() {
        // Clean up text-to-speech
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        // Cancel jobs
        uiUpdateJob?.cancel()
        autoScrollJob?.cancel()

        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}