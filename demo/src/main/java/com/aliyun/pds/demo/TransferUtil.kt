package com.aliyun.pds.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.aliyun.pds.demo.ui.TaskModel
import com.aliyun.pds.demo.ui.TaskList
import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.download.DownloadRequestInfo
import com.aliyun.pds.sdk.model.FileInfoResp
import com.aliyun.pds.sdk.upload.UploadRequestInfo
import java.io.File

object TransferUtil {

    const val TAG = "TransferUtil"
    const val TRANSFER_UPLOAD = 0x000001
    const val TRANSFER_DOWNLOAD = 0x000002

    fun initTransfer(context: Context) {
        val accessToken = Config.accessToken
        val apiHost = Config.apiHost

        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(apiHost)) {
            Toast.makeText(context, "请填写你的token和apiHost", Toast.LENGTH_LONG).show()
        }

        val token = SDToken(accessToken)
        val file = context.getExternalFilesDir(null)
        val dir = File(file, "PDSDemo")
        dir.mkdirs()

        val config = SDConfig.Builder(token, apiHost, 3600)
            .maxRetryCount(3)
            .canFastUpload(false)
            .isDebug(true)
            .downloadBlockSize(1024 * 1024 * 5L)
            .uploadBlockSize(1024 * 1024 * 5L)
            .connectTimeout(30L)
            .readTimeout(30L)
            .build()
        SDClient.instance.init(context, config)
    }

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
        val task =
            if (transferType == TRANSFER_DOWNLOAD)
                getDownloadTask(act, fileInfo!!, downloadFilePath!!)
            else
                getUploadTask(act, file!!)

        task.setOnCompleteListener(object : OnCompleteListener{
            override fun onComplete(taskId: String, fileMeta: SDFileMeta, errorInfo: SDErrorInfo?) {
                if (errorInfo!!.code == SDTransferError.None) {
                    uploadSuccessListener!!.onUploadSuccess()
                }
            }
        })
    }

    private fun getUploadTask(act: Activity, file: File): SDTask {

        val requestInfo = UploadRequestInfo.Builder()
            .fileName(file.name)
            .filePath(file.path)
            .fileSize(file.length())
            .parentId("root")
            .driveId(Config.driveId)
            .build()

        val taskId = TaskUtil.getTaskId(act)
        val sdTask = SDClient.instance.createUploadTask(
            TaskUtil.getTaskId(act), requestInfo
        )
        TaskList.newInstance().addUploadTask(TaskModel(taskId, requestInfo.fileName, requestInfo.fileSize), sdTask)

        return sdTask
    }

    private fun getDownloadTask(act: Activity, item: FileInfoResp, downloadFilePath: String): SDTask {

        val requestInfo = DownloadRequestInfo.Builder()
            .downloadUrl(item.downloadUrl!!)
            .fileId(item.fileId!!)
            .fileName(item.name!!)
            .filePath(downloadFilePath)
            .fileSize(item.fileSize!!)
            .driveId(Config.driveId)
            .contentHash(item.crc64Hash)
            .contentHashName("crc64")
            .build()

        val taskId = TaskUtil.getTaskId(act)
        val sdTask = SDClient.instance.createDownloadTask(
            TaskUtil.getTaskId(act), requestInfo
        )
        TaskList.newInstance().addDownloadTask(TaskModel(taskId, requestInfo.fileName, requestInfo.fileSize), sdTask)

        return sdTask
    }

}