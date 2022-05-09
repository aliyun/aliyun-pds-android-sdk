package com.aliyun.pds.sdk.download

class DownloadRequestInfo(builder: Builder) {

   val downloadUrl: String
   val fileId: String
   val fileName: String
   val filePath: String
   val fileSize: Long
   val driveId: String
   val shareId: String?
   val shareToken: String?
   val sharePwd: String?
   val revisionId: String?
   val contentHash: String?
   val contentHashName: String?
   val isLivePhoto: Boolean

   init {
      downloadUrl = builder.downloadUrl
      fileId = builder.fileId
      fileName = builder.fileName
      filePath = builder.filePath
      fileSize = builder.fileSize
      driveId = builder.driveId
      shareId = builder.shareId
      shareToken = builder.shareToken
      sharePwd = builder.sharePwd
      revisionId = builder.revisionId
      contentHash = builder.contentHash
      contentHashName = builder.contentHashName
      isLivePhoto = builder.isLivePhoto
   }

   class Builder {
      internal var downloadUrl: String = ""
      internal var fileId: String = "" 
      internal var fileName: String = ""

      internal var filePath: String = ""
      internal var fileSize: Long = 0
      internal var driveId: String = ""
      internal var shareId: String? = null
      internal var shareToken: String? = null
      internal var sharePwd: String? = null
      internal var revisionId: String? = null
      internal var contentHash: String? = null
      internal var contentHashName: String? = null
      internal var isLivePhoto: Boolean = false


      fun downloadUrl(url: String) : Builder = apply {
         this.downloadUrl = url
      }

      fun fileId(fileId: String) : Builder = apply {
         this.fileId = fileId
      }

      fun fileName(fileName: String) : Builder = apply {
         this.fileName = fileName
      }

      fun filePath(filePath: String) : Builder = apply {
         this.filePath = filePath
      }

      fun fileSize(fileSize: Long) : Builder = apply {
         this.fileSize = fileSize
      }

      fun driveId(driveId: String) : Builder = apply {
         this.driveId = driveId
      }

      fun shareId(shareId: String) : Builder = apply {
         this.shareId = shareId
      }

      fun shareToken(shareToken: String) : Builder = apply {
         this.shareToken = shareToken
      }

      fun sharePwd(sharePwd: String) : Builder = apply {
         this.sharePwd = sharePwd
      }

      fun revisionId(revisionId: String) : Builder = apply {
         this.revisionId = revisionId
      }

      fun contentHash(contentHash: String) : Builder = apply {
         this.contentHash = contentHash
      }

      fun contentHashName(contentHashName: String) : Builder = apply {
         this.contentHashName = contentHashName
      }

      fun isLivePhoto(isLivePhoto: Boolean) : Builder = apply {
         this.isLivePhoto = isLivePhoto
      }

      fun build() : DownloadRequestInfo {
         return DownloadRequestInfo(this)
      }

   }

}

