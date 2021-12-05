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

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.aliyun.pds.sdk.OnCompleteListener
import com.aliyun.pds.sdk.OnProgressListener
import com.aliyun.pds.sdk.SDTask
import com.aliyun.pds.sdk.download.SDDownloadTask
import com.aliyun.pds.sdk.upload.SDUploadTask


class TaskList(private val context: Context) {

    private val linearLayout: LinearLayout
    private val view: View = LayoutInflater.from(context).inflate(R.layout.task_list, null)

    init {
        linearLayout = view.findViewById(R.id.task_list)
    }

    fun addTask(task: SDTask) {
        val taskItem = TaskItem(context, task)
        taskItem.setDelClickListener {
            task.cancel()
            linearLayout.removeView(taskItem.taskView)
        }
        linearLayout.addView(taskItem.taskView)
    }

    fun getView(): View {
       return view
    }
}

class TaskItem(
    context: Context,
    private val task: SDTask,
) {

    var state = 0; // 0 running, 1 paused
    var progressBar: ProgressBar
    val taskView: View =
        LayoutInflater.from(context).inflate(R.layout.task_item, LinearLayout(context), false)
    private val delBtn: Button

    init {

        val actionBtn = taskView.findViewById<Button>(R.id.action_btn)
        delBtn = taskView.findViewById<Button>(R.id.delete_btn)

        val textView = taskView.findViewById<TextView>(R.id.name)
        val taskName = if (task is SDDownloadTask) {
            task.fileName
        } else if (task is SDUploadTask){
           task.fileName
        } else {
           ""
        }
        textView.text = taskName

        progressBar = taskView.findViewById<ProgressBar>(R.id.progressbar)

        actionBtn.setOnClickListener {
            state = if (0 == state) {
                task.pause()
                1

            } else {
                task.resume()
                0
            }
            if (0 == state) {
                actionBtn.text = "暂停"
            } else if (1 == state) {
                actionBtn.text = "继续"
            }
        }

//        delBtn.setOnClickListener(delListener)
        delBtn.setOnClickListener {
        }
        task.setOnProgressChangeListener(object : OnProgressListener {
            override fun onProgressChange(currentSize: Long) {
                Log.d("size => ", currentSize.toString())
                val p = (currentSize * 1000 / fileSize(task)).toInt()
                Log.d("progress => ", p.toString())
                progressBar.progress = p
            }
        })

        task.setOnCompleteListener(object : OnCompleteListener {
            override fun onComplete(info: Map<String, Any>, exception: Exception?) {
                Log.d("task", "完成")
                Log.d("task", info.toString())
                Log.d("task", exception.toString())
                exception?.printStackTrace()
                textView.post {
                    textView.text = info["fileName"].toString()
                }
            }
        })
    }

    fun setDelClickListener(listener: View.OnClickListener) {
       delBtn.setOnClickListener(listener)
    }

    fun fileSize(task: SDTask): Long {
        return if (task is SDDownloadTask) {
            task.fileSize
        } else if(task is SDUploadTask) {
            task.fileSize
        } else {
           0
        }
    }
}
