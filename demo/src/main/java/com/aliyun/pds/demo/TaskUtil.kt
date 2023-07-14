package com.aliyun.pds.demo

import android.app.Activity
import android.content.Context
import com.aliyun.pds.sdk.SDErrorInfo
import com.aliyun.pds.sdk.SDTransferError

/**
 * 任务相关工具类
 */
object TaskUtil {

    /**
     * 生成当前任务taskId
     */
    fun getTaskId(act: Activity): String {
        val sp = act.getSharedPreferences("pds_sdk", Context.MODE_PRIVATE)
        val transferNum = sp.getInt("TRANSFER_NUM", 0)

        val editor = sp.edit()
        editor.putInt("TRANSFER_NUM", transferNum + 1)
        editor.commit()

        return transferNum.toString()
    }

    /**
     * 获取传输结果
     */
    fun getTransferResultMsg(transferType: Int, errorInfo: SDErrorInfo?): String {
        if (errorInfo == null) {
            return if (transferType == TransferUtil.TRANSFER_DOWNLOAD) "下载成功" else "上传成功"
        } else {
            when (errorInfo.code) {
                SDTransferError.None -> {
                    return if (transferType == TransferUtil.TRANSFER_DOWNLOAD) "下载成功" else "上传成功"
                }
                SDTransferError.Unknown -> {
                    return "未知错误"
                }
                SDTransferError.Network -> {
                    return "网络错误"
                }
                SDTransferError.FileNotExist -> {
                    return "文件不存在"
                }
                SDTransferError.SpaceNotEnough -> {
                    return "空间不够"
                }
                SDTransferError.Server -> {
                    return "服务出错"
                }
                else -> {
                    return "未知错误"
                }
            }
        }
    }

}