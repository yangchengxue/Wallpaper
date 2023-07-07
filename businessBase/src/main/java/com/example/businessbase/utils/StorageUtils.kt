//package com.example.businessbase.utils
//
//import android.content.Context
//import android.os.Environment
//import android.os.StatFs
//import android.os.storage.StorageManager
//import java.io.File
//import java.lang.reflect.Array.get
//import java.lang.reflect.Array.getLength
//import java.lang.reflect.Method
//
///**
// * 有道领世
// * silverBullet
// * Description: 存储工具类
// * Created by wanghuayi on 2023/3/4 14:25
// * Copyright @ 2023 网易有道. All rights reserved.
// **/
//const val ROOT_FOLDER = "Wallpaper"
//private val IN_PATH = Environment.getExternalStorageDirectory().absolutePath
//
/////mnt/expand/3ae43b01-bda2-44c0-ab6f-62f1e5dada99/media/0
//private const val EXPAND_PATH_PREFIX = "/mnt/expand/"
//private const val EXPAND_PATH_SUFFIX = "/media/0"
//
//private fun getExpandPath(): String {
//    try {
//        val extendFolder = File(EXPAND_PATH_PREFIX)
//        if (extendFolder.exists()) {
//            val files = extendFolder.listFiles()
//            if (files.isNotEmpty()) {
//                val inner = files[0]
//                return inner.absolutePath.plus(EXPAND_PATH_SUFFIX)
//            }
//        }
//    } catch (e: Throwable) {
//    }
//    return ""
//}
//
///**
// * 获取记忆卡basePath
// */
//fun getMCBasePath(context: Context): String {
//    return getBasePath(context).plus("/$MC_FOLDER/")
//}
//
///**
// * 获取名师讲堂basePath
// */
//fun getTLBasePath(context: Context): String {
//    return getBasePath(context).plus("/$TL_FOLDER/")
//}
//
///**
// * 获取备考密卷basePath
// */
//fun getEPBasePath(context: Context): String {
//    return getBasePath(context).plus("/$EP_FOLDER/")
//}
//
///**
// * 获取超级听力basePath
// */
//fun getSLBasePath(context: Context): String {
//    return getBasePath(context).plus("/$SL_FOLDER/")
//}
//
///**
// * 获取备考密卷的cache的basePath
// */
//fun getEPCachePath(context: Context): String {
//    return getBasePath(context).plus("/$EP_CACHE_FOLDER/")
//}
//
//fun getBasePath(context: Context): String {
//    //检查内置sdcard空间，足够才会返回
//    if (isStorageSpaceEnough(IN_PATH)) {
//        return IN_PATH
//    } else {
//        //尝试使用外置sdcard
//        val outPath = getStoragePath(context, true)
//        if (outPath != null && isStorageSpaceEnough(outPath)) {
//            d(msg = "getBasePath use outPath= $outPath")
//            return outPath
//        }
//        val extendPath = getExpandPath()
//        if (extendPath.isNotEmpty() && isStorageSpaceEnough(extendPath)) {
//            d(msg = "getBasePath use extendPath= $extendPath")
//            return extendPath
//        }
//    }
//    return IN_PATH
//}
//
//fun isStorageSpaceEnough(path: String): Boolean {
//    try {
//        val stat = StatFs(path)
//        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong;
//        val megAvailable = bytesAvailable / (1024 * 1024)
//        if (megAvailable > STORAGE_LIMIT) {
//            return true
//        }
//    } catch (e: Throwable) {
//        e(msg = "isStorageSpaceEnough e=${e.message}")
//    }
//    return false
//}
//
//fun isStorageSpaceEnough2(path: String): Boolean {
//    if (path.contains(ROOT_FOLDER)) {
//        val root = path.substring(0, path.indexOf(ROOT_FOLDER))
//        return isStorageSpaceEnough(root)
//    }
//    if (path.contains(EP_FOLDER)) {
//        val root = path.substring(0, path.indexOf(EP_FOLDER))
//        return isStorageSpaceEnough(root)
//    }
//    if (path.contains(EP_CACHE_FOLDER)) {
//        val root = path.substring(0, path.indexOf(EP_CACHE_FOLDER))
//        return isStorageSpaceEnough(root)
//    }
//    return false
//}
//
///**
// * 获取sdcard路径（内置和外置的）
// */
//private fun getStoragePath(context: Context, isRemovale: Boolean): String? {
//    val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
//    var storageVolumeClazz: Class<*>? = null
//    try {
//        storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
//        val getVolumeList: Method = mStorageManager.javaClass.getMethod("getVolumeList")
//        val getPath: Method = storageVolumeClazz.getMethod("getPath")
//        val isRemovable: Method = storageVolumeClazz.getMethod("isRemovable")
//        val result: Any = getVolumeList.invoke(mStorageManager)
//        val length: Int = getLength(result)
//        for (i in 0 until length) {
//            val storageVolumeElement: Any = get(result, i)
//            val path = getPath.invoke(storageVolumeElement) as String
//            val removable = isRemovable.invoke(storageVolumeElement) as Boolean
//            if (isRemovale == removable) {
//                return path
//            }
//        }
//    } catch (e: Throwable) {
//        e(msg = "getStoragePath exception= ${e.message}")
//    }
//    return null
//}
