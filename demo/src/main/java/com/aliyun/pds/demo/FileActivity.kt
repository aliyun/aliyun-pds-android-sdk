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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.model.*
import kotlinx.android.synthetic.main.activity_file_list.*
import kotlin.concurrent.thread

import android.os.Build

import java.io.*


fun Any.toJSONString(): String {
   return JSON.toJSONString(this)
}

class FileActivity : BaseActivity(), OnItemClickListener {

    companion object {
        const val TAG = "FileActivity"
    }

    private val REQUEST_COPY = 0x000001
    private val REQUEST_MOVE = 0x000002
    private val REQUEST_DEL = 0x000003
    private val REQUEST_UPDATE = 0x000004
    private val REQUEST_DETAILS = 0x000005

    private lateinit var gridlayoutManager: GridLayoutManager
    private lateinit var dataList: ArrayList<FileInfoResp>

    private val startUpload = registerForActivityResult(ResultContract()) {
        if (it != null) {
            upload(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchEdit.text.isNullOrEmpty()) {
                    fileSearch(searchEdit.text.toString())
                } else {
                    loadData()
                }
            }
            false
        }

        addBtn.setOnClickListener {
            showAddDialog()
        }

        gridlayoutManager = GridLayoutManager(this, 2)
        recyclerview.layoutManager = gridlayoutManager

