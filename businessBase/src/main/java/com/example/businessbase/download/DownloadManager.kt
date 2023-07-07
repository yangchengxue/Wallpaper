package com.example.businessbase.download

import android.util.Log
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadTask
import com.example.businessbase.R
import com.example.businessbase.utils.CommonUtil.getAppContext
import com.example.businessbase.utils.ToastUtils.showToast
import java.io.File

class DownloadManager private constructor() : IDownload {

    private var mTaskId: Long = -1
    private var mIsDownloading = false
    private var mDownloadingBean: DownloadBean? = null

    init {
        Aria.init(getAppContext())
        init()
    }

    override fun init() {
        Aria.download(this).register()
    }

    override fun release() {
        Aria.download(this).unRegister()
    }

    override fun start(download: DownloadBean): Long {
        if (mIsDownloading.not()) {
            val downloadFile = File(download.folderPath)
            if (!downloadFile.exists()) {
                downloadFile.mkdirs()
                Log.i(TAG, "文件夹不存在，创建文件夹: ${downloadFile.absolutePath}")
            }
            val filePath = download.folderPath + File.separator + download.fileName
            mDownloadingBean = download
            mIsDownloading = true
            mTaskId = Aria.download(this)
                .load(download.downloadUrl)
                .setFilePath(filePath)
                .resetState()
                .create()
        }
        return mTaskId
    }

    override fun delete() {
        Aria.download(this).load(mTaskId).cancel()
    }

    @Download.onPre
    fun onPre(task: DownloadTask?) {
        Log.i(TAG, "onPre")
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask?) {
        Log.i(TAG, "onStart")
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask?) {
        Log.i(TAG, "resume")
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask?) {
        Log.i(TAG, "stop")
        mIsDownloading = false
        mDownloadingBean?.let {
            DownloadExtUtils.deleteDownloadPartFile(listOf(it))
            delete()
        }
        mDownloadingBean = null
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask?) {
        Log.i(TAG, "cancel")
        mIsDownloading = false
        mDownloadingBean?.let {
            DownloadExtUtils.deleteDownloadPartFile(listOf(it))
            delete()
        }
        mDownloadingBean = null
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        showToast(getAppContext(), getAppContext().getString(R.string.base_str_download_fail))
        Log.i(TAG, "fail")
        mIsDownloading = false
        mDownloadingBean?.let {
            DownloadExtUtils.deleteDownloadPartFile(listOf(it))
            delete()
        }
        mDownloadingBean = null
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        showToast(getAppContext(), getAppContext().getString(R.string.base_str_download_success))
        Log.i(TAG, "taskComplete path=${task.downloadEntity.filePath}")
        mIsDownloading = false
        mDownloadingBean = null
    }

    companion object {
        const val TAG = "DownloadManager"
        val instance: IDownload by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadManager()
        }
    }
}