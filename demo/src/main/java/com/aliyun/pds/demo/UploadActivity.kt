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
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aliyun.pds.sdk.SDClient
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File


class UploadActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        title = "上传"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        val taskList = TaskList(this)
        val contentView = findViewById<LinearLayout>(R.id.body)
        contentView.addView(taskList.getView())

        text.text = "上传测试需要设置 上传目录parentId，driveId，上传文件默认为下载的测试文件要传指定文件请指定文件路径"
        findViewById<Button>(R.id.choice_file).setOnClickListener {

            var file = this.getExternalFilesDir(null)
            file = File(file, "Download")
            file = File(file, "edmDrive-0.7.0-mac.dmg")
            if (!file.exists()) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // 请填写自己的parentId
            val parentId = "root"
            // 请填写自己的driveId
            val driveId = BuildConfig.driveId
            val mimeType = "mimeType"
//            val shareId = "shareId"
            val task = SDClient.instance.createUploadTask(
                "3",
                "edmDrive",
                file.absolutePath,
                file.length(),
                parentId,
                mimeType,
                driveId,
            )
            taskList.addTask(task)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
