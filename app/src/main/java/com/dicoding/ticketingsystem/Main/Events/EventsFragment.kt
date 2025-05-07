package com.dicoding.ticketingsystem.Main.Events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.ticketingsystem.Main.Events.EventDetails.EventDetailsActivity
import com.dicoding.ticketingsystem.databinding.FragmentEventsBinding
import com.google.gson.Gson


class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        observeViewModel()

        // Load upcoming events by default
        viewModel.loadEvents(upcoming = true)
    }

    private fun setupAdapter() {
        eventsAdapter = EventsAdapter { event ->
            // Navigate to event details
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)

            // Convert Event to JSON string
            val gson = Gson()
            val eventJson = gson.toJson(event)

            // Pass the data to the activity
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT, eventJson)

            // Start the activity
            startActivity(intent)
        }

        binding.rvEvents.apply {
            // Create a custom layout manager that doesn't scroll
            val layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean {
                    return false // Disable vertical scrolling in the RecyclerView
                }
            }
            this.layoutManager = layoutManager
            this.adapter = eventsAdapter

            // Make sure nested scrolling is disabled
            this.isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    // Show loading state
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    // Update adapter with events - notice we access result.data correctly
                    eventsAdapter.submitList(result.data)
                }
                is Result.Error -> {
                    showLoading(false)
                    // Show error message
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // If you have a progress bar, update its visibility here
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}