package com.dicoding.ticketingsystem.Main.Profile

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.dicoding.ticketingsystem.Main.Profile.ProfileDetails.ProfileDetailsActivity
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.StarterActivity
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())

        // Set up logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.layoutProfile.setOnClickListener {
            profileDetails()
        }
    }

    private fun profileDetails() {
        val intent = Intent(requireActivity(), ProfileDetailsActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        // Show a confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Clear session data
                lifecycleScope.launch {
                    sessionManager.clearSession()

                    // Navigate to StarterActivity
                    val intent = Intent(requireActivity(), StarterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}