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

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aliyun.pds.sdk.SDClient
import kotlinx.android.synthetic.main.activity_download.*
import java.io.File

class DownloadActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_download)
        title = "下载"
        val contentView = findViewById<LinearLayout>(R.id.body)
        val taskList = TaskList(this)
        contentView.addView(taskList.getView())
        download_test.setOnClickListener {
            val url =
                "https://statics.aliyunpds.com/download/edm/desktop/0.7.0/edmDrive-0.7.0-mac.dmg"
            val fileId = "fileId"
            val driveId = "driveId"
            val fileName = "edmDrive-0.7.0-mac.dmg"
            val fileSize = 114499672L
            val file = this.getExternalFilesDir(null)
            val dir = File(file, "Download")
            Log.d("dir", dir.absolutePath)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val task = SDClient.instance.createDownloadTask(
                "1" ,
                url,
                fileId,
                driveId,
                fileName,
                fileSize,
                dir.path,
                null,
                null,
                null
            )
            taskList.addTask(task)
        }

        add_task.setOnClickListener {
            // 请在这里填写上你的 task 信息

//            val url =
//                "https://bj943.api.aliyunfile.com/v2/redirect?id=c16c8e6c620049af844ce5748223e073"
            val url = ""
            val fileId = "618d1193e9436d4571db46ce916bfb593bffc040"
            val driveId = "10460"
            val fileName = "IMG_3004.JPG"
            val fileSize = 403656L
            val file = this.getExternalFilesDir(null)
            val dir = File(file, "Download")
            Log.d("dir", dir.absolutePath)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val task = SDClient.instance.createDownloadTask(
                "2",
                url,
                fileId,
                driveId,
                fileName,
                fileSize,
                dir.path,
                null,
                "8913932804751030850",
                null
            )

            taskList.addTask(task)

//            Toast.makeText(this, "请填写你的下载任务信息", Toast.LENGTH_LONG).show()
        }
    }
}
