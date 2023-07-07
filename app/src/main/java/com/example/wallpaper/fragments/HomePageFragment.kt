package com.example.wallpaper.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.businessbase.ui.BaseFragment
import com.example.businessbase.utils.CommonUtil.screenWidth
import com.example.wallpaper.R
import com.example.wallpaper.adapters.ScreenSlidePagerAdapter
import com.example.wallpaper.adapters.anim.DepthPageTransformer
import com.example.wallpaper.adapters.anim.ZoomOutPageTransformer
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_PROPORTION
import com.example.wallpaper.databinding.WpFragmentHomePageBinding
import com.example.wallpaper.model.BaseData
import com.example.wallpaper.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePageFragment :
    BaseFragment<WpFragmentHomePageBinding>(R.layout.wp_fragment_home_page) {

    private val mViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.viewPager.adapter = ScreenSlidePagerAdapter(requireActivity())
        val interval = (screenWidth * WALLPAPER_INTERVAL_PROPORTION).toInt()
        val params = mBinding.tabLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = interval * 3
        params.marginEnd = interval * 3
        mBinding.tabLayout.layoutParams = params
        mBinding.tabLayout.setViewPager2(mBinding.viewPager, BaseData.WALLPAPER_STYLE_LIST)
        mBinding.viewPager.setPageTransformer(DepthPageTransformer())

    }

    companion object {
        fun newInstance() = HomePageFragment()
    }
}
