package com.example.wallpaper.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.arialyy.aria.core.Aria
import com.example.businessbase.ui.BaseActivity
import com.example.wallpaper.R
import com.example.wallpaper.databinding.WpActivityMainBinding
import com.example.wallpaper.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity<WpActivityMainBinding>(R.layout.wp_activity_main) {
    private val mViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Aria.download(this).register();
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        mViewModel.enterHomePageFm(this)
    }

}