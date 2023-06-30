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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.aliyun.pds.sdk.download.DownloadBlockInfoDao
import com.aliyun.pds.sdk.upload.UploadInfoDao

const val CREATE_DOWNLOAD_TABLE = """CREATE TABLE ${TransferDBModel.DownloadDB.table_name} (
            ${TransferDBModel.DownloadDB.id} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${TransferDBModel.DownloadDB.taskId} TEXT,
            ${TransferDBModel.DownloadDB.offset} TEXT,
            ${TransferDBModel.DownloadDB.start} TEXT,
            ${TransferDBModel.DownloadDB.end} TEXT)"""

const val CREATE_UPLOAD_TABLE = """CREATE TABLE ${TransferDBModel.UploadDB.table_name}(
            ${TransferDBModel.UploadDB.id} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${TransferDBModel.UploadDB.taskId} TEXT,
            ${TransferDBModel.UploadDB.currentBlock} INTEGER,
            ${TransferDBModel.UploadDB.fileId} TEXT,
            ${TransferDBModel.UploadDB.uploadId} TEXT,
            ${TransferDBModel.UploadDB.uploadState} INTEGER)"""

private class DatabaseHelperInternal(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_DOWNLOAD_TABLE)
        db?.execSQL(CREATE_UPLOAD_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}

internal class DatabaseHelper(context: Context, name: String, version: Int) {
    private val dbHelper: SQLiteOpenHelper = DatabaseHelperInternal(context, name, version)

    val downloadDao: DownloadBlockInfoDao by lazy {
        DownloadBlockInfoDao(dbHelper.writableDatabase)
    }
    val uploadInfoDao: UploadInfoDao by lazy {
        UploadInfoDao(dbHelper.writableDatabase)
    }
}