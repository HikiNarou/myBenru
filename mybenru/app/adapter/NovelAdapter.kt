package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.R
import com.mybenru.app.databinding.ItemNovelGridBinding
import com.mybenru.app.databinding.ItemNovelHorizontalBinding
import com.mybenru.app.model.NovelUiModel
import com.mybenru.app.utils.loadImage

/**
 * Adapter for displaying novels in either grid or horizontal list format
 */
class NovelAdapter(
    private val displayMode: DisplayMode,
    private val listener: NovelClickListener
) : ListAdapter<NovelUiModel, RecyclerView.ViewHolder>(NovelDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return displayMode.ordinal
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DisplayMode.GRID.ordinal -> {
                val binding = ItemNovelGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GridViewHolder(binding, listener)
            }
            else -> {
                val binding = ItemNovelHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HorizontalViewHolder(binding, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val novel = getItem(position)
        when (holder) {
            is GridViewHolder -> holder.bind(novel)
            is HorizontalViewHolder -> holder.bind(novel)
        }
    }

    /**
     * ViewHolder for grid layout
     */
    class GridViewHolder(
        private val binding: ItemNovelGridBinding,
        private val listener: NovelClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(novel: NovelUiModel) {
            binding.txtTitle.text = novel.title
            binding.txtAuthor.text = novel.getFormattedAuthors()

            // Load cover image
            binding.imgCover.loadImage(
                url = novel.coverUrl,
                placeholder = R.drawable.placeholder_cover,
                error = R.drawable.error_cover,
                cornerRadius = 8f
            )

            // Handle reading progress
            val hasReadingProgress = novel.readingProgress > 0f
            binding.progressReading.isVisible = hasReadingProgress
            if (hasReadingProgress) {
                binding.progressReading.progress = novel.getReadingCompletionPercentage()
            }

            // Handle update badge
            val updateCount = novel.getUpdateBadgeText()
            binding.cardBadge.isVisible = updateCount != null
            binding.txtBadge.text = updateCount

            // Set click listeners
            binding.root.setOnClickListener {
                listener.onNovelClicked(novel)
            }

            binding.root.setOnLongClickListener {
                listener.onNovelLongClicked(novel)
            }

            binding.btnMore.setOnClickListener {
                listener.onMoreOptionsClicked(novel, it)
            }
        }
    }

    /**
     * ViewHolder for horizontal layout
     */
    class HorizontalViewHolder(
        private val binding: ItemNovelHorizontalBinding,
        private val listener: NovelClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(novel: NovelUiModel) {
            binding.txtTitle.text = novel.title
            binding.txtAuthor.text = novel.getFormattedAuthors()

            // Load cover image
            binding.imgCover.loadImage(
                url = novel.coverUrl,
                placeholder = R.drawable.placeholder_cover,
                error = R.drawable.error_cover,
                cornerRadius = 8f
            )

            // Handle reading progress
            val hasReadingProgress = novel.readingProgress > 0f
            binding.progressReading.isVisible = hasReadingProgress
            if (hasReadingProgress) {
                binding.progressReading.progress = novel.getReadingCompletionPercentage()
            }

            // Handle update badge
            val updateCount = novel.getUpdateBadgeText()
            binding.cardBadge.isVisible = updateCount != null
            binding.txtBadge.text = updateCount

            // Show "Add to Library" button for non-library novels
            binding.btnAddToLibrary.isVisible = !novel.isInLibrary

            // Set click listeners
            binding.root.setOnClickListener {
                listener.onNovelClicked(novel)
            }

            binding.root.setOnLongClickListener {
                listener.onNovelLongClicked(novel)
            }

            binding.btnAddToLibrary.setOnClickListener {
                listener.onAddToLibraryClicked(novel)
            }
        }
    }

    /**
     * DiffUtil callback for novel items
     */
    class NovelDiffCallback : DiffUtil.ItemCallback<NovelUiModel>() {
        override fun areItemsTheSame(oldItem: NovelUiModel, newItem: NovelUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NovelUiModel, newItem: NovelUiModel): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Display modes for the adapter
     */
    enum class DisplayMode {
        GRID, HORIZONTAL
    }

    /**
     * Interface for novel item interactions
     */
    interface NovelClickListener {
        fun onNovelClicked(novel: NovelUiModel)
        fun onNovelLongClicked(novel: NovelUiModel): Boolean
        fun onAddToLibraryClicked(novel: NovelUiModel)
        fun onMoreOptionsClicked(novel: NovelUiModel, view: View)
    }
}