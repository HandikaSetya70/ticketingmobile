package com.dicoding.ticketingsystem.Main.MyTickets

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.FragmentTicketsBinding

class TicketsFragment : Fragment() {

    companion object {
        fun newInstance() = TicketsFragment()
    }

    private val viewModel: TicketsViewModel by viewModels()
    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: TicketsPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        pagerAdapter = TicketsPagerAdapter(requireActivity())
        binding.viewPager.adapter = pagerAdapter

        // Add page change callback to update button states
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPageSelected(position: Int) {
                updateButtonStates(position)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupButtons() {
        binding.buttonConfirmed.setOnClickListener {
            binding.viewPager.currentItem = 0
        }

        binding.buttonPending.setOnClickListener {
            binding.viewPager.currentItem = 1
        }

        // Set initial button state
        updateButtonStates(0)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateButtonStates(position: Int) {
        when (position) {
            0 -> {
                // Confirmed selected
                binding.buttonConfirmed.apply {
                    setBackgroundResource(R.drawable.custom_button_orange)
                    setTextColor(resources.getColor(R.color.white, null))
                }
                binding.buttonPending.apply {
                    setBackgroundResource(R.drawable.custom_button_no_border)
                    setTextColor(resources.getColor(R.color.black, null))
                }
            }
            1 -> {
                // Pending selected
                binding.buttonPending.apply {
                    setBackgroundResource(R.drawable.custom_button_orange)
                    setTextColor(resources.getColor(R.color.white, null))
                }
                binding.buttonConfirmed.apply {
                    setBackgroundResource(R.drawable.custom_button_no_border)
                    setTextColor(resources.getColor(R.color.black, null))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}