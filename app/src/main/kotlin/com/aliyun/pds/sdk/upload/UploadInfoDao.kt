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

package com.aliyun.pds.sdk.upload

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.aliyun.pds.sdk.database.TransferDBModel

class UploadInfoDao(private val db: SQLiteDatabase) {

    @SuppressLint("Range")
    fun getUploadInfo(taskId : String) : UploadInfo? {
        var info: UploadInfo? = null
        val cursor = db.query(TransferDBModel.UploadDB.table_name, null, "${TransferDBModel.UploadDB.taskId}=$taskId", null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    info = UploadInfo()
                    info.id = cursor.getInt(cursor.getColumnIndex(TransferDBModel.UploadDB.id))
                    info.taskId =
                        cursor.getString(cursor.getColumnIndex(TransferDBModel.UploadDB.taskId))
                    info.currentBlock =
                        cursor.getInt(cursor.getColumnIndex(TransferDBModel.UploadDB.currentBlock))
                    info.fileId =
                        cursor.getString(cursor.getColumnIndex(TransferDBModel.UploadDB.fileId))
                    info.uploadId =
                        cursor.getString(cursor.getColumnIndex(TransferDBModel.UploadDB.uploadId))
                    info.uploadState =
                        cursor.getInt(cursor.getColumnIndex(TransferDBModel.UploadDB.uploadState))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return info
    }

    fun insert(info: UploadInfo) : Long {
        val value = ContentValues().apply {
            put(TransferDBModel.UploadDB.taskId, info.taskId)
            put(TransferDBModel.UploadDB.currentBlock, info.currentBlock)
            put(TransferDBModel.UploadDB.fileId, info.fileId)
            put(TransferDBModel.UploadDB.uploadId, info.uploadId)
            put(TransferDBModel.UploadDB.uploadState, info.uploadState)
        }
        return db.insert(TransferDBModel.UploadDB.table_name, null, value)
    }

    fun update(info : UploadInfo) {
        val value = ContentValues().apply {
            put(TransferDBModel.UploadDB.currentBlock, info.currentBlock)
            put(TransferDBModel.UploadDB.fileId, info.fileId)
            put(TransferDBModel.UploadDB.uploadId, info.uploadId)
            put(TransferDBModel.UploadDB.uploadState, info.uploadState)
        }
        db.update(TransferDBModel.UploadDB.table_name, value,
            "${TransferDBModel.UploadDB.id}=?",
            arrayOf(info.id.toString())
        )
    }

    fun delete(info: UploadInfo) {
        db.delete(TransferDBModel.UploadDB.table_name,
            "${TransferDBModel.UploadDB.taskId}=?",
            arrayOf(info.taskId)
        )
    }

}