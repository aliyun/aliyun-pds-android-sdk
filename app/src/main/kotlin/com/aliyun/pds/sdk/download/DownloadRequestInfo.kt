package com.aliyun.pds.sdk.download

class DownloadRequestInfo(
   val downloadUrl: String,
   val fileId: String,
   val fileName: String,
   val filePath: String,
   val fileSize: Long,
   val driveId: String,
   val shareId: String? = "",
   val shareToken: String? = "",
   val revisionId: String? = "",
   val contentHash: String? = "",
   val contentHashName: String? = "",
   val isLivePhoto: Boolean = false
)