package com.aliyun.pds.sdk

import android.content.Context
import com.aliyun.pds.sdk.database.DatabaseHelper
import com.aliyun.pds.sdk.download.DownloadBlockInfoDao
import com.aliyun.pds.sdk.upload.UploadInfoDao
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

abstract class BaseTest {

    @Mock
    lateinit var mockContext: Context

    @Mock
    internal lateinit var databaseHelper: DatabaseHelper

    @Mock
    lateinit var downloadDao: DownloadBlockInfoDao

    @Mock
    lateinit var uploadInfoDao: UploadInfoDao


    val config = MockUtils.mockSDConfig()

    @Before
    fun init() {
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        SDClient.instance.init(mockContext, config)
        SDClient.instance.database = databaseHelper
        setup()
    }

    abstract fun setup()

}