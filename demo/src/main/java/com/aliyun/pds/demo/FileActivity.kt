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

package com.aliyun.pds.demo

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.aliyun.pds.demo.FileActivity.Companion.TAG
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.model.*
import kotlinx.android.synthetic.main.activity_file_list.*
import kotlin.concurrent.thread

fun Any.toJSONString(): String {
   return JSON.toJSONString(this)
}

class FileActivity : BaseActivity() {

    companion object {
        const val TAG = "FileActivity"
    }

    lateinit var gridlayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        gridlayoutManager = GridLayoutManager(this, 2)
        recyclerview.layoutManager = gridlayoutManager

        thread {
            val request = FileListRequest()
            request.parentId = "root"
            request.all = true
            request.driveId = "10460"
            request.fields = "*"
//            request.domainId = "daily14795"
            val resp = SDClient.instance.fileApi.fileList(request)
            runOnUiThread {
                if (resp?.items != null) {
                    recyclerview.adapter = GridAdapter(this, resp.items!!)
                }
            }
        }
    }
}


class GridAdapter(private val context: Context, private val dataList: ArrayList<FileInfoResp>) :
    RecyclerView.Adapter<FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewTypes: Int): FileViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view, viewTypes)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = dataList[position]
        holder.bindModel(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}


class FileViewHolder(itemView: View, var viewTypes: Int) : RecyclerView.ViewHolder(itemView) {

    var text: TextView? = null
    var image: ImageView? = null
    var more: View? = null

    fun bindModel(item: FileInfoResp) {
        image = itemView.findViewById(R.id.image)
        text = itemView.findViewById(R.id.text)
        more = itemView.findViewById(R.id.more)

        val imageRes = if (item.type == "file") {
            R.drawable.file

        } else if (item.type == "folder") {
            R.drawable.folder

        } else {
            R.drawable.unknown
        }
        image?.setImageResource(imageRes)
        text?.text = item.name
        more?.setOnClickListener{
            showListDialog(item)
        }
    }

    private fun showListDialog(item: FileInfoResp) {
        val items = arrayOf("拷贝", "移动", "详情", "删除", "更新")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
        listDialog.setTitle("操作")
        listDialog.setItems(items) { dialog, idx ->
           when (idx) {
               0 ->  {
                   val copyRequest = FileCopyRequest()
                   copyRequest.driveId = item.driveId!!
                   copyRequest.fileId = item.fileId!!
                   copyRequest.toDriveId = item.driveId!!
                   copyRequest.newName = "copy_test"
                   copyRequest.toParentId = "root"
                   thread {
                       val resp = SDClient.instance.fileApi.fileCopy(copyRequest)
                       Log.d(TAG, resp!!.toJSONString())
                   }
               }
               1 ->   {
                   val moveRequest = FileMoveRequest()
                   moveRequest.driveId = item.driveId!!
                   moveRequest.fileId = item.fileId!!
                   moveRequest.toDriveId = item.driveId!!
                   moveRequest.newName = "move_test"
                   moveRequest.toParentId = "root"
                   thread {
                       val resp = SDClient.instance.fileApi.fileMove(moveRequest)
                       Log.d(TAG, resp!!.toJSONString())
                   }

               }
               2 -> {
                   val getRequest = FileGetRequest()
                   getRequest.driveId = item.driveId
                   getRequest.fileId = item.fileId!!
                   getRequest.fields = "*"
                   thread {
                       val resp = SDClient.instance.fileApi.fileGet(getRequest)
                       Log.d(TAG, resp!!.toJSONString())
                   }

               }

               3 -> {
                   val delRequest = FileDeleteRequest()
                   delRequest.driveId = item.driveId!!
                   delRequest.fileId = item.fileId!!
                   thread {
                       val resp = SDClient.instance.fileApi.fileDelete(delRequest)
                       Log.d(TAG, resp!!.toJSONString())
                   }

               }
               4 -> {
                   val updateRequest = FileUpdateRequest()
                   updateRequest.driveId = item.driveId!!
                   updateRequest.fileId = item.fileId
                   updateRequest.name = "update_test"
                   thread {
                       val resp = SDClient.instance.fileApi.fileUpdate(updateRequest)
                       Log.d(TAG, resp!!.toJSONString())
                   }
               }
           }
        }
        listDialog.show()
    }
}