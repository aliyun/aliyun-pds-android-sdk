package com.aliyun.pds.demo.ui

import com.aliyun.pds.sdk.SDTask


class TaskList {

    var taskMaps: MutableMap<String, SDTask> = HashMap()
    var uploadTasks = ArrayList<TaskModel>()
    var downloadTasks = ArrayList<TaskModel>()

        companion object {

        private var instance: TaskList? = null

        @JvmStatic
        fun newInstance(): TaskList {
            if (instance == null) {
                instance = TaskList()
            }
            return instance!!
        }
    }

    fun addUploadTask(taskModel: TaskModel, sdTask: SDTask) {
        uploadTasks.add(taskModel)
        taskMaps[taskModel.taskId] = sdTask
    }

    fun removeUploadTask(taskModel: TaskModel) {
        uploadTasks.remove(taskModel)
        taskMaps.remove(taskModel.taskId)
    }

    fun addDownloadTask(taskModel: TaskModel, sdTask: SDTask) {
        downloadTasks.add(taskModel)
        taskMaps[taskModel.taskId] = sdTask
    }

    fun removeDownloadTask(taskModel: TaskModel) {
        downloadTasks.remove(taskModel)
        taskMaps.remove(taskModel.taskId)
    }

}