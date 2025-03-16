package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.R
import com.mybenru.app.databinding.ItemCategoryBinding
import com.mybenru.app.model.CategoryUiModel
import com.mybenru.app.utils.loadImage

/**
 * Adapter for displaying categories in RecyclerView
 * (Merged version of VERSI 1 and VERSI 2)
 */
class CategoryAdapter(
    private val listener: CategoryClickListener
) : ListAdapter<CategoryUiModel, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        val isSelected = category.id == selectedCategoryId
        holder.bind(category, isSelected)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    fun setSelectedCategory(categoryId: String) {
        val oldSelectedId = selectedCategoryId
        selectedCategoryId = categoryId

        if (oldSelectedId != null) {
            val oldPosition = currentList.indexOfFirst { it.id == oldSelectedId }
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition)
            }
        }

        val newPosition = currentList.indexOfFirst { it.id == categoryId }
        if (newPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(newPosition)
        }
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = getItem(position)
                    listener.onCategoryClick(category)
                    setSelectedCategory(category.id)
                }
            }
        }

        fun bind(category: CategoryUiModel, isSelected: Boolean) {
            binding.apply {
                txtCategoryTitle.text = category.name
                txtNovelCount.text = itemView.context.getString(
                    R.string.category_novel_count,
                    category.novelCount
                )
                imgCategory.loadImage(
                    url = category.coverUrl,
                    cornerRadius = 8f,
                    placeholder = R.drawable.placeholder_category
                )
                if (isSelected) {
                    root.setBackgroundResource(R.drawable.bg_category_selected)
                    txtCategoryTitle.setTextColor(root.context.getColor(R.color.colorAccent))
                } else {
                    root.setBackgroundResource(R.drawable.bg_category_normal)
                    txtCategoryTitle.setTextColor(root.context.getColor(R.color.textPrimary))
                }
            }
        }
    }

    interface CategoryClickListener {
        fun onCategoryClick(category: CategoryUiModel)
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryUiModel>() {
        override fun areItemsTheSame(oldItem: CategoryUiModel, newItem: CategoryUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryUiModel, newItem: CategoryUiModel): Boolean {
            return oldItem == newItem
        }
    }
}