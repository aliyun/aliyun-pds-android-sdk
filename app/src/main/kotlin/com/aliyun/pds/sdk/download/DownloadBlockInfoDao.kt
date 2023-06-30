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

package com.aliyun.pds.sdk.download

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.aliyun.pds.sdk.database.TransferDBModel

class DownloadBlockInfoDao(private val db: SQLiteDatabase) {

   @SuppressLint("Range")
   fun getAll(taskId : String) : List<DownloadBlockInfo> {
      var infos: MutableList<DownloadBlockInfo> = ArrayList()
      val cursor = db.query(TransferDBModel.DownloadDB.table_name, null, "${TransferDBModel.DownloadDB.taskId}=$taskId", null, null, null, null)
      if (cursor != null) {
         if (cursor.moveToFirst()) {
            do {
               val info = DownloadBlockInfo()
               info.id = cursor.getInt(cursor.getColumnIndex(TransferDBModel.DownloadDB.id))
               info.taskId =
                  cursor.getString(cursor.getColumnIndex(TransferDBModel.DownloadDB.taskId))
               info.offset =
                  cursor.getLong(cursor.getColumnIndex(TransferDBModel.DownloadDB.offset))
               info.start = cursor.getLong(cursor.getColumnIndex(TransferDBModel.DownloadDB.start))
               info.end = cursor.getLong(cursor.getColumnIndex(TransferDBModel.DownloadDB.end))
               infos.add(info)
            } while (cursor.moveToNext())
         }
         cursor.close()
      }
      return infos
   }

   fun insert(infos: List<DownloadBlockInfo>) : List<Long> {
      var ids: MutableList<Long> = ArrayList()
      infos.forEach {
         val value = ContentValues().apply {
            put(TransferDBModel.DownloadDB.taskId, it.taskId)
            put(TransferDBModel.DownloadDB.offset, it.offset)
            put(TransferDBModel.DownloadDB.start, it.start)
            put(TransferDBModel.DownloadDB.end, it.end)
         }
         ids.add(db.insert(TransferDBModel.DownloadDB.table_name, null, value))
      }
      return ids
   }

   fun update(info : DownloadBlockInfo) {
      val value = ContentValues().apply {
         put(TransferDBModel.DownloadDB.offset, info.offset)
         put(TransferDBModel.DownloadDB.start, info.start)
         put(TransferDBModel.DownloadDB.end, info.end)
      }
      db.update(TransferDBModel.DownloadDB.table_name, value,
         "${TransferDBModel.DownloadDB.id}=?",
         arrayOf(info.id.toString())
      )
   }

   fun delete(taskId: String) {
      db.delete(TransferDBModel.DownloadDB.table_name,
         "${TransferDBModel.DownloadDB.taskId}=?",
         arrayOf(taskId)
      )
   }
}