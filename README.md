# PDS Android SDK

## 当前主要功能

* 文件上传下载
* 文件操作相关API

## 集成

```kotlin
implementation 'com.aliyun.pds:android-sdk:0.1.1'
```
 ** 支持的SDK最低版本为21 **


## 初始化

```kotlin
val token = SDToken("you access token") // 这个通用流程是你们登入自己的账号系统后获取，后端使用 PDS 平台申请的 appKey & appSecret 换取 token 返回给客户端
val apiHost = "you api host"            // 请在PDS控制台获取你的 api host
// val config = SDConfig(token, 3600, apiHost) 旧版本方法，已废弃，不建议使用
val config = SDConfig.Builder(token, apiHost, 3600)
    .canFastUpload()        // 是否支持妙传，默认true (选填)
    .userAgent()            // (选填)
    .maxRetryCount()        // 最大重试次数,默认3  (选填)
    .isDebug()              // 是否开启调试模式,默认false (选填)
    .downloadBlockSize()    // 下载分片大小,默认10M (选填)
    .uploadBlockSize()      // 上传分片大小,默认4M  (选填)
    .connectTimeout()       // 建立网络连接超时时间设置,默认15s (选填)
    .readTimeout()          // 网络连接响应超时时间设置,默认60s (选填)
    .writeTimeout()         // 网络传输响应超时时间设置,默认60s (选填)
    .build()
SDClient.instance.init(this, config)
```


## 下载任务

**注意任务的进度和状态回调都在子线程,若要更新UI请自行切换到主线程**

创建一个 `下载任务` 并操作它 :

```kotlin
// 初始化下载信息
val downloadInfo = DownloadRequestInfo.Builder()
    .downloadUrl(url)
    .driveId(driveId)
    .fileId(fileId)
    .fileName(fileName)
    .fileSize(fileSize)
    .filePath(dir.path)             // 文件保存路径
    .shareId(shareId)               // 文件来自分享：id(不涉及分享业务可不传)
    .shareToken(shareToken)         // 文件来自分享：token(不涉及分享业务可不传)
    .sharePwd(sharePwd)             // 文件来自分享：pwd(不涉及分享业务可不传)
    .revisionId(revisionId)         // 历史版本相关：id(下载文件的历史版本时，需要传入，不涉及可不传)
    .contentHash(hash)              // hash 效验值
    .contentHashName("crc64")       // hash 效验算法名 当前只支持 crc64
    .build()

// 创建任务, 
val task = SDClient.instance.createDownloadTask(
    taskId,                 // taskId
    downloadInfo,           // 下载信息
    completeListener,       // 下载完成监听（成功，失败都会回调。失败时会返回错误信息)
    progressListener        // 下载进度监听	
)

// 暂停任务，只有运行中的任务可以暂停
task.pause()

// 恢复任务, 只有暂停态的任务可以恢复
task.resume()

// 取消删除任务
task.cancel()

// 重启任务
task.restart()
```


## 上传任务

**注意任务的进度和状态回调都在子线程,若要更新UI请自行切换到主线程**

创建一个 `上传任务` 并操作它 :

```kotlin
// 初始化上传信息
val uploadInfo = UploadRequestInfo.Builder()
    .fileName("edmDrive")
    .filePath(file.absolutePath)
    .fileSize(file.length())
    .parentId(parentId)
    .driveId(driveId)
    .mimeType(mimeType)
    .fileId(fileId)                     // 文件id，覆盖上传时必填
    .checkNameMode(checkNameMode)       // 同名文件处理模式，默认为"auto_rename"，具体参数说明如下：
                                        // auto_rename: 当发现同名文件是，云端自动重命名，默认为追加当前时间点，如 xxx _20060102_150405;
                                        // ignore: 允许同名文件;
                                        // refuse：当云端存在同名文件时，拒绝创建新文件，直接提示上传成功

    .shareId(shareId)                   //上传到的文件夹来自分享：id(不涉及可不传)
    .shareToken(shareToken)             //上传到的文件夹来自分享：token(不涉及可不传)
    .sharePwd(sharePwd)                 //上传到的文件夹来自分享：pwd(不涉及可不传)
    .build()

// 创建任务
val task = SDClient.instance.createUploadTask(
    taskId,                 // 任务id
    uploadInfo,             // 上传信息
    completeListener,       // 上传完成监听（成功，失败都会回调。失败时会返回错误信息)
    progressListener        // 上传进度监听
)

// 暂停任务
task.pause()

// 恢复任务
task.resume()

// 删除取消任务
task.cancel()

```

## 文件上传/下载错误信息错误码说明

