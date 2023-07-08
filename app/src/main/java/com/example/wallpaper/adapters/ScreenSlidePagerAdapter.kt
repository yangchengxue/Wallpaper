package com.example.wallpaper.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.wallpaper.fragments.WallpapersFragment
import com.example.wallpaper.model.BaseData.VIEW_PAGER_NUM_PAGES

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = VIEW_PAGER_NUM_PAGES

    override fun createFragment(position: Int): Fragment = WallpapersFragment.newInstance()
}