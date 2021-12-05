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

package com.aliyun.pds.sdk.thread

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class ThreadPoolUtils {

    companion object {
        val instance = ThreadPoolUtils()
    }

    val downloadTaskPool: ThreadPoolExecutor
//    val downloadConcurrentPool: ThreadPoolExecutor
    val taskHandlerThread: ExecutorService = Executors.newSingleThreadExecutor()
    val uploadTaskPool: ThreadPoolExecutor
    private val maxTaskCount = 32

    init {
        downloadTaskPool = ThreadPoolWrap(
            maxTaskCount,
            maxTaskCount,
            "download-task"
        ).pool

//        downloadConcurrentPool = ThreadPoolWrap(
//            4, 4 * maxTaskCount, "download-concurrent").pool

        uploadTaskPool = ThreadPoolWrap(
            maxTaskCount,
            maxTaskCount,
            "upload-task"
        ).pool

    }
}

class ThreadPoolWrap(coreSize: Int, maximumPoolSize: Int, name: String, blockingqueue: BlockingQueue<Runnable> = LinkedBlockingDeque()) {
    var id = AtomicInteger(0)
    var pool: ThreadPoolExecutor = ThreadPoolExecutor(
        coreSize,
        maximumPoolSize,
        30,
        TimeUnit.SECONDS,
        blockingqueue,
        ThreadFactory {
            val thread = Thread(it)
            thread.isDaemon = false
            thread.name = name + id.getAndIncrement()
            thread
        })
}
