package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.databinding.ItemFilterChipBinding
import com.mybenru.app.model.LibraryFilterUiModel

/**
 * Adapter for displaying filter chips in a RecyclerView
 */
class FilterAdapter(
    private val listener: FilterClickListener
) : ListAdapter<LibraryFilterUiModel, FilterAdapter.FilterViewHolder>(FilterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    /**
     * ViewHolder for filter chips
     */
    class FilterViewHolder(
        private val binding: ItemFilterChipBinding,
        private val listener: FilterClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(filter: LibraryFilterUiModel) {
            binding.filterChip.apply {
                text = filter.getDisplayName()
                isChecked = filter.isActive

                setOnClickListener {
                    listener.onFilterClicked(filter)
                }
            }
        }
    }

    /**
     * DiffUtil callback for filter items
     */
    class FilterDiffCallback : DiffUtil.ItemCallback<LibraryFilterUiModel>() {
        override fun areItemsTheSame(oldItem: LibraryFilterUiModel, newItem: LibraryFilterUiModel): Boolean {
            return oldItem.filterType == newItem.filterType
        }

        override fun areContentsTheSame(oldItem: LibraryFilterUiModel, newItem: LibraryFilterUiModel): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Interface for filter click events
     */
    interface FilterClickListener {
        fun onFilterClicked(filter: LibraryFilterUiModel)
    }
}