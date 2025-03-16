package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.R
import com.mybenru.app.databinding.ItemFilterChipBinding
import com.mybenru.app.model.LibraryFilterUiModel

/**
 * Adapter for displaying library filters as chips in RecyclerView
 */
class LibraryFilterAdapter(
    private val listener: FilterClickListener
) : RecyclerView.Adapter<LibraryFilterAdapter.FilterViewHolder>() {

    private val filters = mutableListOf<LibraryFilterUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount(): Int = filters.size

    fun submitList(newFilters: List<LibraryFilterUiModel>) {
        filters.clear()
        filters.addAll(newFilters)
        notifyDataSetChanged()
    }

    fun updateFilter(filterType: LibraryFilterUiModel.FilterType, isActive: Boolean) {
        val index = filters.indexOfFirst { it.filterType == filterType }
        if (index != -1) {
            val updatedFilter = filters[index].copy(isActive = isActive)
            filters[index] = updatedFilter
            notifyItemChanged(index)
        }
    }

    inner class FilterViewHolder(private val binding: ItemFilterChipBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.chipFilter.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val filter = filters[position]
                    val newState = !filter.isActive
                    listener.onFilterClick(filter.filterType, newState)
                }
            }
        }

        fun bind(filter: LibraryFilterUiModel) {
            binding.apply {
                chipFilter.text = filter.getDisplayName()
                chipFilter.isChecked = filter.isActive

                // Set different visual styling based on filter type
                when (filter.filterType) {
                    LibraryFilterUiModel.FilterType.ALL -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_all)
                    }
                    LibraryFilterUiModel.FilterType.UNREAD -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_unread)
                    }
                    LibraryFilterUiModel.FilterType.COMPLETED -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_completed)
                    }
                    LibraryFilterUiModel.FilterType.DOWNLOADED -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_downloaded)
                    }
                    LibraryFilterUiModel.FilterType.BY_AUTHOR -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_author)
                    }
                    LibraryFilterUiModel.FilterType.BY_GENRE -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_genre)
                    }
                    LibraryFilterUiModel.FilterType.BY_STATUS -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_status)
                    }
                    LibraryFilterUiModel.FilterType.RECENTLY_READ -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_recent)
                    }
                    LibraryFilterUiModel.FilterType.RECENTLY_ADDED -> {
                        chipFilter.setChipIconResource(R.drawable.ic_filter_added)
                    }
                }
            }
        }
    }

    interface FilterClickListener {
        fun onFilterClick(filterType: LibraryFilterUiModel.FilterType, isActive: Boolean)
    }
}