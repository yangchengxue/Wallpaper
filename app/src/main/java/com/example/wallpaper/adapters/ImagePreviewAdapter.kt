package com.example.wallpaper.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpaper.R
import com.example.wallpaper.model.bean.ImagePreviewBean
import com.example.wallpaper.adapters.diff.ImagePreviewDiffItemCallBack
import com.example.wallpaper.adapters.vm.ImagePreviewViewModel
import com.example.wallpaper.databinding.WpItemImagePreviewBinding
import javax.inject.Inject

class ImagePreviewAdapter @Inject constructor() :
    ListAdapter<ImagePreviewBean, ImagePreviewAdapter.AbstractViewHolder>(
        ImagePreviewDiffItemCallBack()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return ContentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.wp_item_image_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    abstract class AbstractViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        abstract fun bind(data: ImagePreviewBean)
    }

    class ContentViewHolder(
        private val binding: WpItemImagePreviewBinding
    ) : AbstractViewHolder(binding.root) {

        override fun bind(data: ImagePreviewBean) {
            with(binding) {
                if (viewModel == null) {
                    viewModel = ImagePreviewViewModel()
                }
                viewModel?.showImage(photoView, data.url)
                executePendingBindings()
            }
        }
    }

}