```kotlin
SDTransferError.Unknown // 未知错误
SDTransferError.Network // 网络错误
SDTransferError.FileNotExist // 文件没有找到
SDTransferError.SpaceNotEnough // 空间不足
SDTransferError.Server // 服务器错误
SDTransferError.TmpFileNotExist // 下载临时文件不存在
SDTransferError.PathRuleError // 下载路径规则错误
SDTransferError.SizeExceed // 文件过大
SDTransferError.PermissionDenied // 没有权限
SDTransferError.RemoteFileNotExist // 找不到远程文件
SDTransferError.ShareLinkCancelled // 分享连接已取消
```
> 注：错误类型为SDTransferError.Server时，错误信息中会包含errorCode，可对照[错误码文档](https://next.api.aliyun.com/document/pds/2022-03-01/errorCode)确定具体错误原因。

## 文件操作接口

具体请求参数和返回值参考[官方API文档](https://help.aliyun.com/document_detail/440389.html)

通过 `SDClient.fileApi` 拿到 `fileApi` 对象后调用如下方法访问对应 api
> 注：请求示例中的参数仅为基础参数，其它参数请参考官方API文档

### 列举文件或文件夹

```kotlin
fun fileList(fileListRequest: FileListRequest): FileListResp?

// FileListRequest 示例
val request = FileListRequest()
request.parentId = "root"               // 所获取文件夹的fileId(root为根目录)
request.driveId = ""                    // 获取列表的用户driveId
```

### 文件搜索

```kotlin
fun fileSearch(fileSearchRequest: FileSearchRequest): FileListResp?

// FileSearchRequest示例
val request = FileSearchRequest()
request.query = "name match '$keyStr' and status = 'available'"     // keyStr:搜索关键词
request.driveId = ""                                                // 搜索的用户driveId
```

### 创建文件或者文件夹

```kotlin
fun fileCreate(createRequest: FileCreateRequest): FileCreateResp?

// FileCreateRequest示例
val createRequest = FileCreateRequest()
createRequest.checkNameMode = "auto_rename"     // enum (ignore, auto_rename, refuse)
createRequest.driveId = ""                      // 实施创建操作的用户driveId
createRequest.name = ""                         // 新建文件的名称
createRequest.parentFileId = "root"             // 目标文件夹的fileId(root为根目录)
createRequest.type = "folder"                   // enum (file, folder)
```

### 获取文件或文件夹信息

```kotlin
fun fileGet(getResp: FileGetRequest): FileGetResp?

// FileGetRequest 示例
val getRequest = FileGetRequest()
getRequest.driveId = ""                     // 实施查看操作的用户driveId
getRequest.fileId = ""                      // 文件的fileId
```

### 拷贝文件或文件夹

```kotlin
fun fileCopy(fileCopyRequest: FileCopyRequest): FileCopyResp?

// FileCopyRequest 示例
val copyRequest = FileCopyRequest()
copyRequest.driveId = ""                    // 文件的driveId
copyRequest.fileId = ""                     // 文件的fileId
copyRequest.toDriveId = ""                  // 拷贝目标文件夹的driveId
copyRequest.newName = ""                    // 文件拷贝后的新名字
copyRequest.toParentId = "root"             // 拷贝目标文件夹的fileId(root为根目录)
```

### 移动文件或文件夹

```kotlin
fun fileMove(fileMoveRequest: FileMoveRequest): FileMoveResp?

// FileMoveRequest 示例
val moveRequest = FileMoveRequest()
moveRequest.driveId = ""                    // 文件的driveId
moveRequest.fileId = ""                     // 文件的fileId
moveRequest.toDriveId = ""                  // 移动目标文件夹的driveId
moveRequest.newName = ""                    // 文件移动后的新名字
moveRequest.toParentId = "root"             // 移动目标文件夹的fileId(root为根目录)
```

### 更新文件或文件夹信息

```kotlin
fun fileUpdate(updateRequest: FileUpdateRequest): FileGetResp?

// FileUpdateRequest 示例
val updateRequest = FileUpdateRequest()
updateRequest.driveId = item.driveId!!      // 实施更新操作的用户driveId
updateRequest.fileId = item.fileId          // 文件的fileId
updateRequest.name = ""                     // 文件更新后的新名字
```

### 删除文件或文件夹

```kotlin
fun fileDelete(deleteRequest: FileDeleteRequest): FileDeleteResp?

// FileDeleteRequest 示例
val delRequest = FileDeleteRequest()
delRequest.driveId = ""                     // 实施删除操作的用户driveId
delRequest.fileId = ""                      // 需要删除文件或文件夹的fileId
```

### 其它

```kotlin
// 获取文件分片的上传地址
fun fileGetUploadUrl(getUploadUrlRequest: FileGetUploadUrlRequest): FileGetUploadUrlResp?

// 完成文件上传
fun fileComplete(completeRequest: FileCompleteRequest): FileGetResp?

// 获取文件下载地址
fun fileGetDownloadUrl(getDownloadUrlRequest: FileGetDownloadUrlRequest): FileGetDownloadUrlResp?

// 异步任务状态，例如删除包含多个文件的文件夹，此时是一个异步任务，可以通过这个接口获取任务状态
fun getAsyncTask(getAsyncTaskRequest: AsyncTaskRequest): AsyncTaskResp?
```

## demo配置

> Demo配置见demo目录中[README.md](./demo/README.md)文件








