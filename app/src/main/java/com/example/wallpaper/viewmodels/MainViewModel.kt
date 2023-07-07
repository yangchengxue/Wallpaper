package com.example.wallpaper.viewmodels

import android.graphics.Rect
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.State
import com.example.businessbase.download.DownloadBean
import com.example.businessbase.download.DownloadManager
import com.example.businessbase.utils.CommonUtil.screenWidth
import com.example.businessbase.utils.toMD5
import com.example.wallpaper.R
import com.example.wallpaper.adapters.WallpapersAdapter.Companion.VIEW_TYPE1
import com.example.wallpaper.adapters.vm.MyTestDataViewModel
import com.example.wallpaper.consts.AppConst.BASE_PATH
import com.example.wallpaper.consts.AppConst.TAG
import com.example.wallpaper.consts.AppConst.WALLPAPER_INTERVAL_PROPORTION
import com.example.wallpaper.fragments.HomePageFragment
import com.example.wallpaper.fragments.ImagePreviewFragment
import com.example.wallpaper.model.bean.ImagePreviewBean
import com.example.wallpaper.model.bean.ImgPreviewDataListBean
import com.example.wallpaper.model.bean.WallpapersData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val remoteRepository: MyRemoteRepository,
) : ViewModel() {

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
        viewModelScope.launch {
            delay(2000)
            val data = ArrayList<WallpapersData>()
            for (i in 0..60) {
                when (i % 4) {
                    0 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE1,
                                "https://images.pexels.com/photos/3717270/pexels-photo-3717270.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }

                    1 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE1,
                                "https://static-cse.canva.cn/blob/324660/739w-UBC9Exu0RD0.jpg",
                                "标题${i}"
                            )
                        )
                    }

                    2 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE1,
                                "https://images.pexels.com/photos/3565742/pexels-photo-3565742.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                                "标题${i}"
                            )
                        )
                    }

                    3 -> {
                        data.add(
                            WallpapersData(
                                VIEW_TYPE1,
                                "https://images.pexels.com/photos/17314099/pexels-photo-17314099.jpeg?auto=compress&cs=tinysrgb&w=1600",
                                "标题${i}"
                            )
                        )
                    }
                }
            }
            _listData.value = data
        }
    }

    fun enterHomePageFm(fragmentActivity: FragmentActivity) {
        fragmentActivity.supportFragmentManager.commit {
            add(R.id.fragmentContainerView, HomePageFragment.newInstance())
        }
    }

    fun popBackStackFragment(fragment: Fragment) {
        fragment.requireActivity().supportFragmentManager.popBackStack()
    }

    fun enterPhotoPagePreviewFm(fragmentActivity: FragmentActivity, vm: MyTestDataViewModel?) {
        fragmentActivity.supportFragmentManager.commit {
            add(R.id.fragmentContainerView, ImagePreviewFragment.newInstance(vm?.str ?: ""))
            addToBackStack(null)
        }
    }

    fun loadBookUnitPagesPreviewData(imgUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data: MutableList<ImagePreviewBean> = ArrayList()
            data.add(ImagePreviewBean(imgUrl))
            EventBus.getDefault().post(ImgPreviewDataListBean(data))
        }
    }

    fun downloadImg(url: String) {
        val folderPath = Environment.getExternalStorageDirectory().absolutePath.plus("/$BASE_PATH")
        val fileName = "${url.toMD5()}.png"
        DownloadManager.instance.start(DownloadBean(folderPath, fileName, url))
    }
}
