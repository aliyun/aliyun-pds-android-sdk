package com.aliyun.pds.demo

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {

    fun isNetwork(context: Context): Boolean {
        val connectivityManager : ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo == null
    }

}