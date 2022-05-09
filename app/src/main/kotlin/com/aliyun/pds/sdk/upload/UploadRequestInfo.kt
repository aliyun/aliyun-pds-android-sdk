package com.aliyun.pds.sdk.upload

class UploadRequestInfo(builder: Builder) {

    val fileName: String
    val filePath: String
    val fileSize: Long
    val parentId: String
    val driveId: String?
    val fileId: String?
    val shareId: String?
    val shareToken: String?
    val sharePwd: String?
    val mimeType: String?
    val checkNameMode: String?

    init {
       fileName = builder.fileName
       filePath = builder.filePath
       fileSize = builder.fileSize
       parentId = builder.parentId
       driveId = builder.driveId
       fileId = builder.fileId
       shareId = builder.shareId
       shareToken = builder.shareToken
       sharePwd = builder.sharePwd
       mimeType = builder.mimeType
       checkNameMode = builder.checkNameMode
    }

    class Builder {
        internal var fileName: String = ""
        internal var filePath: String = ""
        var fileSize: Long = 0
        var parentId: String = ""
        var driveId: String? = null
        var fileId: String? = null
        var shareId: String? = null
        var shareToken: String? = null
        var sharePwd: String? = null
        var mimeType: String? = ""
        var checkNameMode: String? = "auto_rename"

        fun fileName(name: String): Builder = apply {
           fileName = name
        }

        fun filePath(path: String): Builder = apply {
            filePath = path
        }

        fun fileSize(size: Long): Builder = apply {
            fileSize = size
        }

        fun fileId(fileId: String): Builder = apply {
            this.fileId = fileId
        }

        fun parentId(parentId: String) : Builder = apply {
            this.parentId = parentId
        }

        fun driveId(driverId: String): Builder = apply {
            this.driveId = driverId
        }

        fun shareId(shareId: String): Builder = apply {
            this.shareId = shareId
        }

        fun shareToken(shareToken: String): Builder = apply {
            this.shareToken = shareToken
        }

        fun sharePwd(sharePwd: String): Builder = apply {
            this.sharePwd = sharePwd
        }

        fun mimeType(mimeType: String): Builder = apply {
            this.mimeType = mimeType
        }

        fun checkNameMode(checkNameMode: String): Builder = apply {
            this.checkNameMode = checkNameMode
        } 

        fun build(): UploadRequestInfo {
            return UploadRequestInfo(this)
        }
    }
}
