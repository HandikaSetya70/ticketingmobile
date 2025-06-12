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
        binding.swipeRefresh.isRefreshing = false
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(errorMessage: String) {
        binding.swipeRefresh.isRefreshing = false
        binding.rvTickets.visibility = View.GONE
        binding.tvNoTickets.visibility = View.VISIBLE
        binding.tvNoTickets.text = "Error: $errorMessage"

        // Show toast for user feedback
        Toast.makeText(context, "Failed to load tickets: $errorMessage", Toast.LENGTH_LONG).show()
    }

    private fun showTickets(ticketsResponse: UserTicketsResponse) {
        binding.swipeRefresh.isRefreshing = false

        // Combine ticket groups and standalone tickets for display
        val allTicketGroups = mutableListOf<TicketGroup>()

        // Add existing ticket groups
        allTicketGroups.addAll(ticketsResponse.ticket_groups)

        // Convert standalone tickets to single-ticket groups for consistent display
        ticketsResponse.standalone_tickets.forEach { standaloneTicket ->
            val standaloneGroup = TicketGroup(
                parent = standaloneTicket,
                children = emptyList(),
                total_in_group = 1,
                group_summary = com.dicoding.ticketingsystem.DataSource.Response.GroupSummary(
                    total_paid = standaloneTicket.purchase_info.amount_paid,
                    all_valid = standaloneTicket.status == "valid",
                    event_name = standaloneTicket.event.name
                )
            )
            allTicketGroups.add(standaloneGroup)
        }

        // Show/hide no tickets message
        val hasTickets = allTicketGroups.isNotEmpty()
        binding.rvTickets.visibility = if (hasTickets) View.VISIBLE else View.GONE
        binding.tvNoTickets.visibility = if (hasTickets) View.GONE else View.VISIBLE

        if (hasTickets) {
            // Set up adapter with all ticket groups
            val adapter = TicketAdapter(allTicketGroups) { ticketGroup ->
                // Handle ticket click - navigate to details screen
                navigateToTicketDetails(ticketGroup)
            }
            binding.rvTickets.adapter = adapter

            // Show summary information
            showTicketSummary(ticketsResponse.summary)
        } else {
            binding.tvNoTickets.text = "No tickets found.\nPurchase some tickets to see them here!"
        }
    }

    private fun showTicketSummary(summary: com.dicoding.ticketingsystem.DataSource.Response.TicketSummary) {
        // You can add a summary view to show ticket statistics
        // For example, total spent, upcoming events count, etc.
        Log.d(TAG, "Ticket Summary - Total: ${summary.total}, Valid: ${summary.valid}, " +
                "Upcoming Events: ${summary.upcoming_events}, Total Spent: $${summary.total_spent}")
    }

    private fun navigateToTicketDetails(ticketGroup: TicketGroup) {
        // Create intent to navigate to TicketDetailsActivity
        val intent = Intent(context, TicketDetailsActivity::class.java)

        // Convert TicketGroup to JSON string using Gson
        val gson = Gson()
        val ticketGroupJson = gson.toJson(ticketGroup)

        // Pass the JSON string to the activity
        intent.putExtra(TicketDetailsActivity.EXTRA_TICKET_GROUP, ticketGroupJson)

        // Start the activity
        startActivity(intent)
    }

    @OptIn(UnstableApi::class)
    private fun loadTickets() {
        Log.d(TAG, "Loading tickets...")
        viewModel.loadUserTickets(
            // You can add filtering parameters here if needed
            status = null,           // Load all statuses
            eventId = null,          // Load all events
            groupByEvent = false     // Use the current grouping structure
        )
    }

    // Optional: Add method to refresh with specific filters
    fun loadTicketsWithFilter(status: String? = null, eventId: String? = null) {
        viewModel.loadUserTickets(
            status = status,
            eventId = eventId,
            groupByEvent = false
        )
    }

    // Optional: Add method to load only upcoming tickets
    fun loadUpcomingTickets() {
        // This would require modifying the API call to include upcoming_only parameter
        // For now, we can filter on the client side or add the parameter to the repository
        loadTickets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TicketsFragment()
    }
}