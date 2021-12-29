/*
 *  Copyright 2009-2021 Alibaba Cloud All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aliyun.pds.sdk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aliyun.pds.sdk.download.DownloadBlockInfo
import com.aliyun.pds.sdk.download.DownloadBlockInfoDao
import com.aliyun.pds.sdk.upload.UploadInfo
import com.aliyun.pds.sdk.upload.UploadInfoDao


@Database(entities = [DownloadBlockInfo::class, UploadInfo::class], version = 1, exportSchema = false)
abstract class TransferDB : RoomDatabase() {
    abstract fun downloadBlockInfoDao(): DownloadBlockInfoDao
    abstract fun uploadInfoDao(): UploadInfoDao
}