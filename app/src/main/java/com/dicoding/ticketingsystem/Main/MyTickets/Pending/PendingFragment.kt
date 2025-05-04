package com.dicoding.ticketingsystem.Main.MyTickets.Pending

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dicoding.ticketingsystem.R

class PendingFragment : Fragment() {

    companion object {
        fun newInstance() = PendingFragment()
    }

    private val viewModel: PendingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }
}