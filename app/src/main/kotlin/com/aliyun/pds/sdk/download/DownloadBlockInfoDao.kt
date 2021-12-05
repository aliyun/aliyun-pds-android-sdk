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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DownloadBlockInfoDao {
   @Query("SELECT * FROM downloadblockinfo WHERE taskId = :taskId")
   fun getAll(taskId : String) : List<DownloadBlockInfo>

   @Insert
   fun insert(info: List<DownloadBlockInfo>) : List<Long>

   @Update
   fun update(info :DownloadBlockInfo)

   @Query("DELETE FROM downloadblockinfo WHERE taskId = :taskId")
   fun delete(taskId: String)
}