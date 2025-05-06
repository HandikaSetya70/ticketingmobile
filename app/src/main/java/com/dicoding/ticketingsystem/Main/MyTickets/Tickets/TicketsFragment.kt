package com.dicoding.ticketingsystem.Main.MyTickets.Tickets

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
import com.dicoding.ticketingsystem.databinding.FragmentTicketsBinding

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
        // Implementation for navigating to ticket details
        // For example:
        // val action = TicketsFragmentDirections.actionTicketsFragmentToTicketDetailsFragment(ticketGroup.parent.ticket_id)
        // findNavController().navigate(action)
        Toast.makeText(context, "Clicked on ticket: ${ticketGroup.parent.events?.event_name}", Toast.LENGTH_SHORT).show()
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