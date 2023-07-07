package com.example.wallpaper.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.businessbase.dialogs.PrimitiveDialog
import com.example.businessbase.ui.BaseFragment
import com.example.businessbase.utils.ToastUtils
import com.example.wallpaper.ImagePreviewLayoutManager
import com.example.wallpaper.ImagePreviewListener
import com.example.wallpaper.R
import com.example.wallpaper.adapters.ImagePreviewAdapter
import com.example.wallpaper.consts.AppConst.ARG_IMAGE_URL
import com.example.wallpaper.consts.AppConst.TAG
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i(TAG, "权限已被授予")
            PrimitiveDialog.showDialog(childFragmentManager) {
                mViewModel.downloadImg(mImgUrl)
            }
        } else {
            ToastUtils.showToast(requireContext(), "存储权限已被拒绝，请到设置中开启")
            Log.i(TAG, "权限已被拒绝")
        }
    }

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
            checkPermission()
        }
    }

    private fun initData() {
        arguments?.getString(ARG_IMAGE_URL)?.let {
            mImgUrl = it
            mViewModel.loadBookUnitPagesPreviewData(it)
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "权限已授予，开始下载图片")
                PrimitiveDialog.showDialog(childFragmentManager) {
                    mViewModel.downloadImg(mImgUrl)
                }
            }

            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Log.i(TAG, "继续申请权限")
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            else -> {
                Log.i(TAG, "申请权限")
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
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
