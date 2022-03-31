package com.aliyun.pds.sdk.upload

class UploadRequestInfo(
    fileName: String,
    filePath: String,
    fileSize: Long,
    parentId: String,
    driveId: String,
    fileId: String? = null,
    shareId: String? = null,
    mimeType: String? = "",
    checkNameMode: String = "auto_rename"
){
    val fileId = fileId
    val fileName = fileName
    val filePath = filePath
    val fileSize = fileSize
    val parentId = parentId
    val driveId = driveId
    val shareId = shareId
    val mimeType = mimeType
    val checkNameMode = checkNameMode
}
