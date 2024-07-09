package com.faberapps.mishi.ui.main

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.faberapps.mishi.fragment.CouponFragment
import com.faberapps.mishi.fragment.MapFragment
import com.faberapps.mishi.fragment.QrFragment


class SectionsAdapter(fa : FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> QrFragment()
            1 -> CouponFragment()
            else -> MapFragment()
        }
    }
}