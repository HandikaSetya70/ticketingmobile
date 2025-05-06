package com.dicoding.ticketingsystem.Main.MyTickets

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dicoding.ticketingsystem.Main.MyTickets.Tickets.TicketsFragment
import com.dicoding.ticketingsystem.Main.MyTickets.Payments.PaymentsFragment

class HistoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TicketsFragment.newInstance()
            1 -> PaymentsFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}