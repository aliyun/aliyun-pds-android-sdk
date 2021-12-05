### 配置

token 和 apihost 默认配置在 local.properties，可以配置到这里

配置格式 

```
accessToken="xxxxx"
apiHost="xxxx"
```

也可以直接修改代码即可代码在 MainActivity.kt

```kotlin
val accessToken = BuildConfig.accessToken
val apiHost = BuildConfig.apiHost

```



下载测试需要自行构建一个下载任务

具体参照  **DownloadActivity**

上传需要自行构建上传任务 

具体参照 **UploadActivity**

文件API操作参照 **FileActivity**

 
