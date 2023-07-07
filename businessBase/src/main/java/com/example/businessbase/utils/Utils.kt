package com.example.businessbase.utils

import android.content.Context
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun dp2px(context: Context, dpValue: Float) =
    (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()

fun px2dp(context: Context, pxValue: Float) =
    (pxValue / context.resources.displayMetrics.density + 0.5f).toInt()

fun String.toMD5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(this.toByteArray())
        val hexString = StringBuilder()
        for (b in messageDigest) {
            hexString.append(String.format("%02x", b.toInt() and 0xff))
        }
        hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
        ""
    }
}