package com.example.businessbase.download

interface IDownload {

    fun init() {}

    fun release() {}

    fun start(download: DownloadBean): Long?

    fun delete()
}