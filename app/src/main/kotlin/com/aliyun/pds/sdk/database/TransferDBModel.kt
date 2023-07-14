package com.aliyun.pds.sdk.database

object TransferDBModel {

    object DownloadDB {
        const val table_name = "DownloadBlockInfo"
        const val id = "id"
        const val taskId = "taskId"
        const val offset = "offset"
        const val start = "start"
        const val end = "end"
    }

    object UploadDB {
        const val table_name = "UploadInfo"
        const val id = "id"
        const val taskId = "taskId"
        const val currentBlock = "currentBlock"
        const val fileId = "fileId"
        const val uploadId = "uploadId"
        const val uploadState = "uploadState"
    }

}