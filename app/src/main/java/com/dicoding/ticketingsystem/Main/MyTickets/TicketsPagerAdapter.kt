package com.dicoding.ticketingsystem.Main.MyTickets

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dicoding.ticketingsystem.Main.MyTickets.Confirmed.ConfirmedFragment
import com.dicoding.ticketingsystem.Main.MyTickets.Pending.PendingFragment

class TicketsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ConfirmedFragment.newInstance()
            1 -> PendingFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}