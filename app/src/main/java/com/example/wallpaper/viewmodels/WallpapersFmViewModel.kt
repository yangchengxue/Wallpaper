package com.example.wallpaper.viewmodels

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.State
import com.example.businessbase.utils.CommonUtil.screenWidth
import com.example.wallpaper.R
import com.example.wallpaper.adapters.WallpapersAdapter.Companion.VIEW_TYPE_ADVERTISE
import com.example.wallpaper.adapters.WallpapersAdapter.Companion.VIEW_TYPE_WALLPAPER
import com.example.wallpaper.adapters.vm.MyTestDataViewModel
import com.example.wallpaper.consts.AppConst.DataLoadState.LOADING
import com.example.wallpaper.consts.AppConst.DataLoadState.SUCCESS
import com.example.wallpaper.consts.AppConst.TAG
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_PROPORTION
import com.example.wallpaper.fragments.ImagePreviewFragment
import com.example.wallpaper.model.bean.WallpapersData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpapersFmViewModel @Inject constructor(
    //private val remoteRepository: MyRemoteRepository,
) : ViewModel() {

    private var _loadState = MutableLiveData<String>()
    var loadState: LiveData<String> = _loadState

    private var _listData = MutableLiveData<List<WallpapersData>>()

    var listData: LiveData<List<WallpapersData>> = _listData

    fun getSubjectRvItemDecoration(): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(rect: Rect, v: View, rv: RecyclerView, statse: State) {
                val interval = (screenWidth * WALLPAPER_INTERVAL_PROPORTION).toInt()
                rect.left = interval
                rect.right = interval
                Log.i(TAG, "getItemOffsets interval=$interval")
            }
        }
    }

    fun getOldData() {
        _loadState.value = LOADING
        viewModelScope.launch {
            delay(2000)
            val data = ArrayList<WallpapersData>()
            for (i in 0..60) {
                when (i % 7) {
                    0 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://images.pexels.com/photos/3717270/pexels-photo-3717270.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }

                    1 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://static-cse.canva.cn/blob/324660/739w-UBC9Exu0RD0.jpg",
                                "标题${i}"
                            )
                        )
                    }

                    2 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://images.pexels.com/photos/3565742/pexels-photo-3565742.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                                "标题${i}"
                            )
                        )
                    }

                    3 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://images.pexels.com/photos/17314099/pexels-photo-17314099.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }

                    4 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://images.pexels.com/photos/2953179/pexels-photo-2953179.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }

                    5 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE_WALLPAPER,
                                "https://images.pexels.com/photos/17410648/pexels-photo-17410648.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }

                    6 -> {
                        if (i == 13) {
                            data.add(
                                WallpapersData(
                                    VIEW_TYPE_ADVERTISE,
                                    "https://images.pexels.com/photos/17314099/pexels-photo-17314099.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                    "标题${i}"
                                )
                            )
                        } else {
                            data.add(
                                WallpapersData(
                                    VIEW_TYPE_WALLPAPER,
                                    "https://images.pexels.com/photos/17314099/pexels-photo-17314099.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                    "标题${i}"
                                )
                            )
                        }
                    }
                }
            }
            _loadState.value = SUCCESS
            _listData.value = data
        }
    }

    fun enterPhotoPagePreviewFm(fragmentActivity: FragmentActivity, vm: MyTestDataViewModel?) {
        fragmentActivity.supportFragmentManager.commit {
            add(R.id.fragmentContainerView, ImagePreviewFragment.newInstance(vm?.str ?: ""))
            addToBackStack(null)
        }
    }

}
