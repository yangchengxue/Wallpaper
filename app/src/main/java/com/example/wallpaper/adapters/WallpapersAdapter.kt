package com.example.wallpaper.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.businessbase.utils.CommonUtil.screenRatio
import com.example.businessbase.utils.CommonUtil.screenWidth
import com.example.businessbase.utils.px2dp
import com.example.wallpaper.R
import com.example.wallpaper.adapters.vm.MyTestDataViewModel
import com.example.wallpaper.consts.AppConst.TAG
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_PROPORTION
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_SPAN_COUNT
import com.example.wallpaper.consts.AppConst.WALLPAPER_SPAN_COUNT
import com.example.wallpaper.databinding.WpItemWallpaperBinding
import com.example.wallpaper.databinding.WpItemWallpaperTempBinding
import com.example.wallpaper.model.bean.WallpapersData
import javax.inject.Inject


class WallpapersAdapter @Inject constructor() :
    ListAdapter<WallpapersData, WallpapersAdapter.AbstractViewHolder>(DiffItemCallBack()) {

    var clickItemListener: ((vm: MyTestDataViewModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        return when (viewType) {
            VIEW_TYPE1 -> {
                TitleViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.wp_item_wallpaper,
                        parent,
                        false
                    ), clickItemListener
                )
            }

            VIEW_TYPE2 -> {
                ContentViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.wp_item_wallpaper_temp,
                        parent,
                        false
                    )
                )
            }

            else -> {
                TitleViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.wp_item_wallpaper,
                        parent,
                        false
                    ), clickItemListener
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type

    abstract class AbstractViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        abstract fun bind(data: WallpapersData)
    }

    class TitleViewHolder(
        private val binding: WpItemWallpaperBinding,
        private var clickItemListener: ((vm: MyTestDataViewModel?) -> Unit)?,
    ) : AbstractViewHolder(binding.root) {

        override fun bind(data: WallpapersData) {
            with(binding) {
                viewModel = MyTestDataViewModel(data)
                val imgWidth =
                    (screenWidth - WALLPAPER_INTERVAL_SPAN_COUNT * (screenWidth * WALLPAPER_INTERVAL_PROPORTION)) / WALLPAPER_SPAN_COUNT
                val imgHeight = imgWidth * screenRatio
                val layoutParams = wallpaperIv.layoutParams
                layoutParams.width = imgWidth.toInt()
                layoutParams.height = imgHeight.toInt()
                wallpaperIv.layoutParams = layoutParams

                Glide.with(root.context)
                    .load(data.paperUrl)
                    .override(imgWidth.toInt(), imgHeight.toInt())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // .placeholder(R.drawable.yd_cbc_book_cover_place_holder)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(px2dp(root.context, 10F))
                        )
                    )
                    .into(wallpaperIv)

                Log.i(TAG, "imgWidth=${imgWidth.toInt()} imgHeight=${imgHeight.toInt()}")

                setClickListener {
                    clickItemListener?.invoke(viewModel)
                }

                executePendingBindings()
            }
        }
    }

    class ContentViewHolder(
        private val binding: WpItemWallpaperTempBinding,
    ) : AbstractViewHolder(binding.root) {
        init {
            binding.setClickListener {
                // todo binding click
            }
        }

        override fun bind(data: WallpapersData) {
            with(binding) {
                viewModel = MyTestDataViewModel(data)
                executePendingBindings()
            }
        }
    }

    companion object {
        const val VIEW_TYPE1 = 1
        const val VIEW_TYPE2 = 2
    }
}

private class DiffItemCallBack : DiffUtil.ItemCallback<WallpapersData>() {

    /**
     * 判断两个对象是否是相同的 Item，例如，如果你的 Item 有唯一的 id 字段，这个方法就可以判断 id 是否相等
     */
    override fun areItemsTheSame(oldItem: WallpapersData, newItem: WallpapersData): Boolean {
        return oldItem.paperName == newItem.paperName
    }

    /**
     * 检查两个 Item 是否有相同的数据，一般来说，如果 Item 的视觉表现是否相同的，那就可以返回 true，则对应 Item 将不会刷新
     * 注意：该方法仅在 areItemsTheSame() 返回 true 时调用
     */
    override fun areContentsTheSame(oldItem: WallpapersData, newItem: WallpapersData): Boolean {
        return oldItem.type == newItem.type && oldItem.paperUrl == oldItem.paperUrl
    }
}
