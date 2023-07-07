package com.example.wallpaper.viewmodels

import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessbase.download.DownloadBean
import com.example.businessbase.download.DownloadManager
import com.example.businessbase.utils.toMD5
import com.example.wallpaper.R
import com.example.wallpaper.adapters.vm.MyTestDataViewModel
import com.example.wallpaper.consts.AppConst.BASE_PATH
import com.example.wallpaper.fragments.HomePageFragment
import com.example.wallpaper.fragments.ImagePreviewFragment
import com.example.wallpaper.model.bean.ImagePreviewBean
import com.example.wallpaper.model.bean.ImgPreviewDataListBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val remoteRepository: MyRemoteRepository,
) : ViewModel() {

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
