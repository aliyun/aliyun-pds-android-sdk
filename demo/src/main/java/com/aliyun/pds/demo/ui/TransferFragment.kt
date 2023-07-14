package com.aliyun.pds.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.aliyun.pds.demo.databinding.FragmentTransferBinding
import com.google.android.material.tabs.TabLayout

class TransferFragment : Fragment() {

    private lateinit var binding: FragmentTransferBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransferBinding.inflate(inflater, container, false)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = TaskAdapter(childFragmentManager)
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        return binding.root
    }
}