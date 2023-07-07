package com.example.businessbase.download

import android.net.Uri
import android.util.Log
import com.example.businessbase.download.DownloadManager.Companion.TAG
import java.io.File

object DownloadExtUtils {

    /**
     * 删除下载目录下的 part 文件
     */
    fun deleteDownloadPartFile(downloads: List<DownloadBean>) {
        if (downloads.isEmpty()) {
            return
        }
        val folderPath = File(downloads[0].folderPath)
        folderPath.listFiles()?.let {
            for (file in it) {
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "part") {
                    val deleteSuccess = file.delete()
                    if (deleteSuccess) Log.i(TAG, "delete success ${file.name}")
                }
            }
        }
    }

    /**
     * 删除下载目录下的 .index 文件
     */
    fun deleteDownloadIndexFile(folderPath: String) {
        val folder = File(folderPath)
        var hasIndexFile = false
        folder.listFiles()?.let {
            for (file in it) {
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "index") {
                    hasIndexFile = true
                    break
                }
            }
        }
        Log.i(TAG, "hasIndexFile=$hasIndexFile")
        if (hasIndexFile) {
            deleteFolder(folder)
        }
    }

    fun deleteFolder(folder: File): Boolean {
        var success = true
        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    success = success and deleteFolder(file)
                }
            }
        }
        success = success and folder.delete()
        return success
    }

    fun getFolderByFile(filePath: String) = filePath.substring(0, filePath.lastIndexOf(File.separator)) + File.separator

    fun deleteFile(filePath: String) : Boolean {
        return File(filePath).delete()
    }

    fun checkVideoIsDownloaded(folderPath: String): Boolean {
        var flag = 0
        val folder = File(folderPath)
        folder.listFiles()?.let {
            for (file in it) {
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "index") {
                    return false
                }
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "m3u8_0") {
                    flag++
                }
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "m3u8") {
                    flag++
                }
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "key") {
                    flag++
                }
            }
        }
        return flag == 3
    }

}