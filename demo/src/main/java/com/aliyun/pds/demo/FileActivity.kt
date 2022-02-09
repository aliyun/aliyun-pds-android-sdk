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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.model.*
import kotlinx.android.synthetic.main.activity_file_list.*
import kotlin.concurrent.thread

fun Any.toJSONString(): String {
   return JSON.toJSONString(this)
}

class FileActivity : BaseActivity(), OnItemClickListener {

    companion object {
        const val TAG = "FileActivity"
    }

    lateinit var gridlayoutManager: GridLayoutManager
    lateinit var dataList: ArrayList<FileInfoResp>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        searchEdit.setOnEditorActionListener {v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchEdit.text.isNullOrEmpty()) {
                    fileSearch(searchEdit.text.toString())
                } else {
                    loadData()
                }
                true
            }
            false
        }

        gridlayoutManager = GridLayoutManager(this, 2)
        recyclerview.layoutManager = gridlayoutManager

        loadData()
    }

    private fun loadData() {
        thread {
            val request = FileListRequest()
            request.parentId = "root"
            request.all = true
            request.driveId = BuildConfig.driveId
            request.fields = "*"
            val resp = SDClient.instance.fileApi.fileList(request)
            runOnUiThread {
                if (resp?.items != null) {
                    dataList = resp?.items!!
                    recyclerview.adapter = GridAdapter(this, dataList, this)
                }
            }
        }
    }

    /**
     * 搜索文件
     */
    private fun fileSearch(keyStr: String) {
        thread {
            val request = FileSearchRequest()
            request.query = "name match '$keyStr' and status = 'available'"
            request.driveId = BuildConfig.driveId
            request.fields = "*"

            val resp = SDClient.instance.fileApi.fileSearch(request)
            Log.e("HX", "code: " + resp?.code)
            Log.e("HX", "msg: " + resp?.errorMessage)
            runOnUiThread {
                if (resp?.items != null) {
                    recyclerview.adapter = GridAdapter(this, resp.items!!, this)
                }
            }
        }
    }

    /**
     * 文件夹列表
     */
    private fun showFolderListDialog(title: String, item: FileInfoResp) {
        var folderList: ArrayList<String> = ArrayList()
        folderList.add("我的网盘")
        for (data in dataList) {
            if (data.type == "folder" && data.fileId != item.fileId) {
                folderList.add(data.name!!)
            }
        }

        val listDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        listDialog.setTitle(title + "到")
        listDialog.setItems(folderList.toTypedArray()) { dialog, idx ->
            if (title == "拷贝") {
                val copyRequest = FileCopyRequest()
                copyRequest.driveId = item.driveId!!
                copyRequest.fileId = item.fileId!!
                copyRequest.toDriveId = item.driveId!!
                copyRequest.newName = "copy_" + item.name!!
                copyRequest.toParentId = if (idx == 0) "root" else dataList.get(idx - 1).fileId!!
                thread {
                    val resp = SDClient.instance.fileApi.fileCopy(copyRequest)
                    Log.d(TAG, resp!!.toJSONString())
                    loadData()
                }
            } else if (title == "移动") {
                val moveRequest = FileMoveRequest()
                moveRequest.driveId = item.driveId!!
                moveRequest.fileId = item.fileId!!
                moveRequest.toDriveId = item.driveId!!
                moveRequest.newName = item.name!!
                moveRequest.toParentId = dataList.get(idx).fileId!!
                thread {
                    val resp = SDClient.instance.fileApi.fileMove(moveRequest)
                    Log.d(TAG, resp!!.toJSONString())
                    loadData()
                }
            }

        }
        listDialog.show()
    }

    /**
     * 文件详情
     */
    private fun showDetailsDialog(info: FileInfoResp) {
        Handler(Looper.getMainLooper()).post {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_file_details, null)
            view.findViewById<AppCompatTextView>(R.id.tvFileName).text = info.name

            var fileSize: String
            if (info.fileSize == null) {
                fileSize = ""
            } else {
                if (info.fileSize!! > (1024 * 1024)) {
                    fileSize = (info.fileSize!! / (1024 * 1024)).toString() + "MB"
                } else if (info.fileSize!! > 1024) {
                    fileSize = (info.fileSize!! / 1024).toString() + "KB"
                } else {
                    fileSize = info.fileSize.toString() + "B"
                }
            }
            view.findViewById<AppCompatTextView>(R.id.tvFileSize).text = fileSize

            val detailsDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            detailsDialog.setTitle("文件详情")
            detailsDialog.setView(view)
            detailsDialog.show()
        }
    }

    override fun copy(item: FileInfoResp) {
        showFolderListDialog("拷贝", item)
    }

    override fun move(item: FileInfoResp) {
        showFolderListDialog("移动", item)
    }

    override fun details(item: FileInfoResp) {
        val getRequest = FileGetRequest()
        getRequest.driveId = item.driveId
        getRequest.fileId = item.fileId!!
        getRequest.fields = "*"
        thread {
            val resp = SDClient.instance.fileApi.fileGet(getRequest)
            showDetailsDialog(resp!!)
            Log.d(TAG, resp!!.toJSONString())
        }
    }

    override fun del(item: FileInfoResp) {
        val delRequest = FileDeleteRequest()
        delRequest.driveId = item.driveId!!
        delRequest.fileId = item.fileId!!
        thread {
            val resp = SDClient.instance.fileApi.fileDelete(delRequest)
            Log.d(TAG, resp!!.toJSONString())
            loadData()
        }
    }

    override fun update(item: FileInfoResp) {
        val updateRequest = FileUpdateRequest()
        updateRequest.driveId = item.driveId!!
        updateRequest.fileId = item.fileId
        updateRequest.name = "update_" + item.name
        thread {
            val resp = SDClient.instance.fileApi.fileUpdate(updateRequest)
            Log.d(TAG, resp!!.toJSONString())
            loadData()
        }
    }
}


class GridAdapter(private val context: Context, private val dataList: ArrayList<FileInfoResp>, listener: OnItemClickListener) :
    RecyclerView.Adapter<FileViewHolder>() {

    private val itemClickListener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewTypes: Int): FileViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view, viewTypes)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = dataList[position]
        holder.bindModel(item, itemClickListener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}


class FileViewHolder(itemView: View, var viewTypes: Int) : RecyclerView.ViewHolder(itemView) {

    var text: TextView? = null
    var image: ImageView? = null
    var more: View? = null

    fun bindModel(item: FileInfoResp, listener: OnItemClickListener) {
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
            showListDialog(item, listener)
        }
    }

    private fun showListDialog(item: FileInfoResp, listener: OnItemClickListener) {
        val items = if (item.type == "folder")
                        arrayOf("拷贝", "移动", "删除", "更新")
                    else
                        arrayOf("拷贝", "移动", "删除", "更新", "详情")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
        listDialog.setTitle("操作")
        listDialog.setItems(items) { dialog, idx ->
           when (idx) {
               0 ->  {
                   listener.copy(item)
               }
               1 ->  {
                   listener.move(item)
               }
               2 -> {
                   listener.del(item)
               }
               3 -> {
                   listener.update(item)
               }
               4 -> {
                   listener.details(item)
               }
           }
        }
        listDialog.show()
    }
}

interface OnItemClickListener {
    fun copy(item: FileInfoResp)
    fun move(item: FileInfoResp)
    fun details(item: FileInfoResp)
    fun del(item: FileInfoResp)
    fun update(item: FileInfoResp)
}