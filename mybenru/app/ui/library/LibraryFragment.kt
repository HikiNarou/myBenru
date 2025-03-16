package com.mybenru.app.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybenru.app.R
import com.mybenru.app.adapter.FilterAdapter
import com.mybenru.app.adapter.NovelAdapter
import com.mybenru.app.databinding.FragmentLibraryBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.extension.safeNavigate
import com.mybenru.app.model.LibraryFilterUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.hide
import com.mybenru.app.utils.show
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment for displaying and managing library novels
 */
@AndroidEntryPoint
class LibraryFragment : Fragment(), NovelAdapter.NovelClickListener, FilterAdapter.FilterClickListener {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels()

    private lateinit var novelsAdapter: NovelAdapter
    private lateinit var filtersAdapter: FilterAdapter

    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAdapters()
        setupListeners()
        setupSearchView()
        observeViewModel()

        // Load library data
        viewModel.loadLibrary()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.title_library)

        binding.btnSearch.setOnClickListener {
            toggleSearchView()
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun setupAdapters() {
        // Novels adapter
        novelsAdapter = NovelAdapter(NovelAdapter.DisplayMode.GRID, this).apply {
            setHasStableIds(true)
        }
        binding.rvNovels.apply {
            adapter = novelsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }

        // Filters adapter
        filtersAdapter = FilterAdapter(this).apply {
            setHasStableIds(true)
        }
        binding.rvFilters.apply {
            adapter = filtersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshLibrary()
        }

        // Setup library tabs
        binding.libraryTabs.setTabs(listOf(
            getString(R.string.all),
            getString(R.string.unread),
            getString(R.string.completed),
            getString(R.string.downloaded)
        ))

        binding.libraryTabs.setOnTabSelectedListener { index ->
            when (index) {
                0 -> viewModel.setActiveFilter(LibraryFilterUiModel.FilterType.ALL)
                1 -> viewModel.setActiveFilter(LibraryFilterUiModel.FilterType.UNREAD)
                2 -> viewModel.setActiveFilter(LibraryFilterUiModel.FilterType.COMPLETED)
                3 -> viewModel.setActiveFilter(LibraryFilterUiModel.FilterType.DOWNLOADED)
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchLibrary(newText.orEmpty())
                currentSearchQuery = newText.orEmpty()
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            binding.searchView.hide()
            false
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }

        // Observe novels
        viewModel.novels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updateNovelsList(novels)
        }

        // Observe filters
        viewModel.filters.observeWithLifecycle(viewLifecycleOwner) { filters ->
            filtersAdapter.submitList(filters)
            binding.rvFilters.isVisible = filters.isNotEmpty()
        }

        // Observe novel count
        viewModel.novelCount.observeWithLifecycle(viewLifecycleOwner) { count ->
            updateNovelCount(count)
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.root.showSnackbar(errorMessage)
                Timber.e("Error in LibraryFragment: $errorMessage")
            }
        }
    }

    private fun handleUiState(state: LibraryViewModel.LibraryUiState) {
        binding.swipeRefreshLayout.isRefreshing = state is LibraryViewModel.LibraryUiState.Loading
        binding.progressBar.isVisible = state is LibraryViewModel.LibraryUiState.Loading

        // Handle empty state
        binding.layoutEmpty.isVisible = state is LibraryViewModel.LibraryUiState.Empty

        // Handle no results state
        binding.layoutNoResults.isVisible = state is LibraryViewModel.LibraryUiState.NoResults && currentSearchQuery.isNotEmpty()
    }

    private fun updateNovelsList(novels: List<NovelUiModel>) {
        novelsAdapter.submitList(novels)
    }

    private fun updateNovelCount(count: Int) {
        binding.txtNovelCount.text = resources.getQuantityString(R.plurals.novel_count, count, count)
    }

    private fun toggleSearchView() {
        if (binding.searchView.isVisible) {
            binding.searchView.hide()
            binding.searchView.setQuery("", false)
        } else {
            binding.searchView.show()
            binding.searchView.requestFocus()
        }
    }

    private fun showFilterDialog() {
        val filterTypes = arrayOf(
            getString(R.string.all),
            getString(R.string.unread),
            getString(R.string.completed),
            getString(R.string.downloaded),
            getString(R.string.by_author),
            getString(R.string.by_genre),
            getString(R.string.by_status),
            getString(R.string.recently_read),
            getString(R.string.recently_added)
        )

        val currentFilterIndex = when (viewModel.getCurrentFilterType()) {
            LibraryFilterUiModel.FilterType.ALL -> 0
            LibraryFilterUiModel.FilterType.UNREAD -> 1
            LibraryFilterUiModel.FilterType.COMPLETED -> 2
            LibraryFilterUiModel.FilterType.DOWNLOADED -> 3
            LibraryFilterUiModel.FilterType.BY_AUTHOR -> 4
            LibraryFilterUiModel.FilterType.BY_GENRE -> 5
            LibraryFilterUiModel.FilterType.BY_STATUS -> 6
            LibraryFilterUiModel.FilterType.RECENTLY_READ -> 7
            LibraryFilterUiModel.FilterType.RECENTLY_ADDED -> 8
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_by)
            .setSingleChoiceItems(filterTypes, currentFilterIndex) { dialog, which ->
                val filterType = when (which) {
                    0 -> LibraryFilterUiModel.FilterType.ALL
                    1 -> LibraryFilterUiModel.FilterType.UNREAD
                    2 -> LibraryFilterUiModel.FilterType.COMPLETED
                    3 -> LibraryFilterUiModel.FilterType.DOWNLOADED
                    4 -> LibraryFilterUiModel.FilterType.BY_AUTHOR
                    5 -> LibraryFilterUiModel.FilterType.BY_GENRE
                    6 -> LibraryFilterUiModel.FilterType.BY_STATUS
                    7 -> LibraryFilterUiModel.FilterType.RECENTLY_READ
                    8 -> LibraryFilterUiModel.FilterType.RECENTLY_ADDED
                    else -> LibraryFilterUiModel.FilterType.ALL
                }

                viewModel.setActiveFilter(filterType)

                // Handle special filters
                when (filterType) {
                    LibraryFilterUiModel.FilterType.BY_AUTHOR -> showAuthorFilterDialog()
                    LibraryFilterUiModel.FilterType.BY_GENRE -> showGenreFilterDialog()
                    LibraryFilterUiModel.FilterType.BY_STATUS -> showStatusFilterDialog()
                    else -> { /* No special handling needed */ }
                }

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            getString(R.string.sort_alphabetical),
            getString(R.string.sort_last_read),
            getString(R.string.sort_last_updated),
            getString(R.string.sort_date_added),
            getString(R.string.sort_unread_count)
        )

        val currentSortIndex = viewModel.getCurrentSortIndex()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(sortOptions, currentSortIndex) { dialog, which ->
                viewModel.setSortOption(which)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAuthorFilterDialog() {
        val authors = viewModel.getAvailableAuthors()

        if (authors.isEmpty()) {
            requireContext().showToast(getString(R.string.no_authors_found))
            return
        }

        val authorArray = authors.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_author)
            .setItems(authorArray) { _, which ->
                viewModel.applyAuthorFilter(authors[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showGenreFilterDialog() {
        val genres = viewModel.getAvailableGenres()

        if (genres.isEmpty()) {
            requireContext().showToast(getString(R.string.no_genres_found))
            return
        }

        val genreArray = genres.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_genre)
            .setItems(genreArray) { _, which ->
                viewModel.applyGenreFilter(genres[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showStatusFilterDialog() {
        val statuses = arrayOf(
            getString(R.string.status_ongoing),
            getString(R.string.status_completed),
            getString(R.string.status_hiatus),
            getString(R.string.status_dropped)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_status)
            .setItems(statuses) { _, which ->
                val status = when (which) {
                    0 -> "ongoing"
                    1 -> "completed"
                    2 -> "hiatus"
                    3 -> "dropped"
                    else -> ""
                }
                viewModel.applyStatusFilter(status)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // NovelClickListener implementations
    override fun onNovelClicked(novel: NovelUiModel) {
        val action = LibraryFragmentDirections.actionLibraryFragmentToNovelDetailFragment(
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
        // This shouldn't be called in library fragment as all novels are already in the library
    }

    override fun onMoreOptionsClicked(novel: NovelUiModel, view: View) {
        showNovelOptionsMenu(novel, view)
    }

    private fun showNovelQuickActions(novel: NovelUiModel) {
        val options = arrayOf(
            getString(R.string.view_details),
            getString(R.string.remove_from_library),
            getString(R.string.start_reading),
            getString(R.string.download),
            getString(R.string.share)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(novel.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onNovelClicked(novel)
                    1 -> removeFromLibrary(novel)
                    2 -> startReading(novel)
                    3 -> downloadNovel(novel)
                    4 -> shareNovel(novel)
                }
            }
            .show()
    }

    private fun showNovelOptionsMenu(novel: NovelUiModel, anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_library_novel_item, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_details -> {
                    onNovelClicked(novel)
                    true
                }
                R.id.action_remove_from_library -> {
                    removeFromLibrary(novel)
                    true
                }
                R.id.action_start_reading -> {
                    startReading(novel)
                    true
                }
                R.id.action_download -> {
                    downloadNovel(novel)
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

    private fun removeFromLibrary(novel: NovelUiModel) {
        // Show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_from_library)
            .setMessage(getString(R.string.remove_from_library_confirmation, novel.title))
            .setPositiveButton(R.string.remove) { _, _ ->
                viewModel.removeNovelFromLibrary(novel)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
        val action = LibraryFragmentDirections.actionLibraryFragmentToReaderFragment(
            novelId = novelId,
            sourceId = sourceId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            novelTitle = novelTitle,
            chapterTitle = chapterTitle
        )
        findNavController().safeNavigate(action)
    }

    private fun downloadNovel(novel: NovelUiModel) {
        // Navigate to novel detail with download tab selected
        val action = LibraryFragmentDirections.actionLibraryFragmentToNovelDetailFragment(
            novelId = novel.id,
            sourceId = novel.sourceId,
            title = novel.title
        )
        findNavController().safeNavigate(action)
        requireContext().showToast(getString(R.string.open_chapters_to_download))
    }

    private fun shareNovel(novel: NovelUiModel) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, novel.title)
            putExtra(
                android.content.Intent.EXTRA_TEXT,
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

    // FilterClickListener implementation
    override fun onFilterClicked(filter: LibraryFilterUiModel) {
        viewModel.toggleFilter(filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}