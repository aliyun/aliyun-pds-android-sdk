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

/**
 */

package com.aliyun.pds.sdk

import com.aliyun.pds.sdk.thread.ThreadPoolUtils

interface SDTask {

    /**
     *  run task, if task is running nothing to do
     */
    fun start()

    /**
     *  clean is true will clean db and temp file,
     *  this means that when start is called again next time, it will start from the beginning,
     *  if you will continue transfer next time, set clean to false
     */
    fun stop(clean: Boolean = false)

    /**
     * task complete listener, success or failed; it will call non ui thread
     */
    fun setOnCompleteListener(listener: OnCompleteListener?)

    /**
     * task progress listener; it will call non ui thread
     */
    fun setOnProgressChangeListener(listener: OnProgressListener?)
}

abstract class SDBaseTask(val taskId: String) : SDTask {

    var progressListener: OnProgressListener? = null
    var completeListener: OnCompleteListener? = null

    internal enum class TaskState {
        RUNNING, FINISH
    }

    internal var state = TaskState.FINISH

    private var operation: Operation? = null


    override fun start() {
        ThreadPoolUtils.instance.taskHandlerThread.submit {
            if (state == TaskState.RUNNING) return@submit
            this.state = TaskState.RUNNING
            operation = createOperation()
            operation?.execute()
        }
    }


    override fun stop(clean: Boolean) {
        ThreadPoolUtils.instance.taskHandlerThread.submit {
            operation?.stop(clean)
            this.state = TaskState.FINISH
        }
    }

    /**
     *  create a operation UploadOperation or DownloadOperation
     */
    internal abstract fun createOperation(): Operation

    override fun setOnCompleteListener(listener: OnCompleteListener?) {
        completeListener = listener
    }


    override fun setOnProgressChangeListener(listener: OnProgressListener?) {
        progressListener = listener
    }
}