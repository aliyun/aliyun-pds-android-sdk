package com.aliyun.pds.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.model.FileInfoResp
import java.io.File

object TransferUtil {

    const val TAG = "TransferUtil"
    const val TRANSFER_UPLOAD = 0x000001
    const val TRANSFER_DOWNLOAD = 0x000002

    fun startUpload(act: Activity, file: File, uploadSuccessListener: OnUploadSuccessListener) {
        startTransfer(act, TRANSFER_UPLOAD, file = file, uploadSuccessListener = uploadSuccessListener)
    }

    fun startDownload(act: Activity, fileInfo: FileInfoResp, downloadFilePath: String) {
        startTransfer(act, TRANSFER_DOWNLOAD, fileInfo, downloadFilePath)
    }

    @SuppressLint("InflateParams")
    private fun startTransfer(act: Activity,
                              transferType: Int,
                              fileInfo: FileInfoResp? = null,
                              downloadFilePath: String? = null,
                              file: File? = null,
                              uploadSuccessListener: OnUploadSuccessListener? = null) {
        val dialog = Dialog(act, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
        dialog.setCancelable(false)

        val view = LayoutInflater.from(act).inflate(R.layout.dialog_download, null)

        val tvTitle = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)
        tvTitle.text = if (transferType == TRANSFER_DOWNLOAD) "正在下载..." else "上传中..."
        progressBar.max =
            if (transferType == TRANSFER_DOWNLOAD)
                fileInfo!!.fileSize!!.toInt()
            else
                file!!.length().toInt()

        val btnPause = view.findViewById<AppCompatButton>(R.id.btnPause)
        val btnStop = view.findViewById<AppCompatButton>(R.id.btnStop)

        val task =
            if (transferType == TRANSFER_DOWNLOAD)
                getDownloadTask(act, fileInfo!!, downloadFilePath!!)
            else
                getUploadTask(act, file!!)
        task.progressListener = object : OnProgressListener {
            override fun onProgressChange(currentSize: Long) {
                Log.e(TAG, "============>$currentSize")
                progressBar.progress = currentSize.toInt()
            }
        }

        task.completeListener = object : OnCompleteListener {
            override fun onComplete(taskId: String, fileMeta: SDFileMeta, errorInfo: SDErrorInfo?) {
                Log.e(TAG, "============>完成")
                act.runOnUiThread {
                    if (transferType == TRANSFER_UPLOAD) {
                        uploadSuccessListener!!.onUploadSuccess()
                    }

                    Toast.makeText(
                        act,
                        getTransferResultMsg(transferType, errorInfo),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
        }

        btnPause.setOnClickListener{
            if (btnPause.text.toString() == "暂停") {
                task.pause()
                btnPause.text = "开始"
            } else {
                task.resume()
                btnPause.text = "暂停"
            }
        }

        btnStop.setOnClickListener {
            task.cancel()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.create()
        dialog.show()
    }

    private fun getUploadTask(act: Activity, file: File): SDBaseTask {
        return SDClient.instance.createUploadTask(
            getTaskId(act),
            file.name,
            file.path,
            file.length(),
            "",
            "root",
            "",
            Config.driveId,
        )
    }

    private fun getDownloadTask(act: Activity, item: FileInfoResp, downloadFilePath: String): SDBaseTask {
        return SDClient.instance.createDownloadTask(
            getTaskId(act),
            item.downloadUrl,
            item.fileId!!,
            item.driveId,
            item.name!!,
            item.fileSize!!,
            downloadFilePath,
            null,
            null,
            null
        )
    }

    private fun getTaskId(act: Activity): String {
        val sp = act.getSharedPreferences("pds_sdk", MODE_PRIVATE)
        val transferNum = sp.getInt("TRANSFER_NUM", 0)

        val editor = sp.edit()
        editor.putInt("TRANSFER_NUM", transferNum + 1)
        editor.commit()

        return transferNum.toString()
    }

    fun getTransferResultMsg(transferType: Int, errorInfo: SDErrorInfo?): String {
        if (errorInfo == null) {
            return if (transferType == TRANSFER_DOWNLOAD) "下载成功" else "上传成功"
        } else {
            when (errorInfo.code) {
                SDTransferError.None -> {
                    return if (transferType == TRANSFER_DOWNLOAD) "下载成功" else "上传成功"
                }
                SDTransferError.Unknown -> {
                    return "位置错误"
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
                SDTransferError.SizeExceed -> {
                    return "尺寸过大"
                }
                SDTransferError.PermissionDenied -> {
                    return "没有权限"
                }
                SDTransferError.Server -> {
                    return "服务出错"
                }
                SDTransferError.RemoteFileNotExist -> {
                    return "文件不存在"
                }
            }
        }
    }

}