package com.aliyun.pds.sdk.upload

class UploadRequestInfo(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val parentId: String,
    val driveId: String,
    val fileId: String? = null,
    val shareId: String? = null,
    val shareToken: String? = null,
    val mimeType: String? = "",
    val checkNameMode: String? = "auto_rename"
)