package com.dicoding.ticketingsystem.Main.MyTickets.Tickets

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.ticketingsystem.DataSource.Response.TicketGroup
import com.dicoding.ticketingsystem.DataSource.Response.UserTicketsResponse
import com.dicoding.ticketingsystem.Main.MyTickets.TicketDetails.TicketDetailsActivity
import com.dicoding.ticketingsystem.databinding.FragmentTicketsBinding
import com.google.gson.Gson

class TicketsFragment : Fragment() {
    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TicketsViewModel by viewModels()
    private val TAG = "TicketsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.rvTickets.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        // Set up swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadTickets()
        }

        // Show loading initially
        showLoading(true)

        // Set up observers
        setupObservers()

        // Load tickets on start
        loadTickets()
    }

    private fun setupObservers() {
        viewModel.tickets.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    showLoading(false)
                    showTickets(result.data)
                }
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Error -> {
                    showLoading(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(errorMessage: String) {
        binding.swipeRefresh.isRefreshing = false
        binding.rvTickets.visibility = View.GONE
        binding.tvNoTickets.visibility = View.VISIBLE
        binding.tvNoTickets.text = "Error: $errorMessage"
    }

    private fun showTickets(ticketsResponse: UserTicketsResponse) {
        binding.swipeRefresh.isRefreshing = false

        // Show/hide no tickets message
        val hasTickets = ticketsResponse.ticket_groups.isNotEmpty()
        binding.rvTickets.visibility = if (hasTickets) View.VISIBLE else View.GONE
        binding.tvNoTickets.visibility = if (hasTickets) View.GONE else View.VISIBLE

        if (hasTickets) {
            // Set up adapter with ticket groups
            val adapter = TicketAdapter(ticketsResponse.ticket_groups) { ticketGroup ->
                // Handle ticket click - navigate to details screen
                navigateToTicketDetails(ticketGroup)
            }
            binding.rvTickets.adapter = adapter
        }
    }

    private fun navigateToTicketDetails(ticketGroup: TicketGroup) {
        // Create intent to navigate to TicketDetailsActivity
        val intent = Intent(context, TicketDetailsActivity::class.java)

        // Convert TicketGroup to Parcelable or serialize it
        // Using a simple approach with Gson for serialization
        val gson = Gson()
        val ticketGroupJson = gson.toJson(ticketGroup)

        // Pass the data to the activity
        intent.putExtra(TicketDetailsActivity.EXTRA_TICKET_GROUP, ticketGroupJson)

        // Start the activity
        startActivity(intent)
    }

    @OptIn(UnstableApi::class)
    private fun loadTickets() {
        Log.d(TAG, "Loading tickets...")
        viewModel.loadUserTickets()
    }



//    private fun showLoading() {
//        binding.progressBar.visibility = View.VISIBLE
//        binding.tvError.visibility = View.GONE
//        binding.btnRetry.visibility = View.GONE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TicketsFragment()
    }
}