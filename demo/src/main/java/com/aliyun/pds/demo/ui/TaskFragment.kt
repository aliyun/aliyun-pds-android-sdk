package com.aliyun.pds.demo.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliyun.pds.demo.R
import com.aliyun.pds.demo.TransferUtil
import com.aliyun.pds.demo.databinding.FragmentTaskBinding
import com.aliyun.pds.sdk.*

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding
    private lateinit var mAdapter: TaskListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)

        val tasksView: RecyclerView = binding.recyclerView
        val type = arguments?.get(TAB_TYPE)

        tasksView.layoutManager = LinearLayoutManager(context)
        mAdapter = TaskListAdapter(context!!, type as Int)
        tasksView.adapter = mAdapter

        return binding.root
    }

    companion object {
        private const val TAB_TYPE = "tab_type"
        const val TAB_UPLOAD : Int = 1
        const val TAB_DOWNLOAD : Int = 2

        val TAB_TYPES = arrayOf(
            TAB_UPLOAD,
            TAB_DOWNLOAD
        )

        @JvmStatic
        fun newInstance(sectionNumber: Int): TaskFragment {
            return TaskFragment().apply {
                arguments = Bundle().apply {
                    putInt(TAB_TYPE, sectionNumber)
                }
            }
        }
    }
}

class TaskListAdapter(private val context: Context, private val tabType: Int) :
    RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewTypes: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = when (tabType) {
            TaskFragment.TAB_UPLOAD -> {
                TaskList.newInstance().uploadTasks[position]
            }
            TaskFragment.TAB_DOWNLOAD -> {
                TaskList.newInstance().downloadTasks[position]
            }
            else -> {
                null
            }
        }
        holder.bindModel(item, tabType)
    }

    override fun getItemCount(): Int {
        val count = when (tabType) {
            TaskFragment.TAB_UPLOAD -> {
                TaskList.newInstance().uploadTasks.size
            }
            TaskFragment.TAB_DOWNLOAD -> {
                TaskList.newInstance().downloadTasks.size
            }
            else -> {
                0
            }
        }
        return count
    }

}

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var nameTv: TextView
    private lateinit var proBar: ProgressBar
    private lateinit var actionBtn: Button
    private lateinit var delBtn: Button

    fun bindModel(item: TaskModel?, tabType: Int) {
        nameTv = itemView.findViewById(R.id.name)
        proBar = itemView.findViewById(R.id.progressbar)
        actionBtn = itemView.findViewById(R.id.action_btn)
        delBtn = itemView.findViewById(R.id.delete_btn)


        if (item != null) {
            nameTv.text = item.taskName
            proBar.max = item.size.toInt()

            when(item.state) {
                TaskState.RUNNING -> {
                    actionBtn.tag = 0
                }
                TaskState.PAUSE -> {
                    actionBtn.tag = 1
                    actionBtn.text = "继续"
                }
                TaskState.DONE -> {
                    nameTv.text = "${item.taskName} (已完成)"
                    proBar.progress = proBar.max
                    actionBtn.visibility = View.INVISIBLE
                }
                TaskState.FAILE -> {
                    nameTv.text = "${item.taskName} (任务失败)"
                    actionBtn.text = "重试"
                }
            }

            var task = TaskList.newInstance().taskMaps[item.taskId]!!
            task.setOnProgressChangeListener(object : OnProgressListener {
                override fun onProgressChange(currentSize: Long) {
                    Log.e("TaskFragment", "============>$currentSize")
                    proBar.progress = currentSize.toInt()
                }
            })

            task.setOnCompleteListener(object : OnCompleteListener {
                override fun onComplete(taskId: String, fileMeta: SDFileMeta, errorInfo: SDErrorInfo?) {
                    Log.e(TransferUtil.TAG, "============>完成")
                    if (errorInfo!!.code == SDTransferError.None) {
                        item.state = TaskState.DONE
                        actionBtn.visibility = View.INVISIBLE
                        nameTv.post {
                            nameTv.text = "${item.taskName} (已完成)"
                        }
                    } else {
                        item.state = TaskState.FAILE

                        nameTv.post {
                            nameTv.text = "${item.taskName} (任务失败)"
                        }

                        actionBtn.post {
                            actionBtn.text = "重试"
                        }
                    }
                }
            })

            actionBtn.setOnClickListener {
                actionBtn.tag = if (item.state == TaskState.RUNNING) {
                    TaskList.newInstance().taskMaps[item.taskId]!!.stop(false)
                    item.state = TaskState.PAUSE

                } else {
                    TaskList.newInstance().taskMaps[item.taskId]!!.start()
                    item.state = TaskState.RUNNING
                }
                if (item.state == TaskState.RUNNING) {
                    actionBtn.text = "暂停"
                } else if (item.state == TaskState.PAUSE) {
                    actionBtn.text = "继续"
                }
            }

            delBtn.setOnClickListener {
                TaskList.newInstance().taskMaps[item.taskId]!!.stop(true)
                if (tabType == TaskFragment.TAB_UPLOAD) {
                    TaskList.newInstance().removeUploadTask(item)
                } else if (tabType == TaskFragment.TAB_DOWNLOAD) {
                    TaskList.newInstance().removeDownloadTask(item)
                }
            }
        }
    }
}