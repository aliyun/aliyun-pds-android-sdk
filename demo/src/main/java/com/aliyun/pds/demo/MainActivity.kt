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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.aliyun.pds.sdk.SDToken
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.SDConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val reqCode = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accessToken = Config.accessToken
        val apiHost = Config.apiHost

        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(apiHost)) {
            Toast.makeText(this, "请填写你的token和apiHost", Toast.LENGTH_LONG).show()
        }

        val token = SDToken(accessToken)
        val file = this.getExternalFilesDir(null)
        val dir = File(file, "PDSDemo")
        dir.mkdirs()

        val config = SDConfig(token, 3600, apiHost, canFastUpload = false)
        SDClient.instance.init(this, config)

        download.setOnClickListener {
            val intent = Intent(this, DownloadActivity::class.java)
            startActivity(intent)
        }
        upload.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

        fileapi.setOnClickListener {

            val intent = Intent(this, FileActivity::class.java)
            startActivity(intent)
        }

       requestPermission()
    }


    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                return
            }
           else -> {
               requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                   reqCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            reqCode -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    Toast.makeText(this, "请给与文件访问权限", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

}