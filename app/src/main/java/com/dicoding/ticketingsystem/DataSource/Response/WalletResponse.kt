package com.dicoding.ticketingsystem.DataSource.Response

data class ConnectWalletRequest(
    val wallet_address: String,
    val signature: String,
    val message: String? = null
)

data class WalletResponse(
    val wallet_id: String,
    val wallet_address: String
)

data class WalletsResponse(
    val wallets: List<WalletDetailResponse>
)

data class WalletDetailResponse(
    val wallet_id: String,
    val wallet_address: String,
    val linked_at: String,
    val expires_at: String?,
    val is_active: Boolean,
    val last_signed: String?,
    val nft_count: Int?
)