package com.aliyun.pds.demo.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TaskAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val TAB_TITLES = arrayOf(
        "上传",
        "下载"
    )

    override fun getItem(position: Int): Fragment {
        return TaskFragment.newInstance(TaskFragment.TAB_TYPES[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return TAB_TITLES[position]
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}