        loadData()
    }

    /**
     * 创建文件夹
     */
    private fun creatFolder() {
        val createRequest = FileCreateRequest()
        createRequest.checkNameMode = "auto_rename"
        createRequest.driveId = Config.driveId
        createRequest.name = "NewFolder"
        createRequest.parentFileId = "root"
        createRequest.type = "folder"  //file: 文件  folder: 文件夹

        thread {
            val resp = SDClient.instance.fileApi.fileCreate(createRequest)
            if (resp!!.code == 201) {
                loadData()
            }
        }
    }

    /**
     * 上传文件
     */
    private fun upload(uri: Uri) {
        val file =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                FileUtil.uriToFileApiQ(this, uri)
            else
                FileUtil.uriToFileApi(this, uri)

        if (file != null) {
            TransferUtil.startUpload(this, file, object : OnUploadSuccessListener {
                override fun onUploadSuccess() {
                    loadData()
                }
            })
        }
    }

    /**
     * 获取文件列表
     */
    private fun loadData() {
        thread {
            val request = FileListRequest()
            request.parentId = "root"
            request.all = false
            request.driveId = Config.driveId
            request.fields = "*"
            val resp = SDClient.instance.fileApi.fileList(request)
            runOnUiThread {
                if (resp?.items != null) {
                    dataList = resp.items!!
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
            request.driveId = Config.driveId
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
     * 显示文件夹列表
     */
    private fun showFolderListDialog(title: String, item: FileInfoResp) {
        val folderList: ArrayList<String> = ArrayList()
        folderList.add("个人空间")
        for (data in dataList) {
            if (data.type == "folder" && data.fileId != item.fileId) {
                folderList.add(data.name!!)
            }
        }

        val listDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        listDialog.setTitle(title + "到")
        listDialog.setItems(folderList.toTypedArray()) { _, idx ->
            if (title == "拷贝") {
                val copyRequest = FileCopyRequest()
                copyRequest.driveId = item.driveId!!
                copyRequest.fileId = item.fileId!!
                copyRequest.toDriveId = if (idx == 0) Config.driveId else dataList[idx - 1].driveId
                copyRequest.newName = "copy_" + item.name!!
                copyRequest.toParentId = if (idx == 0) "root" else dataList[idx - 1].fileId!!

                request(REQUEST_COPY, copyRequest)
            } else if (title == "移动") {
                val moveRequest = FileMoveRequest()
                moveRequest.driveId = item.driveId!!
                moveRequest.fileId = item.fileId!!
                moveRequest.toDriveId = if (idx == 0) Config.driveId else dataList[idx - 1].driveId
                moveRequest.newName = item.name!!
                moveRequest.toParentId = if (idx == 0) "root" else dataList[idx - 1].fileId!!

                request(REQUEST_MOVE, moveRequest)
            }

        }
        listDialog.show()
    }

    /**
     * 显示文件详情
     */
    private fun showDetailsDialog(info: FileInfoResp) {
        Handler(Looper.getMainLooper()).post {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_file_details, null)
            view.findViewById<AppCompatTextView>(R.id.tvFileName).text = info.name

            val fileSize = if (info.fileSize == null) {
                    ""
                } else {
                    when {
                        info.fileSize!! > (1024 * 1024) -> {
                            (info.fileSize!! / (1024 * 1024)).toString() + "MB"
                        }
                        info.fileSize!! > 1024 -> {
                            (info.fileSize!! / 1024).toString() + "KB"
                        }
                        else -> {
                            info.fileSize.toString() + "B"
                        }
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
        getRequest.driveId = Config.driveId
        getRequest.fileId = item.fileId!!
        getRequest.fields = "*"

        request(REQUEST_DETAILS, getRequest)
    }

    override fun del(item: FileInfoResp) {
        val delRequest = FileDeleteRequest()
        delRequest.driveId = Config.driveId
        delRequest.fileId = item.fileId!!

        request(REQUEST_DEL, delRequest)
    }

    override fun update(item: FileInfoResp) {
        val updateRequest = FileUpdateRequest()
        updateRequest.driveId = Config.driveId
        updateRequest.fileId = item.fileId
        updateRequest.name = "update_" + item.name

        request(REQUEST_UPDATE, updateRequest)
    }

    override fun download(item: FileInfoResp) {
        val file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val downloadFilePath = File(file, item.name!!).path
        TransferUtil.startDownload(this, item, downloadFilePath)
    }

    /**
     * 操作接口
     */
    private fun request(requestType: Int, requestBody: Any) {
        thread {
            var resp = BaseResp()
            when (requestType) {
                REQUEST_COPY -> {
                    resp = SDClient.instance.fileApi.fileCopy(requestBody as FileCopyRequest)!!
                }

                REQUEST_MOVE -> {
                    resp = SDClient.instance.fileApi.fileMove(requestBody as FileMoveRequest)!!
                }

                REQUEST_DEL -> {
                    resp = SDClient.instance.fileApi.fileDelete(requestBody as FileDeleteRequest)!!
                }

                REQUEST_UPDATE -> {
                    resp = SDClient.instance.fileApi.fileUpdate(requestBody as FileUpdateRequest)!!
                }

                REQUEST_DETAILS -> {
                    resp = SDClient.instance.fileApi.fileGet(requestBody as FileGetRequest)!!
                }
            }
            Log.d(TAG, resp.toJSONString())
            if (requestType == REQUEST_DETAILS) {
                showDetailsDialog(resp as FileInfoResp)
            } else {
                loadData()
            }
        }
    }

    /**
     * 选择上传文件还是新建文件
     */
    private fun showAddDialog() {
        val items = arrayOf("上传文件", "新建文件夹")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        listDialog.setTitle("操作")
        listDialog.setItems(items) { _, idx ->
            when (idx) {
                0 ->  {
                    startUpload.launch("")
                }
                1 ->  {
                    creatFolder()
                }
            }
        }
        listDialog.show()
    }

}

class GridAdapter(private val context: Context, private val dataList: ArrayList<FileInfoResp>, listener: OnItemClickListener) :
    RecyclerView.Adapter<FileViewHolder>() {

    private val itemClickListener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewTypes: Int): FileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false)
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

    private var text: TextView? = null
    private var image: ImageView? = null
    private var more: View? = null

    fun bindModel(item: FileInfoResp, listener: OnItemClickListener) {
        image = itemView.findViewById(R.id.image)
        text = itemView.findViewById(R.id.text)
        more = itemView.findViewById(R.id.more)

        val imageRes = when (item.type) {
            "file" -> {
                R.drawable.file

            }
            "folder" -> {
                R.drawable.folder

            }
            else -> {
                R.drawable.unknown
            }
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
                        arrayOf("拷贝", "移动", "删除", "更新", "详情", "下载")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
        listDialog.setTitle("操作")
        listDialog.setItems(items) { _, idx ->
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
               5 -> {
                   listener.download(item)
               }
           }
        }
        listDialog.show()
    }
}

