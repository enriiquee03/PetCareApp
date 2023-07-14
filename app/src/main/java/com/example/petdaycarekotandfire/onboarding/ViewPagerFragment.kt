package com.example.petdaycarekotandfire.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.petdaycarekotandfire.R
import com.example.petdaycarekotandfire.onboarding.screens.FirstFragment
import com.example.petdaycarekotandfire.onboarding.screens.SecondFragment
import com.example.petdaycarekotandfire.onboarding.screens.ThirdFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_view_pager, container, false)


        val fragmentList = arrayListOf<Fragment>(
            FirstFragment(),
            SecondFragment(),
            ThirdFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList,requireActivity().supportFragmentManager,lifecycle)
        val tablayout = view.findViewById<TabLayout>(R.id.tab_layout)

        view.findViewById<ViewPager2>(R.id.viewPager).adapter = adapter
        TabLayoutMediator(tablayout, view.findViewById<ViewPager2>(R.id.viewPager)) { tab, position ->

        }.attach()



        return view
    }

}