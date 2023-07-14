package com.example.petdaycarekotandfire.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(list: ArrayList<Fragment>,fm: FragmentManager,lifecycle : Lifecycle): FragmentStateAdapter(fm,lifecycle){

    val fragmentlist : ArrayList<Fragment> = list
    override fun getItemCount(): Int {
        return fragmentlist.size
    }

    override fun createFragment(position: Int): Fragment {
        return  fragmentlist[position]
    }

}