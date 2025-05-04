package com.dicoding.ticketingsystem.Main.MyTickets.Confirmed

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dicoding.ticketingsystem.R

class ConfirmedFragment : Fragment() {

    companion object {
        fun newInstance() = ConfirmedFragment()
    }

    private val viewModel: ConfirmedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_confirmed, container, false)
    }
}