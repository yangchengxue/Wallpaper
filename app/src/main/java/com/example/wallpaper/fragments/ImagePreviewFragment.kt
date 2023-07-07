package com.example.wallpaper.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.businessbase.dialogs.PrimitiveDialog
import com.example.businessbase.ui.BaseFragment
import com.example.wallpaper.ImagePreviewLayoutManager
import com.example.wallpaper.ImagePreviewListener
import com.example.wallpaper.R
import com.example.wallpaper.adapters.ImagePreviewAdapter
import com.example.wallpaper.consts.AppConst.ARG_IMAGE_URL
import com.example.wallpaper.databinding.WpFramentImagePreviewBinding
import com.example.wallpaper.model.bean.ImgPreviewDataListBean
import com.example.wallpaper.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class ImagePreviewFragment :
    BaseFragment<WpFramentImagePreviewBinding>(R.layout.wp_frament_image_preview) {

    private val mViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var mImagePreviewAdapter: ImagePreviewAdapter

    @Inject
    lateinit var mLayoutManager: ImagePreviewLayoutManager

    private var mImgUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        initData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.fragment = this
        mBinding.viewModel = mViewModel
        mBinding.imagePreviewRv.layoutManager = mLayoutManager
        mBinding.imagePreviewRv.adapter = mImagePreviewAdapter
        mLayoutManager.setOnViewPagerListener(object : ImagePreviewListener {
            override fun onPageSelected(position: Int) {

            }
        })
        mBinding.controlRL.setOnClickListener {
            PrimitiveDialog.showDialog(childFragmentManager) {
                mViewModel.downloadImg(mImgUrl)
            }

        }
    }

    private fun initData() {
        arguments?.getString(ARG_IMAGE_URL)?.let {
            mImgUrl = it
            mViewModel.loadBookUnitPagesPreviewData(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceivedPreviewData(bean: ImgPreviewDataListBean) {
        if (bean.list.isEmpty()) {
            mViewModel.popBackStackFragment(this)
        }
        mImagePreviewAdapter.submitList(bean.list)
    }

    companion object {
        fun newInstance(imgUrl: String) = ImagePreviewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_IMAGE_URL, imgUrl)
            }
        }
    }

}
