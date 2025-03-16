package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.R
import com.mybenru.app.databinding.ItemChapterBinding
import com.mybenru.app.model.ChapterUiModel
import com.mybenru.app.utils.DateUtils

/**
 * Adapter for displaying chapter items in a RecyclerView
 */
class ChapterAdapter(
    private val listener: ChapterClickListener
) : ListAdapter<ChapterUiModel, ChapterAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    private var selectMode = false
    private val selectedChapters = mutableSetOf<String>()

    // Whether to show download buttons and other UI elements
    private var showDownloadControls = false
    private var showReadingProgress = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = getItem(position)
        holder.bind(
            chapter,
            isSelected = selectedChapters.contains(chapter.id),
            selectMode = selectMode,
            showDownloadControls = showDownloadControls,
            showReadingProgress = showReadingProgress
        )
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    /**
     * Enter selection mode
     */
    fun enterSelectMode() {
        selectMode = true
        selectedChapters.clear()
        notifyDataSetChanged()
    }

    /**
     * Exit selection mode
     */
    fun exitSelectMode() {
        selectMode = false
        selectedChapters.clear()
        notifyDataSetChanged()
    }

    /**
     * Set whether to show download controls
     */
    fun setShowDownloadControls(show: Boolean) {
        showDownloadControls = show
        notifyDataSetChanged()
    }

    /**
     * Set whether to show reading progress
     */
    fun setShowReadingProgress(show: Boolean) {
        showReadingProgress = show
        notifyDataSetChanged()
    }

    /**
     * Toggle selection of a chapter
     */
    fun toggleSelection(chapterId: String) {
        if (selectedChapters.contains(chapterId)) {
            selectedChapters.remove(chapterId)
        } else {
            selectedChapters.add(chapterId)
        }

        val index = currentList.indexOfFirst { it.id == chapterId }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    /**
     * Select all chapters
     */
    fun selectAll() {
        currentList.forEach { chapter ->
            selectedChapters.add(chapter.id)
        }
        notifyDataSetChanged()
    }

    /**
     * Clear all selections
     */
    fun clearSelection() {
        selectedChapters.clear()
        notifyDataSetChanged()
    }

    /**
     * Get all selected chapter IDs
     */
    fun getSelectedChapterIds(): List<String> {
        return selectedChapters.toList()
    }

    /**
     * Get count of selected chapters
     */
    fun getSelectedCount(): Int {
        return selectedChapters.size
    }

    /**
     * Is the adapter in selection mode?
     */
    fun isInSelectMode(): Boolean {
        return selectMode
    }

    /**
     * ViewHolder for chapter items
     */
    class ChapterViewHolder(
        private val binding: ItemChapterBinding,
        private val listener: ChapterClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            chapter: ChapterUiModel,
            isSelected: Boolean,
            selectMode: Boolean,
            showDownloadControls: Boolean,
            showReadingProgress: Boolean
        ) {
            // Set chapter number and title
            binding.txtChapterNumber.text = if (chapter.title.isEmpty()) {
                chapter.number.toString()
            } else {
                "Chapter ${chapter.number}"
            }

            binding.txtChapterTitle.text = chapter.title

            // Format and set upload date
            val uploadDate = DateUtils.formatDate(chapter.uploadDate)
            binding.txtUploadDate.text = uploadDate

            // Set reading time if available
            if (chapter.wordCount > 0) {
                val readingMinutes = DateUtils.calculateReadingTimeMinutes(chapter.wordCount)
                binding.txtReadingTime.text = "$readingMinutes min"
                binding.txtReadingTime.visibility = View.VISIBLE
            } else {
                binding.txtReadingTime.visibility = View.GONE
            }

            // Set reading progress if applicable
            binding.txtReadingProgress.visibility = if (chapter.readingProgress > 0f && showReadingProgress) {
                binding.txtReadingProgress.text = "${(chapter.readingProgress * 100).toInt()}%"
                View.VISIBLE
            } else {
                View.GONE
            }

            // Set "New" badge if applicable
            val isNew = chapter.uploadDate > System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days
            binding.cardBadge.isVisible = isNew && !chapter.isRead
            if (isNew && !chapter.isRead) {
                binding.txtBadge.text = "New"
            }

            // Set chapter text color based on read status
            val textColor = if (chapter.isRead) {
                ContextCompat.getColor(itemView.context, R.color.colorOnSurface)
            } else {
                ContextCompat.getColor(itemView.context, R.color.colorPrimary)
            }
            binding.txtChapterNumber.setTextColor(textColor)

            // Show/hide download button
            binding.btnDownload.visibility = if (showDownloadControls && !chapter.isDownloaded) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Show/hide bookmark button
            binding.btnBookmark.visibility = View.VISIBLE

            // Set download button icon
            val downloadIcon = if (chapter.isDownloaded) {
                R.drawable.ic_downloaded
            } else {
                R.drawable.ic_download
            }
            binding.btnDownload.setImageResource(downloadIcon)

            // Set bookmark button icon
            val bookmarkIcon = if (chapter.isBookmarked) {
                R.drawable.ic_bookmark_filled
            } else {
                R.drawable.ic_bookmark_outline
            }
            binding.btnBookmark.setImageResource(bookmarkIcon)

            // Handle selection mode
            binding.checkboxDownload.isVisible = selectMode
            binding.checkboxDownload.isChecked = isSelected

            // Set click listeners
            binding.root.setOnClickListener {
                if (selectMode) {
                    listener.onChapterSelectionToggled(chapter)
                } else {
                    listener.onChapterClicked(chapter)
                }
            }

            binding.root.setOnLongClickListener {
                return@setOnLongClickListener listener.onChapterLongClicked(chapter)
            }

            binding.checkboxDownload.setOnClickListener {
                listener.onChapterSelectionToggled(chapter)
            }

            binding.btnBookmark.setOnClickListener {
                listener.onChapterBookmarkClicked(chapter)
            }

            binding.btnDownload.setOnClickListener {
                listener.onChapterDownloadClicked(chapter)
            }
        }
    }

    /**
     * DiffUtil callback for chapter items
     */
    class ChapterDiffCallback : DiffUtil.ItemCallback<ChapterUiModel>() {
        override fun areItemsTheSame(oldItem: ChapterUiModel, newItem: ChapterUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChapterUiModel, newItem: ChapterUiModel): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Interface for chapter interactions
     */
    interface ChapterClickListener {
        fun onChapterClicked(chapter: ChapterUiModel)
        fun onChapterLongClicked(chapter: ChapterUiModel): Boolean
        fun onChapterSelectionToggled(chapter: ChapterUiModel)
        fun onChapterBookmarkClicked(chapter: ChapterUiModel)
        fun onChapterDownloadClicked(chapter: ChapterUiModel)
    }
}