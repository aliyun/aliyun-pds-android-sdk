### 配置

token, apihost, driveId 默认都配置在 local.properties 文件中:

> 配置格式 
```
accessToken="xxxxx"
apiHost="xxxx"
driveId="xxxx"
```

也可以直接修改代码,在 [Config.kt](src/main/java/com/aliyun/pds/demo/Config.kt) :

```kotlin
const val accessToken = BuildConfig.accessToken
const val apiHost = BuildConfig.apiHost
const val driveId = BuildConfig.driveId
```



下载测试需要自行构建一个下载任务

具体参照  **DownloadActivity**

上传需要自行构建上传任务 

具体参照 **UploadActivity**

文件API操作参照 **FileActivity**

 
