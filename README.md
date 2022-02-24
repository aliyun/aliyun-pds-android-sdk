# PDS Android SDK

## 当前主要功能

* 文件上传下载
* 文件操作相关API

## 集成

```kotlin
implementation 'com.aliyun.pds:android-sdk:0.0.2'
```


## 初始化

```kotlin
val token = SDToken("you access token")  // 这个通用流程是你们登入自己的账号系统后获取，后端使用 PDS 平台申请的 appKey & appSecret 换取 token 返回给客户端
val apiHost = "you api host"   // 请在PDS控制台获取你的 api host
val config = SDConfig(token, 3600, apiHost)
SDClient.instance.init(this, config)
```


- [PDS Android SDK](#PDS Android SDK)
  -[当前主要功能](#当前主要功能)
  -[集成](#集成)
  -[初始化](#初始化)
  -[下载任务](#下载任务)
  -[上传任务](#上传任务)
  -[文件操作接口](#文件操作接口)
    -[列举文件或文件夹](#列举文件或文件夹)
    -[文件搜索](#文件搜索)
    -[创建文件或者文件夹](#创建文件或者文件夹)
    -[获取文件或文件夹信息](#获取文件或文件夹信息)
    -[拷贝文件或文件夹](#拷贝文件或文件夹)
    -[移动文件或文件夹](#移动文件或文件夹)
    -[更新文件或文件夹信息](#更新文件或文件夹信息)
    -[删除文件或文件夹](#删除文件或文件夹)
    -[其它](#其它)
  -[demo配置](#demo配置)


## 下载任务

**注意任务的进度和状态回调都在子线程,若要更新UI请自行切换到主线程**

创建一个 `下载任务` 并操作它 :

```kotlin
// 创建任务, 
val task = SDClient.instance.createDownloadTask(
    // taskId
    taskId,
	// 下载url 
	url,
	// fileId
	fileId,
	// 所属driveId
	driveId,
	// 文件名
	fileName,
	// 文件大小
	fileSize,
	// 文件保存路径
	filePath,
	// 文件来自分享（不涉及分享业务设为空 
	shareId,
	// 效验hash值
	contentHash,
	// hash格式( "sha1", "crc64")
	contentHashName,
	// 完成监听（成功，失败都会回调
	completeListener,
	// 下载进度监听
	progressListener, 			
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
// 创建任务
val task = SDClient.instance.createUploadTask(
    // 任务id
    taskId,
	// 文件名
	fileName,
	// 文件路径
	path,
	// 文件大小
	length,
	// 文件父目录id
	parentId,
	// 文件类型 （可选
	mimeType,
	// 父目录所属 driveId
	driveId,
	// 不涉及分享业务 不传
	shareId,
	// 完成监听
	completeListener,
	// 进度监听
	progressListener, 
)

// 暂停任务
task.pause()

// 恢复任务
task.resume()

// 删除取消任务
task.cancel()

```


## 文件操作接口 

具体请求参数和返回值参考[官方API文档](https://help.aliyun.com/document_detail/175927.html)

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








