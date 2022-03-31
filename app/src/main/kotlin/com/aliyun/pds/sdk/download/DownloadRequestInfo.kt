package com.aliyun.pds.sdk.download

class DownloadRequestInfo(
    downloadUrl: String,
    fileId: String,
    fileName: String,
    filePath: String,
    fileSize: Long,
    driveId: String,
    shareId: String? = "",
    shareToken: String? = "",
    revisionId: String? = "",
    contentHash: String? = "",
    contentHashName: String? = "",
    isLivePhoto: Boolean = false
    ) {
    val downloadUrl = downloadUrl
    val fileId = fileId
    val fileName = fileName
    val filePath = filePath
    val fileSize = fileSize
    val driveId = driveId
    val shareId = shareId
    val shareToken = shareToken
    val revisionId = revisionId
    val contentHash = contentHash
    val contentHashName = contentHashName
    val isLivePhoto = isLivePhoto
}