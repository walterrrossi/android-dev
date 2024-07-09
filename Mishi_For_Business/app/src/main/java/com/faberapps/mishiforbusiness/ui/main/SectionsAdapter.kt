package com.faberapps.mishiforbusiness.ui.main

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.faberapps.mishiforbusiness.fragment.CouponFragment
import com.faberapps.mishiforbusiness.fragment.QrFragment


class SectionsAdapter(fa : FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> QrFragment()
            else -> CouponFragment()
        }
    }
}