package com.mybenru.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mybenru.app.R
import com.mybenru.app.databinding.ItemSourceBinding
import com.mybenru.app.model.SourceUiModel
import com.mybenru.app.utils.loadImage
import com.mybenru.app.utils.show
import com.mybenru.app.utils.hide
import com.google.android.material.chip.Chip

/**
 * Adapter for displaying novel sources in RecyclerView
 */
class SourceAdapter(
    private val listener: SourceClickListener
) : ListAdapter<SourceUiModel, SourceAdapter.SourceViewHolder>(SourceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val binding = ItemSourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSourceStatus(sourceId: String, isEnabled: Boolean) {
        val index = currentList.indexOfFirst { it.id == sourceId }
        if (index != -1) {
            val updatedSource = currentList[index].copy(isEnabled = isEnabled)
            val mutableList = currentList.toMutableList()
            mutableList[index] = updatedSource
            submitList(mutableList)
        }
    }

    inner class SourceViewHolder(private val binding: ItemSourceBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val source = getItem(position)
                    listener.onSourceClick(source)
                }
            }

            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val source = getItem(position)
                    if (source.isEnabled != isChecked) {
                        listener.onSourceToggle(source, isChecked)
                    }
                }
            }

            binding.btnInfo.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val source = getItem(position)
                    listener.onInfoClick(source)
                }
            }
        }

        fun bind(source: SourceUiModel) {
            binding.apply {
                // Set source name and information
                txtSourceName.text = source.name
                txtLanguage.text = source.getLanguageDisplayName()
                txtVersion.text = source.version

                // Load source icon
                imgSourceIcon.loadImage(
                    url = source.iconUrl,
                    placeholder = R.drawable.ic_source_placeholder,
                    error = R.drawable.ic_source_placeholder,
                    cornerRadius = 4f
                )

                // Set enabled state without triggering listener
                switchEnabled.isChecked = source.isEnabled

                // Show NSFW badge if needed
                if (source.isNsfw) {
                    badgeNsfw.show()
                } else {
                    badgeNsfw.hide()
                }

                // Create feature chips
                chipGroupFeatures.removeAllViews()
                source.getFeatures().forEach { feature ->
                    val chip = LayoutInflater.from(root.context)
                        .inflate(R.layout.item_feature_chip, chipGroupFeatures, false) as Chip
                    chip.text = feature
                    chipGroupFeatures.addView(chip)
                }

                // Set base URL
                txtBaseUrl.text = source.baseUrl

                // Visual styling based on enabled status
                if (source.isEnabled) {
                    root.setBackgroundResource(R.drawable.bg_source_enabled)
                    txtSourceName.alpha = 1.0f
                } else {
                    root.setBackgroundResource(R.drawable.bg_source_disabled)
                    txtSourceName.alpha = 0.6f
                }
            }
        }
    }

    interface SourceClickListener {
        fun onSourceClick(source: SourceUiModel)
        fun onSourceToggle(source: SourceUiModel, isEnabled: Boolean)
        fun onInfoClick(source: SourceUiModel)
    }

    private class SourceDiffCallback : DiffUtil.ItemCallback<SourceUiModel>() {
        override fun areItemsTheSame(oldItem: SourceUiModel, newItem: SourceUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SourceUiModel, newItem: SourceUiModel): Boolean {
            return oldItem == newItem
        }
    }
}