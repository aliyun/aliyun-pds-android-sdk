package com.aliyun.pds.demo.ui

class TaskModel(var taskId: String, var taskName: String, var size: Long, var state: TaskState = TaskState.RUNNING)

enum class TaskState{
    RUNNING,
    DONE,
    PAUSE,
    FAILE
}