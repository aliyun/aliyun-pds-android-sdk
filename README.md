## PDS Android SDK


**当前主要功能**

* 文件上传下载
* 文件相关API操作

**集成**

    implementation 'com.aliyun.pds:android-sdk:0.0.2'


**初始化**

```kotlin
val token = SDToken("you access token")  // 这个通用流程是你们登入自己的账号系统后获取，后端使用 PDS 平台申请的 appKey & appSecret 换取 token 返回给客户端
val apiHost = "you api host"   // 请在PDS控制台获取你的 api host
val config = SDConfig(token, 3600, apiHost)
SDClient.instance.init(this, config)
```



**下载任务**

**注意任务的进度和状态回调都在子线程若要更新UI请自行切换到主线程**

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



上传任务

**注意任务的进度和状态回调都在子线程若要更新UI请自行切换到主线程**

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



**文件操作接口** 

具体请求参数和返回值参考官方 AIP 文档  https://help.aliyun.com/document_detail/175927.html

```kotlin

通过 SDClient.fileApi 拿到 fileApi 对象后调用如下方法访问对应 api

// 创建文件
fun fileCreate(createRequest: FileCreateRequest): FileCreateResp?

// 获取上传url
fun fileGetUploadUrl(getUploadUrlRequest: FileGetUploadUrlRequest): FileGetUploadUrlResp?

// 文件上传完成
fun fileComplete(completeRequest: FileCompleteRequest): FileGetResp?

// 获取下载链接
fun fileGetDownloadUrl(getDownloadUrlRequest: FileGetDownloadUrlRequest): FileGetDownloadUrlResp?

// 获取单个文件详情
fun fileGet(getResp: FileGetRequest): FileGetResp?

// 更新文件
fun fileUpdate(updateRequest: FileUpdateRequest): FileGetResp?

// 删除文件
fun fileDelete(deleteRequest: FileDeleteRequest): FileDeleteResp?

// 拷贝文件
fun fileCopy(fileCopyRequest: FileCopyRequest): FileCopyResp?

// 移动文件
fun fileMove(fileMoveRequest: FileMoveRequest): FileMoveResp?

// 文件列表
fun fileList(fileListRequest: FileListRequest): FileListResp?

// 搜索文件
fun fileSearch(fileSearchRequest: FileSearchRequest): FileListResp?

// 异步任务状态，例如删除包含多个文件的文件夹，此时是一个异步任务，可以通过这个接口获取任务状态
fun getAsyncTask(getAsyncTaskRequest: AsyncTaskRequest): AsyncTaskResp?
```









