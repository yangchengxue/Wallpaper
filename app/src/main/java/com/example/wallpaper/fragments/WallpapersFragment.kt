package com.example.wallpaper.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.example.businessbase.ui.BaseFragment
import com.example.businessbase.utils.CommonUtil.screenWidth
import com.example.wallpaper.R
import com.example.wallpaper.adapters.WallpapersAdapter
import com.example.wallpaper.adapters.WallpapersAdapter.Companion.VIEW_TYPE_ADVERTISE
import com.example.wallpaper.adapters.WallpapersAdapter.Companion.VIEW_TYPE_WALLPAPER
import com.example.wallpaper.consts.AppConst.DataLoadState.FAIL
import com.example.wallpaper.consts.AppConst.DataLoadState.LOADING
import com.example.wallpaper.consts.AppConst.DataLoadState.NO_CONTENT
import com.example.wallpaper.consts.AppConst.DataLoadState.SUCCESS
import com.example.wallpaper.consts.AppConst.TAG
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_PROPORTION
import com.example.wallpaper.consts.AppConst.WALLPAPER_SPAN_COUNT
import com.example.wallpaper.databinding.WpFragmentWallpapersBinding
import com.example.wallpaper.viewmodels.WallpapersFmViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WallpapersFragment :
    BaseFragment<WpFragmentWallpapersBinding>(R.layout.wp_fragment_wallpapers) {

    private val mViewModel: WallpapersFmViewModel by viewModels()

    @Inject
    lateinit var mListAdapter: WallpapersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.rv.apply {
            val interval = (screenWidth * WALLPAPER_INTERVAL_PROPORTION).toInt()
            setPadding(interval, 0, interval, 0)
            Log.i(TAG, "rv setPadding， interval=$interval")
            val gridLayoutManager = GridLayoutManager(requireContext(), WALLPAPER_SPAN_COUNT)
            layoutManager = gridLayoutManager
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    var spanSize = 1
                    // 显示列数 = spanCount / spanSize。壁纸类型显示为 3 列，广告类型显示为 1 列
                    spanSize = when (mListAdapter.getItemViewType(position)) {
                        VIEW_TYPE_WALLPAPER -> { 1 }
                        VIEW_TYPE_ADVERTISE -> { 3 }
                        else -> {1}
                    }
                    return spanSize
                }
            }
            addItemDecoration(
                mViewModel.getSubjectRvItemDecoration()
            )
            mListAdapter.clickItemListener = {
                mViewModel.enterPhotoPagePreviewFm(requireActivity(), it)
            }
            adapter = mListAdapter
        }
        subscribeUi(mListAdapter)
        mViewModel.getOldData()
    }

    private fun subscribeUi(adapter: WallpapersAdapter) {
        mViewModel.loadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LOADING -> {
                    mBinding.loadingView.visibility = View.VISIBLE
                }

                SUCCESS -> {
                    mBinding.rv.visibility = View.VISIBLE
                    mBinding.loadingView.visibility = View.GONE
                }

                NO_CONTENT -> {
                    mBinding.loadingView.visibility = View.GONE
                }

                FAIL -> {
                    mBinding.loadingView.visibility = View.GONE
                }
            }
        }
        mViewModel.listData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    companion object {
        fun newInstance() = WallpapersFragment()
    }
}
