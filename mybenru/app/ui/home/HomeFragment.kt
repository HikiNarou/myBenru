package com.mybenru.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mybenru.app.R
import com.mybenru.app.adapter.NovelAdapter
import com.mybenru.app.databinding.FragmentHomeBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.extension.safeNavigate
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.hide
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment for the home screen with recommendations and reading progress
 */
@AndroidEntryPoint
class HomeFragment : Fragment(), NovelAdapter.NovelClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    // Adapters
    private lateinit var continueReadingAdapter: NovelAdapter
    private lateinit var recentNovelsAdapter: NovelAdapter
    private lateinit var popularNovelsAdapter: NovelAdapter
    private lateinit var recommendedNovelsAdapter: NovelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAdapters()
        setupListeners()
        observeViewModel()

        // Load data
        viewModel.loadHomeData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.app_name)

        binding.btnSearch.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToExploreFragment()
            findNavController().safeNavigate(action)
        }

        binding.btnSettings.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            findNavController().safeNavigate(action)
        }
    }

    private fun setupAdapters() {
        // Continue Reading adapter
        continueReadingAdapter = NovelAdapter(NovelAdapter.DisplayMode.HORIZONTAL, this).apply {
            setHasStableIds(true)
        }
        binding.rvContinueReading.apply {
            adapter = continueReadingAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        // Recent Novels adapter
        recentNovelsAdapter = NovelAdapter(NovelAdapter.DisplayMode.HORIZONTAL, this).apply {
            setHasStableIds(true)
        }
        binding.rvRecentNovels.apply {
            adapter = recentNovelsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        // Popular Novels adapter
        popularNovelsAdapter = NovelAdapter(NovelAdapter.DisplayMode.HORIZONTAL, this).apply {
            setHasStableIds(true)
        }
        binding.rvPopularNovels.apply {
            adapter = popularNovelsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }

        // Recommended Novels adapter
        recommendedNovelsAdapter = NovelAdapter(NovelAdapter.DisplayMode.HORIZONTAL, this).apply {
            setHasStableIds(true)
        }
        binding.rvRecommendedNovels.apply {
            adapter = recommendedNovelsAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadHomeData()
        }

        // View all continue reading
        binding.txtViewAllContinueReading.setOnClickListener {
            navigateToNovelList(
                getString(R.string.continue_reading),
                "continue_reading"
            )
        }

        // View all recent novels
        binding.txtViewAllRecent.setOnClickListener {
            navigateToNovelList(
                getString(R.string.recent_novels),
                "recent"
            )
        }

        // View all popular novels
        binding.txtViewAllPopular.setOnClickListener {
            navigateToNovelList(
                getString(R.string.popular_novels),
                "popular"
            )
        }

        // View all recommended novels
        binding.txtViewAllRecommended.setOnClickListener {
            navigateToNovelList(
                getString(R.string.recommended_for_you),
                "recommended"
            )
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }

        // Observe continue reading novels
        viewModel.continueReadingNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updateContinueReadingSection(novels)
        }

        // Observe recent novels
        viewModel.recentNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updateRecentNovelsSection(novels)
        }

        // Observe popular novels
        viewModel.popularNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updatePopularNovelsSection(novels)
        }

        // Observe recommended novels
        viewModel.recommendedNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updateRecommendedNovelsSection(novels)
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.root.showSnackbar(errorMessage)
                Timber.e("Error in HomeFragment: $errorMessage")
            }
        }
    }

    private fun handleUiState(state: HomeViewModel.HomeUiState) {
        binding.swipeRefreshLayout.isRefreshing = state is HomeViewModel.HomeUiState.Loading

        when (state) {
            is HomeViewModel.HomeUiState.Loading -> {
                // Show loading indicators for individual sections
                binding.progressBarRecent.isVisible = true
                binding.progressBarPopular.isVisible = true
                binding.progressBarRecommended.isVisible = true
            }
            is HomeViewModel.HomeUiState.Success -> {
                // Hide loading indicators
                binding.progressBarRecent.hide()
                binding.progressBarPopular.hide()
                binding.progressBarRecommended.hide()

                // Success state may have additional handling if needed
            }
            is HomeViewModel.HomeUiState.Error -> {
                // Hide loading indicators
                binding.progressBarRecent.hide()
                binding.progressBarPopular.hide()
                binding.progressBarRecommended.hide()

                // Error is shown through errorEvent LiveData
            }
        }
    }

    private fun updateContinueReadingSection(novels: List<NovelUiModel>) {
        continueReadingAdapter.submitList(novels)

        // Show/hide "continue reading" section based on data availability
        binding.layoutContinueReading.isVisible = novels.isNotEmpty()
    }

    private fun updateRecentNovelsSection(novels: List<NovelUiModel>) {
        recentNovelsAdapter.submitList(novels)

        // Show/hide "recent novels" section based on data availability
        binding.layoutRecentNovels.isVisible = novels.isNotEmpty()
    }

    private fun updatePopularNovelsSection(novels: List<NovelUiModel>) {
        popularNovelsAdapter.submitList(novels)

        // Show/hide "popular novels" section based on data availability
        binding.layoutPopularNovels.isVisible = novels.isNotEmpty()
    }

    private fun updateRecommendedNovelsSection(novels: List<NovelUiModel>) {
        recommendedNovelsAdapter.submitList(novels)

        // Show/hide "recommended novels" section based on data availability
        binding.layoutRecommendedNovels.isVisible = novels.isNotEmpty()
    }

    private fun navigateToNovelList(title: String, listType: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToNovelListFragment(
            title = title,
            listType = listType
        )
        findNavController().safeNavigate(action)
    }

    // NovelClickListener implementations
    override fun onNovelClicked(novel: NovelUiModel) {
        val action = HomeFragmentDirections.actionHomeFragmentToNovelDetailFragment(
            novelId = novel.id,
            sourceId = novel.sourceId,
            title = novel.title
        )
        findNavController().safeNavigate(action)
    }

    override fun onNovelLongClicked(novel: NovelUiModel): Boolean {
        // Show quick actions for the novel
        showNovelQuickActions(novel)
        return true
    }

    override fun onAddToLibraryClicked(novel: NovelUiModel) {
        viewModel.addNovelToLibrary(novel)
        requireContext().showToast(getString(R.string.novel_added_to_library))
    }

    override fun onMoreOptionsClicked(novel: NovelUiModel, view: View) {
        showNovelOptionsMenu(novel, view)
    }

    private fun showNovelQuickActions(novel: NovelUiModel) {
        val options = arrayOf(
            getString(R.string.view_details),
            getString(if (novel.isInLibrary) R.string.remove_from_library else R.string.add_to_library),
            getString(R.string.start_reading),
            getString(R.string.share)
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(novel.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onNovelClicked(novel)
                    1 -> toggleLibraryStatus(novel)
                    2 -> startReading(novel)
                    3 -> shareNovel(novel)
                }
            }
            .show()
    }

    private fun showNovelOptionsMenu(novel: NovelUiModel, anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_novel_item, popup.menu)

        // Update menu items based on novel state
        val menuItem = popup.menu.findItem(R.id.action_add_to_library)
        menuItem.title = getString(
            if (novel.isInLibrary) R.string.remove_from_library
            else R.string.add_to_library
        )

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_details -> {
                    onNovelClicked(novel)
                    true
                }
                R.id.action_add_to_library -> {
                    toggleLibraryStatus(novel)
                    true
                }
                R.id.action_start_reading -> {
                    startReading(novel)
                    true
                }
                R.id.action_share -> {
                    shareNovel(novel)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun toggleLibraryStatus(novel: NovelUiModel) {
        if (novel.isInLibrary) {
            viewModel.removeNovelFromLibrary(novel)
            requireContext().showToast(getString(R.string.novel_removed_from_library))
        } else {
            viewModel.addNovelToLibrary(novel)
            requireContext().showToast(getString(R.string.novel_added_to_library))
        }
    }

    private fun startReading(novel: NovelUiModel) {
        // Try to continue reading if the user has started the novel
        if (novel.lastReadChapter != null) {
            viewModel.getChapterDetail(
                novelId = novel.id,
                chapterId = novel.lastReadChapter.toString()
            ) { chapterDetail ->
                if (chapterDetail != null) {
                    navigateToReader(
                        novel.id,
                        novel.sourceId,
                        chapterDetail.id,
                        chapterDetail.number,
                        novel.title,
                        chapterDetail.title
                    )
                } else {
                    // If can't get last read chapter, start from beginning
                    viewModel.getFirstChapter(novel.id) { firstChapter ->
                        if (firstChapter != null) {
                            navigateToReader(
                                novel.id,
                                novel.sourceId,
                                firstChapter.id,
                                firstChapter.number,
                                novel.title,
                                firstChapter.title
                            )
                        } else {
                            // No chapters available
                            requireContext().showToast(getString(R.string.no_chapters_available))
                        }
                    }
                }
            }
        } else {
            // Start from the beginning
            viewModel.getFirstChapter(novel.id) { firstChapter ->
                if (firstChapter != null) {
                    navigateToReader(
                        novel.id,
                        novel.sourceId,
                        firstChapter.id,
                        firstChapter.number,
                        novel.title,
                        firstChapter.title
                    )
                } else {
                    // No chapters available
                    requireContext().showToast(getString(R.string.no_chapters_available))
                }
            }
        }
    }

    private fun navigateToReader(
        novelId: String,
        sourceId: String,
        chapterId: String,
        chapterNumber: Int,
        novelTitle: String,
        chapterTitle: String?
    ) {
        val action = HomeFragmentDirections.actionHomeFragmentToReaderFragment(
            novelId = novelId,
            sourceId = sourceId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            novelTitle = novelTitle,
            chapterTitle = chapterTitle
        )
        findNavController().safeNavigate(action)
    }

    private fun shareNovel(novel: NovelUiModel) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, novel.title)
            putExtra(android.content.Intent.EXTRA_TEXT,
                getString(
                    R.string.share_novel_text,
                    novel.title,
                    novel.getFormattedAuthors(),
                    "https://mybenru.com/novel/${novel.id}"
                )
            )
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_novel)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}