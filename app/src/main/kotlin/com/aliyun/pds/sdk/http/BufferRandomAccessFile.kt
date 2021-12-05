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

package com.aliyun.pds.sdk.http

import java.io.*


class BufferRandomAccessFile internal constructor(file: File?) {
    private val out: BufferedOutputStream
    private val fd: FileDescriptor
    private val randomAccess: RandomAccessFile = RandomAccessFile(file, "rw")

    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int) {
        out.write(b, off, len)
    }

    @Throws(IOException::class)
    fun flushAndSync() {
        out.flush()
        fd.sync()
    }

    @Throws(IOException::class)
    fun close() {
        out.close()
        randomAccess.close()
    }

    @Throws(IOException::class)
    fun seek(offset: Long) {
        randomAccess.seek(offset)
    }

    @Throws(IOException::class)
    fun setLength(totalBytes: Long) {
        randomAccess.setLength(totalBytes)
    }

    init {
        fd = randomAccess.fd
        out = BufferedOutputStream(FileOutputStream(randomAccess.fd))
    }
}