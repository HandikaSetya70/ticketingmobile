package com.dicoding.ticketingsystem.DataSource.Repository

import com.dicoding.ticketingsystem.DataSource.Response.ConnectWalletRequest
import com.dicoding.ticketingsystem.DataSource.Response.WalletDetailResponse
import com.dicoding.ticketingsystem.DataSource.Response.WalletResponse
import com.dicoding.ticketingsystem.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository {
    private val apiService = ApiConfig.getApiService()

    suspend fun connectWallet(
        walletAddress: String,
        signature: String,
        message: String? = null
    ): Result<WalletResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ConnectWalletRequest(walletAddress, signature, message)
                val response = apiService.connectWallet(request)

                if (response.status == "success") {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWallets(
        walletId: String? = null,
        detailed: Boolean = true,
        includeNftCount: Boolean = true
    ): Result<List<WalletDetailResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWallets(walletId, detailed, includeNftCount)

                if (response.status == "success") {
                    // Handle both single wallet and multiple wallets response
                    val wallets = response.data.wallets
                    Result.success(wallets)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}