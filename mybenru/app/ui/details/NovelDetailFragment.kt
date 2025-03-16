package com.mybenru.app.ui.details

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mybenru.app.R
import com.mybenru.app.adapter.ChapterAdapter
import com.mybenru.app.adapter.NovelAdapter
import com.mybenru.app.databinding.FragmentNovelDetailBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.model.ChapterUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.hide
import com.mybenru.app.utils.loadImage
import com.mybenru.app.utils.show
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.NovelDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment for displaying novel details and chapters
 */
@AndroidEntryPoint
class NovelDetailFragment : Fragment(), ChapterAdapter.ChapterClickListener, NovelAdapter.NovelClickListener {

    private var _binding: FragmentNovelDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NovelDetailViewModel by viewModels()
    private val args: NovelDetailFragmentArgs by navArgs()

    // Adapters
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var relatedNovelsAdapter: NovelAdapter

    // Flag to track download mode
    private var isInDownloadMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNovelDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerViews()
        setupTabLayout()
        setupListeners()
        observeViewModel()

        // Load novel data using args
        viewModel.loadNovel(args.novelId, args.sourceId)
    }

    private fun setupToolbar() {
        binding.toolbar.title = args.title ?: getString(R.string.novel_details)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_share -> {
                    shareNovel()
                    true
                }
                R.id.action_browser -> {
                    openInBrowser()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerViews() {
        // Set up Chapters RecyclerView
        chapterAdapter = ChapterAdapter(this)
        binding.rvChapters.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chapterAdapter
            setHasFixedSize(true)
        }

        // Set up Related Novels RecyclerView
        relatedNovelsAdapter = NovelAdapter(this)
        binding.rvRelatedNovels.apply {
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = relatedNovelsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Info tab
                        binding.layoutInfo.show()
                        binding.layoutChapters.hide()
                    }
                    1 -> { // Chapters tab
                        binding.layoutInfo.hide()
                        binding.layoutChapters.show()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // Library button
        binding.fabLibrary.setOnClickListener {
            handleLibraryButtonClick()
        }

        // Read button
        binding.btnRead.setOnClickListener {
            // Navigate to first chapter if never read, or continue from last read
            navigateToReader()
        }

        // Download button
        binding.btnDownload.setOnClickListener {
            toggleDownloadMode()
        }

        // Download selected button
        binding.btnDownloadSelected.setOnClickListener {
            downloadSelectedChapters()
        }

        // Select all button
        binding.btnSelectAll.setOnClickListener {
            chapterAdapter.selectAllForDownload()
        }

        // Invert selection button
        binding.btnInvertSelection.setOnClickListener {
            chapterAdapter.invertSelectedForDownload()
        }

        // Cancel download mode button
        binding.btnCancelDownload.setOnClickListener {
            toggleDownloadMode()
        }

        // Mark all read button
        binding.btnMarkAllRead.setOnClickListener {
            showMarkAllReadDialog()
        }

        // Chapter filter button
        binding.btnFilter.setOnClickListener {
            showChapterFilterMenu(it)
        }

        // Chapter sort button
        binding.btnSort.setOnClickListener {
            showChapterSortMenu(it)
        }

        // Expand description button
        binding.btnExpandDescription.setOnClickListener {
            toggleDescriptionExpanded()
        }

        // Swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshNovel()
        }
    }

    private fun toggleDescriptionExpanded() {
        val isExpanded = binding.txtDescription.maxLines == Integer.MAX_VALUE

        if (isExpanded) {
            // Collapse
            binding.txtDescription.maxLines = 4
            binding.btnExpandDescription.setImageResource(R.drawable.ic_expand_more)
        } else {
            // Expand
            binding.txtDescription.maxLines = Integer.MAX_VALUE
            binding.btnExpandDescription.setImageResource(R.drawable.ic_expand_less)
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is NovelDetailViewModel.NovelDetailUiState.Loading -> {
                        showLoading(true)
                    }
                    is NovelDetailViewModel.NovelDetailUiState.NovelLoaded -> {
                        // Novel loaded, waiting for chapters
                        binding.layoutNovelInfo.show()
                        binding.progressBarChapters.show()
                    }
                    is NovelDetailViewModel.NovelDetailUiState.Success -> {
                        showLoading(false)
                        binding.layoutNovelInfo.show()
                        binding.progressBarChapters.hide()
                    }
                    is NovelDetailViewModel.NovelDetailUiState.Error -> {
                        showLoading(false)
                        binding.root.showSnackbar(state.message, actionText = "Retry") {
                            viewModel.refreshNovel()
                        }
                    }
                }
            }
        }

        // Observe novel detail
        viewModel.novelDetail.observeWithLifecycle(viewLifecycleOwner) { novel ->
            updateNovelUI(novel)
        }

        // Observe chapter list
        viewModel.chapterList.observeWithLifecycle(viewLifecycleOwner) { chapters ->
            updateChaptersUI(chapters)
        }

        // Observe library status
        viewModel.isInLibrary.observeWithLifecycle(viewLifecycleOwner) { isInLibrary ->
            updateLibraryButton(isInLibrary)
        }

        // Observe related novels
        viewModel.relatedNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            if (novels.isEmpty()) {
                binding.layoutRelatedNovels.hide()
            } else {
                binding.layoutRelatedNovels.show()
                relatedNovelsAdapter.submitList(novels)
            }
        }

        // Observe loading states
        viewModel.isNovelLoading.observeWithLifecycle(viewLifecycleOwner) { isLoading ->
            binding.progressBarNovel.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isChaptersLoading.observeWithLifecycle(viewLifecycleOwner) { isLoading ->
            binding.progressBarChapters.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                binding.root.showSnackbar(error)
                Timber.e("Error in NovelDetailFragment: $error")
            }
        }
    }

    private fun updateNovelUI(novel: NovelUiModel) {
        binding.apply {
            // Set cover image
            imgCover.loadImage(novel.coverUrl, cornerRadius = 8f)

            // Set text fields
            txtTitle.text = novel.title
            txtAuthor.text = novel.getFormattedAuthors()
            txtGenres.text = novel.getFormattedGenres()
            txtStatus.text = novel.status
            txtDescription.text = novel.description

            // Set rating
            ratingBar.rating = novel.rating.toFloatOrNull() ?: 0f
            txtRating.text = novel.rating

            // Set chapter count
            val chapterText = if (novel.totalChapters != null) {
                "${novel.totalChapters} Chapters"
            } else {
                "Unknown chapters"
            }
            txtChapterCount.text = chapterText

            // Update toolbar title with novel title
            toolbar.title = novel.title

            // Update progress if novel is in library
            if (novel.isInLibrary && novel.hasBeenStarted()) {
                progressReading.progress = novel.getProgressPercentage()
                txtProgress.text = novel.getFormattedProgress()
                layoutProgress.show()
            } else {
                layoutProgress.hide()
            }

            // Update the read button text
            if (novel.hasBeenStarted()) {
                btnRead.text = getString(R.string.continue_reading)
            } else {
                btnRead.text = getString(R.string.start_reading)
            }
        }
    }

    private fun updateChaptersUI(chapters: List<ChapterUiModel>) {
        chapterAdapter.submitList(chapters)

        binding.txtChapterCount.text = getString(
            R.string.chapter_count_detail,
            chapters.size
        )

        // Show/hide no chapters message
        if (chapters.isEmpty()) {
            binding.txtNoChapters.show()
            binding.rvChapters.hide()
        } else {
            binding.txtNoChapters.hide()
            binding.rvChapters.show()
        }
    }

    private fun updateLibraryButton(isInLibrary: Boolean) {
        binding.fabLibrary.setImageResource(
            if (isInLibrary) R.drawable.ic_bookmark_filled
            else R.drawable.ic_bookmark_outline
        )

        binding.fabLibrary.contentDescription =
            if (isInLibrary) getString(R.string.remove_from_library)
            else getString(R.string.add_to_library)
    }

    private fun handleLibraryButtonClick() {
        val novel = viewModel.novelDetail.value ?: return
        val isInLibrary = viewModel.isInLibrary.value ?: false

        if (isInLibrary) {
            confirmRemoveFromLibrary()
        } else {
            viewModel.addToLibrary(novel.id, novel.sourceId)
            requireContext().showToast(getString(R.string.added_to_library))
        }
    }

    private fun confirmRemoveFromLibrary() {
        val novel = viewModel.novelDetail.value ?: return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_from_library)
            .setMessage(getString(R.string.remove_from_library_confirm, novel.title))
            .setPositiveButton(R.string.remove) { _, _ ->
                viewModel.removeFromLibrary(novel.id)
                requireContext().showToast(getString(R.string.removed_from_library))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun navigateToReader() {
        val novel = viewModel.novelDetail.value ?: return
        val chapters = viewModel.chapterList.value ?: return

        if (chapters.isEmpty()) {
            requireContext().showToast(getString(R.string.no_chapters_available))
            return
        }

        // If novel has been started, navigate to last read chapter
        if (novel.hasBeenStarted() && novel.lastReadChapter != null) {
            val lastReadChapterNumber = novel.lastReadChapter
            val lastReadChapter = chapters.find { it.number == lastReadChapterNumber }

            if (lastReadChapter != null) {
                navigateToChapter(lastReadChapter)
                return
            }
        }

        // Otherwise navigate to first chapter
        val firstChapter = chapters.minByOrNull { it.number }
        if (firstChapter != null) {
            navigateToChapter(firstChapter)
        } else {
            requireContext().showToast(getString(R.string.no_chapters_available))
        }
    }

    private fun navigateToChapter(chapter: ChapterUiModel) {
        val novel = viewModel.novelDetail.value ?: return

        val action = NovelDetailFragmentDirections.actionNovelDetailFragmentToReaderFragment(
            novelId = novel.id,
            sourceId = novel.sourceId,
            chapterId = chapter.id,
            chapterNumber = chapter.number,
            novelTitle = novel.title,
            chapterTitle = chapter.title
        )
        findNavController().navigate(action)
    }

    private fun toggleDownloadMode() {
        isInDownloadMode = !isInDownloadMode
        chapterAdapter.setDownloadMode(isInDownloadMode)

        if (isInDownloadMode) {
            binding.layoutDownloadActions.show()
            binding.layoutNormalActions.hide()
        } else {
            binding.layoutDownloadActions.hide()
            binding.layoutNormalActions.show()
        }
    }

    private fun downloadSelectedChapters() {
        val selectedChapters = chapterAdapter.getSelectedForDownload()

        if (selectedChapters.isEmpty()) {
            requireContext().showToast(getString(R.string.no_chapters_selected))
            return
        }

        viewModel.downloadChapters(selectedChapters)

        val message = getString(
            R.string.chapters_queued_for_download,
            selectedChapters.size
        )
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

        // Exit download mode after queuing download
        toggleDownloadMode()
    }

    private fun showMarkAllReadDialog() {
        val options = arrayOf(
            getString(R.string.mark_all_as_read),
            getString(R.string.mark_all_as_unread)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mark_chapters)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> chapterAdapter.markReadUnreadStatus(true)
                    1 -> chapterAdapter.markReadUnreadStatus(false)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showChapterFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_chapter_filter, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.filter_all -> {
                    // Show all chapters
                    updateChapterFilter(ChapterFilter.ALL)
                    true
                }
                R.id.filter_unread -> {
                    // Filter unread chapters
                    updateChapterFilter(ChapterFilter.UNREAD)
                    true
                }
                R.id.filter_read -> {
                    // Filter read chapters
                    updateChapterFilter(ChapterFilter.READ)
                    true
                }
                R.id.filter_bookmarked -> {
                    // Filter bookmarked chapters
                    updateChapterFilter(ChapterFilter.BOOKMARKED)
                    true
                }
                R.id.filter_downloaded -> {
                    // Filter downloaded chapters
                    updateChapterFilter(ChapterFilter.DOWNLOADED)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showChapterSortMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_chapter_sort, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_number_asc -> {
                    // Sort by chapter number ascending
                    updateChapterSort(ChapterSort.NUMBER_ASC)
                    true
                }
                R.id.sort_number_desc -> {
                    // Sort by chapter number descending
                    updateChapterSort(ChapterSort.NUMBER_DESC)
                    true
                }
                R.id.sort_date_asc -> {
                    // Sort by date ascending
                    updateChapterSort(ChapterSort.DATE_ASC)
                    true
                }
                R.id.sort_date_desc -> {
                    // Sort by date descending
                    updateChapterSort(ChapterSort.DATE_DESC)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun updateChapterFilter(filter: ChapterFilter) {
        val chapters = viewModel.chapterList.value ?: return

        val filteredChapters = when (filter) {
            ChapterFilter.ALL -> chapters
            ChapterFilter.READ -> chapters.filter { it.isRead }
            ChapterFilter.UNREAD -> chapters.filter { !it.isRead }
            ChapterFilter.BOOKMARKED -> chapters.filter { it.isBookmarked }
            ChapterFilter.DOWNLOADED -> chapters.filter { it.isDownloaded }
        }

        chapterAdapter.submitList(filteredChapters)
        binding.txtChapterCount.text = getString(
            R.string.chapter_count_filtered,
            filteredChapters.size,
            chapters.size
        )

        // Update filter button text
        binding.btnFilter.text = when (filter) {
            ChapterFilter.ALL -> getString(R.string.filter_all)
            ChapterFilter.READ -> getString(R.string.filter_read)
            ChapterFilter.UNREAD -> getString(R.string.filter_unread)
            ChapterFilter.BOOKMARKED -> getString(R.string.filter_bookmarked)
            ChapterFilter.DOWNLOADED -> getString(R.string.filter_downloaded)
        }
    }

    private fun updateChapterSort(sort: ChapterSort) {
        val chapters = viewModel.chapterList.value ?: return

        val sortedChapters = when (sort) {
            ChapterSort.NUMBER_ASC -> chapters.sortedBy { it.number }
            ChapterSort.NUMBER_DESC -> chapters.sortedByDescending { it.number }
            ChapterSort.DATE_ASC -> chapters.sortedBy { it.dateUpload }
            ChapterSort.DATE_DESC -> chapters.sortedByDescending { it.dateUpload }
        }

        chapterAdapter.submitList(sortedChapters)

        // Update sort button text
        binding.btnSort.text = when (sort) {
            ChapterSort.NUMBER_ASC -> getString(R.string.sort_number_asc)
            ChapterSort.NUMBER_DESC -> getString(R.string.sort_number_desc)
            ChapterSort.DATE_ASC -> getString(R.string.sort_date_asc)
            ChapterSort.DATE_DESC -> getString(R.string.sort_date_desc)
        }
    }

    private fun shareNovel() {
        val novel = viewModel.novelDetail.value ?: return

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, novel.title)
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Check out this novel: ${novel.title} by ${novel.getFormattedAuthors()}"
            )
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_novel)))
    }

    private fun openInBrowser() {
        val novel = viewModel.novelDetail.value ?: return

        try {
            val browserIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(novel.sourceId)
            )
            startActivity(browserIntent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening novel in browser")
            binding.root.showSnackbar(getString(R.string.error_opening_in_browser))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isLoading
        binding.progressBarNovel.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // Chapter filter enum
    private enum class ChapterFilter {
        ALL, READ, UNREAD, BOOKMARKED, DOWNLOADED
    }

    // Chapter sort enum
    private enum class ChapterSort {
        NUMBER_ASC, NUMBER_DESC, DATE_ASC, DATE_DESC
    }

    // ChapterAdapter click listener implementations
    override fun onChapterClick(chapter: ChapterUiModel) {
        navigateToChapter(chapter)
    }

    override fun onChapterLongClick(chapter: ChapterUiModel, view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_chapter_options, popup.menu)

        // Set menu items based on chapter state
        val menu = popup.menu
        menu.findItem(R.id.action_mark_as_read).isVisible = !chapter.isRead
        menu.findItem(R.id.action_mark_as_unread).isVisible = chapter.isRead
        menu.findItem(R.id.action_bookmark).isVisible = !chapter.isBookmarked
        menu.findItem(R.id.action_remove_bookmark).isVisible = chapter.isBookmarked
        menu.findItem(R.id.action_download).isVisible = !chapter.isDownloaded
        menu.findItem(R.id.action_delete_download).isVisible = chapter.isDownloaded

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_read -> {
                    navigateToChapter(chapter)
                    true
                }
                R.id.action_mark_as_read -> {
                    markChapterAsRead(chapter, true)
                    true
                }
                R.id.action_mark_as_unread -> {
                    markChapterAsRead(chapter, false)
                    true
                }
                R.id.action_bookmark -> {
                    toggleChapterBookmark(chapter, true)
                    true
                }
                R.id.action_remove_bookmark -> {
                    toggleChapterBookmark(chapter, false)
                    true
                }
                R.id.action_download -> {
                    downloadChapter(chapter)
                    true
                }
                R.id.action_delete_download -> {
                    deleteDownloadedChapter(chapter)
                    true
                }
                R.id.action_mark_previous_as_read -> {
                    markPreviousChaptersAsRead(chapter)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onBookmarkClick(chapter: ChapterUiModel) {
        toggleChapterBookmark(chapter, !chapter.isBookmarked)
    }

    override fun onDownloadClick(chapter: ChapterUiModel) {
        if (chapter.isDownloaded) {
            deleteDownloadedChapter(chapter)
        } else {
            downloadChapter(chapter)
        }
    }

    override fun onMarkChaptersReadUnread(chapterIds: List<String>, read: Boolean) {
        viewModel.markChaptersReadUnread(chapterIds, read)

        val message = if (read) {
            getString(R.string.chapters_marked_as_read, chapterIds.size)
        } else {
            getString(R.string.chapters_marked_as_unread, chapterIds.size)
        }
        requireContext().showToast(message)
    }

    private fun markChapterAsRead(chapter: ChapterUiModel, read: Boolean) {
        viewModel.markChaptersReadUnread(listOf(chapter.id), read)

        val message = if (read) {
            getString(R.string.chapter_marked_as_read)
        } else {
            getString(R.string.chapter_marked_as_unread)
        }
        requireContext().showToast(message)
    }

    private fun toggleChapterBookmark(chapter: ChapterUiModel, bookmark: Boolean) {
        viewModel.toggleChapterBookmark(chapter.id, bookmark)

        val message = if (bookmark) {
            getString(R.string.chapter_bookmarked)
        } else {
            getString(R.string.chapter_bookmark_removed)
        }
        requireContext().showToast(message)
    }

    private fun downloadChapter(chapter: ChapterUiModel) {
        viewModel.downloadChapters(listOf(chapter))
        requireContext().showToast(getString(R.string.chapter_queued_for_download))
    }

    private fun deleteDownloadedChapter(chapter: ChapterUiModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_downloaded_chapter)
            .setMessage(getString(R.string.delete_downloaded_chapter_confirm))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteDownloadedChapter(chapter.id)
                requireContext().showToast(getString(R.string.chapter_download_deleted))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun markPreviousChaptersAsRead(chapter: ChapterUiModel) {
        val chapters = viewModel.chapterList.value ?: return
        val previousChapters = chapters
            .filter { it.number <= chapter.number }
            .map { it.id }

        if (previousChapters.isNotEmpty()) {
            viewModel.markChaptersReadUnread(previousChapters, true)

            requireContext().showToast(getString(
                R.string.chapters_marked_as_read,
                previousChapters.size
            ))
        }
    }

    // NovelAdapter click listener implementations
    override fun onNovelClick(novel: NovelUiModel) {
        val action = NovelDetailFragmentDirections.actionNovelDetailFragmentSelf(
            novelId = novel.id,
            sourceId = novel.sourceId,
            title = novel.title
        )
        findNavController().navigate(action)
    }

    override fun onAddToLibraryClick(novel: NovelUiModel) {
        viewModel.addToLibrary(novel.id, novel.sourceId)
        requireContext().showToast(getString(R.string.added_to_library))
    }

    override fun onMoreOptionsClick(novel: NovelUiModel, view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_novel_options, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_details -> {
                    onNovelClick(novel)
                    true
                }
                R.id.action_share -> {
                    shareRelatedNovel(novel)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun shareRelatedNovel(novel: NovelUiModel) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, novel.title)
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Check out this novel: ${novel.title} by ${novel.getFormattedAuthors()}"
            )
        }
        startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_novel)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}