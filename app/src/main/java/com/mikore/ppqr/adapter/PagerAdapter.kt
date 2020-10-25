package com.mikore.ppqr.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mikore.ppqr.fragment.AccountFragment
import com.mikore.ppqr.fragment.HistoryFragment

class PagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 1) HistoryFragment() else AccountFragment()

}