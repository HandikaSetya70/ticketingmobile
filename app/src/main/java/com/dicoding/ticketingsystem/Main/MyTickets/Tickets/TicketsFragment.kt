package com.dicoding.ticketingsystem.Main.MyTickets.Tickets

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
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

        // Set up swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadTickets()
        }

        // Set up observers
        setupObservers()

        // Load tickets on start
        loadTickets()
    }

    private fun setupObservers() {
        viewModel.tickets.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    showTickets(result.data)
                }
                is Result.Loading -> {
                }
                is Result.Error -> {

                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadTickets() {
        Log.d(TAG, "Loading tickets...")
        viewModel.loadUserTickets()
    }

    @OptIn(UnstableApi::class)
    private fun showTickets(ticketsResponse: UserTicketsResponse) {
//        binding.progressBar.visibility = View.GONE
//        binding.tvError.visibility = View.GONE
//        binding.btnRetry.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false

        // For debugging, log all ticket details
        Log.d(TAG, "Total tickets: ${ticketsResponse.total_tickets}")
        Log.d(TAG, "Ticket summary: ${ticketsResponse.summary}")

        ticketsResponse.ticket_groups.forEach { group ->
            Log.d(TAG, "Ticket group with ${group.total_in_group} tickets:")
            Log.d(TAG, "Parent ticket: ${group.parent.ticket_id} for event: ${group.parent.events?.event_name}")
            group.children.forEach { child ->
                Log.d(TAG, "  Child ticket: ${child.ticket_id}, status: ${child.ticket_status}")
            }
        }

        ticketsResponse.standalone_tickets.forEach { ticket ->
            Log.d(TAG, "Standalone ticket: ${ticket.ticket_id} for event: ${ticket.events?.event_name}")
        }

        // Here you would normally set up RecyclerView adapters,
        // but for this test we're just logging data
        binding.tvNoTickets.visibility =
            if (ticketsResponse.total_tickets == 0) View.VISIBLE else View.GONE

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