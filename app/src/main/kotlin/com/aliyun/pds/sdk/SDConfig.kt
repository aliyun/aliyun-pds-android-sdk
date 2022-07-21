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

package com.aliyun.pds.sdk

import java.util.concurrent.TimeUnit

class SDConfig {

    var token: SDToken
    var apiHost: String
    var canFastUpload: Boolean
    var downloadUrlExpiredTime: Long
    var userAgent: String?
    var maxRetryCount: Int
    var isDebug: Boolean
    var databaseName: String

    var downloadBlockSize = 1024 * 1024 * 10L
    var uploadBlockSize = 1024 * 1024 * 4L
    var connectTimeout = 15L
    var connectTimeUnnit = TimeUnit.SECONDS
    var readTimeout = 60L
    var readTimeUnit = TimeUnit.SECONDS
    var writeTimeout = 60L
    var writeTimeUnit = TimeUnit.SECONDS

    val downloadMaxBlockCount = 100
    val uploadMaxBlockCount = 1000
    val uploadDir = "pds/upload"
    val downloadDir = "pds/download"

    @Deprecated("recommended use 'SDConfig.Builder(token, apiHost, downloadUrlExpiredTime).build()'")
    constructor(
        token: SDToken,
        downloadUrlExpiredTime: Long,
        apiHost: String,
        userAgent: String? = null,
        maxRetryCount: Int = 3,
        canFastUpload: Boolean = true,
        databaseName: String = "pds_transfer.db",
        isDebug: Boolean = false
    ) {
        this.token = token
        this.apiHost = apiHost
        this.canFastUpload = canFastUpload
        this.downloadUrlExpiredTime = downloadUrlExpiredTime
        this.userAgent = userAgent
        this.maxRetryCount = maxRetryCount
        this.canFastUpload = canFastUpload
        this.databaseName = databaseName
        this.isDebug = isDebug
    }

    constructor(builder: Builder) {
        this.token = builder.token
        this.apiHost = builder.apiHost
        this.canFastUpload = builder.canFastUpload
        this.downloadUrlExpiredTime = builder.downloadUrlExpiredTime
        this.userAgent = builder.userAgent
        this.maxRetryCount = builder.maxRetryCount
        this.isDebug = builder.isDebug
        this.databaseName = builder.databaseName

        this.downloadBlockSize = builder.downloadBlockSize
        this.uploadBlockSize = builder.uploadBlockSize
        this.connectTimeout = builder.connectTimeout
        this.connectTimeUnnit = builder.connectTimeUnnit
        this.readTimeout = builder.readTimeout
        this.readTimeUnit = builder.readTimeUnit
        this.writeTimeout = builder.writeTimeout
        this.writeTimeUnit = builder.writeTimeUnit
    }

    class Builder(val token: SDToken, val apiHost: String, val downloadUrlExpiredTime: Long) {
        var canFastUpload = true
        var userAgent = ""
        var maxRetryCount = 3
        var isDebug = false
        var databaseName = "pds_transfer.db"

        val downloadMiniBlockSize = 1024 * 1024 * 1L
        val uploadMiniBlockSize = 1024 * 1024 * 1L
        var downloadBlockSize = 1024 * 1024 * 10L
        var uploadBlockSize = 1024 * 1024 * 4L
        var connectTimeout = 15L
        var connectTimeUnnit = TimeUnit.SECONDS
        var readTimeout = 60L
        var readTimeUnit = TimeUnit.SECONDS
        var writeTimeout = 60L
        var writeTimeUnit = TimeUnit.SECONDS

        fun canFastUpload(canFastUpload: Boolean): Builder = apply {
            this.canFastUpload = canFastUpload
        }

        fun userAgent(userAgent: String): Builder = apply {
            this.userAgent = userAgent
        }

        fun maxRetryCount(maxRetryCount: Int): Builder = apply {
            this.maxRetryCount = maxRetryCount
        }

        fun isDebug(isDebug: Boolean): Builder = apply {
            this.isDebug = isDebug
        }

        fun databaseName(databaseName: String): Builder = apply {
            this.databaseName = databaseName
        }

        fun downloadBlockSize(blockSize: Long): Builder = apply {
            downloadBlockSize = if (blockSize < downloadMiniBlockSize) downloadMiniBlockSize else blockSize
        }

        fun uploadBlockSize(blockSize: Long): Builder = apply {
            uploadBlockSize = if (blockSize < uploadMiniBlockSize) uploadMiniBlockSize else blockSize
        }

        fun connectTimeout(timeout: Long): Builder = apply {
            connectTimeout = timeout
        }

        fun connectTimeUnnit(timeUnit: TimeUnit): Builder = apply {
            connectTimeUnnit = timeUnit
        }

        fun readTimeout(timeout: Long): Builder = apply {
            readTimeout = timeout
        }

        fun readTimeUnit(timeUnit: TimeUnit): Builder = apply {
            readTimeUnit = timeUnit
        }

        fun writeTimeout(timeout: Long): Builder = apply {
            readTimeout = timeout
        }

        fun writeTimeUnit(timeUnit: TimeUnit): Builder = apply {
            readTimeUnit = timeUnit
        }

        fun build(): SDConfig {
            return SDConfig(this)
        }
    }

}