package com.aliyun.pds.demo

import com.aliyun.pds.sdk.model.FileInfoResp

interface OnItemClickListener {
    /**
     * 复制
     */
    fun copy(item: FileInfoResp)

    /**
     * 移动
     */
    fun move(item: FileInfoResp)

    /**
     * 详情
     */
    fun details(item: FileInfoResp)

    /**
     * 删除
     */
    fun del(item: FileInfoResp)

    /**
     * 更新
     */
    fun update(item: FileInfoResp)

    /**
     * 下载
     */
    fun download(item: FileInfoResp)
}