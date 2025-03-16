package com.mybenru.app.ui.explore

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.mybenru.app.R
import com.mybenru.app.adapter.CategoryAdapter
import com.mybenru.app.adapter.NovelAdapter
import com.mybenru.app.databinding.FragmentExploreBinding
import com.mybenru.app.extension.observeWithLifecycle
import com.mybenru.app.extension.safeNavigate
import com.mybenru.app.model.CategoryUiModel
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.hide
import com.mybenru.app.utils.show
import com.mybenru.app.utils.showSnackbar
import com.mybenru.app.utils.showToast
import com.mybenru.app.viewmodel.ExploreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment for exploring novels from different sources
 */
@AndroidEntryPoint
class ExploreFragment : Fragment(), NovelAdapter.NovelClickListener, CategoryAdapter.CategoryClickListener {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels()

    private lateinit var novelsAdapter: NovelAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAdapters()
        setupSearchView()
        setupListeners()
        observeViewModel()

        // Load initial data
        viewModel.loadCategories()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.title_explore)

        binding.btnSource.setOnClickListener {
            navigateToSourceList()
        }

        binding.btnSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun setupAdapters() {
        // Novel adapter for category novels and search results
        novelsAdapter = NovelAdapter(NovelAdapter.DisplayMode.GRID, this).apply {
            setHasStableIds(true)
        }

        binding.rvNovels.apply {
            adapter = novelsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }

        binding.rvSearchNovels.apply {
            adapter = novelsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }

        // Category adapter
        categoryAdapter = CategoryAdapter(this).apply {
            setHasStableIds(true)
        }

        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    viewModel.searchNovels(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce typing
                    if (!newText.isNullOrBlank() && newText.length >= 2) {
                        viewModel.searchNovels(newText)
                    }
                }
                return true
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.searchView.setQuery("", false)
            showCategoriesView()
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (binding.layoutSearchResults.isVisible) {
                val query = binding.searchView.query.toString()
                if (query.isNotBlank()) {
                    viewModel.searchNovels(query)
                }
            } else {
                viewModel.loadCategories()
            }
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }

        // Observe categories
        viewModel.categories.observeWithLifecycle(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        // Observe current category
        viewModel.currentCategory.observeWithLifecycle(viewLifecycleOwner) { category ->
            updateCategoryDetails(category)
        }

        // Observe category novels
        viewModel.categoryNovels.observeWithLifecycle(viewLifecycleOwner) { novels ->
            updateCategoryNovels(novels)
        }

        // Observe search results
        viewModel.searchResults.observeWithLifecycle(viewLifecycleOwner) { results ->
            updateSearchResults(results, binding.searchView.query.toString())
        }

        // Observe errors
        viewModel.errorEvent.observeWithLifecycle(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.root.showSnackbar(errorMessage)
                Timber.e("Error in ExploreFragment: $errorMessage")
            }
        }
    }

    private fun handleUiState(state: ExploreViewModel.ExploreUiState) {
        binding.swipeRefreshLayout.isRefreshing = state is ExploreViewModel.ExploreUiState.Loading

        when (state) {
            is ExploreViewModel.ExploreUiState.Loading -> {
                if (binding.layoutSearchResults.isVisible) {
                    binding.progressBarSearch.show()
                } else {
                    binding.progressBarCategories.show()
                    binding.progressBarCategoryNovels.show()
                }
            }
            is ExploreViewModel.ExploreUiState.Success -> {
                binding.progressBarCategories.hide()
                binding.progressBarCategoryNovels.hide()
                binding.progressBarSearch.hide()

                // Success state
            }
            is ExploreViewModel.ExploreUiState.Empty -> {
                binding.progressBarCategories.hide()
                binding.progressBarCategoryNovels.hide()
                binding.progressBarSearch.hide()

                if (binding.layoutSearchResults.isVisible) {
                    binding.layoutNoResults.show()
                } else {
                    binding.layoutEmpty.show()
                }
            }
            is ExploreViewModel.ExploreUiState.Error -> {
                binding.progressBarCategories.hide()
                binding.progressBarCategoryNovels.hide()
                binding.progressBarSearch.hide()
            }
        }
    }

    private fun updateCategoryDetails(category: CategoryUiModel?) {
        if (category != null) {
            binding.txtCategoryTitle.text = category.name
            binding.txtCategoryDescription.text = category.description
                ?: getString(R.string.no_category_description)
        }
    }

    private fun updateCategoryNovels(novels: List<NovelUiModel>) {
        // Set novels to adapter
        novelsAdapter.submitList(novels)

        // Show/hide empty state
        binding.txtNoCategoryNovels.isVisible = novels.isEmpty()
    }

    private fun updateSearchResults(results: List<NovelUiModel>, query: String) {
        // Switch to search results view
        showSearchResultsView()

        // Update search info
        binding.txtSearchQuery.text = "\"$query\""
        binding.txtSearchResultCount.text = resources.getQuantityString(
            R.plurals.search_result_count, results.size, results.size
        )

        // Update adapter
        novelsAdapter.submitList(results)

        // Show/hide no results view
        binding.layoutNoResults.isVisible = results.isEmpty()
    }

    private fun showSearchResultsView() {
        binding.layoutCategories.hide()
        binding.layoutSearchResults.show()
        binding.layoutNoResults.hide()
        binding.layoutEmpty.hide()
    }

    private fun showCategoriesView() {
        binding.layoutCategories.show()
        binding.layoutSearchResults.hide()
        binding.layoutNoResults.hide()
        binding.layoutEmpty.hide()
    }

    private fun navigateToSourceList() {
        val action = ExploreFragmentDirections.actionExploreFragmentToSourceListFragment()
        findNavController().safeNavigate(action)
    }

    private fun navigateToSettings() {
        val action = ExploreFragmentDirections.actionExploreFragmentToSettingsFragment()
        findNavController().safeNavigate(action)
    }

    // CategoryClickListener implementation
    override fun onCategoryClicked(category: CategoryUiModel) {
        viewModel.selectCategory(category)
    }

    // NovelClickListener implementations
    override fun onNovelClicked(novel: NovelUiModel) {
        val action = ExploreFragmentDirections.actionExploreFragmentToNovelDetailFragment(
            novelId = novel.id,
            sourceId = novel.sourceId,
            title = novel.title
        )
        findNavController().safeNavigate(action)
    }

    override fun onNovelLongClicked(novel: NovelUiModel): Boolean {
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
            getString(R.string.share)
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(novel.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onNovelClicked(novel)
                    1 -> toggleLibraryStatus(novel)
                    2 -> shareNovel(novel)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}