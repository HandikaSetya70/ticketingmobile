package com.dicoding.ticketingsystem.DataSource.Response

// Base response class that wraps API responses
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)

// Main response class for user tickets
data class UserTicketsResponse(
    val total_tickets: Int,
    val ticket_groups: List<TicketGroup>,
    val standalone_tickets: List<Ticket>,
    val summary: TicketSummary
)

// Class representing a group of tickets (parent + children)
data class TicketGroup(
    val parent: Ticket,
    val children: List<Ticket>,
    val total_in_group: Int
)

// Class for individual ticket data
data class Ticket(
    val ticket_id: String,
    val user_id: String,
    val event_id: String?,
    val purchase_date: String,
    val ticket_status: String,
    val blockchain_ticket_id: String,
    val qr_code_hash: String,
    val payment_id: String,
    val is_parent_ticket: Boolean,
    val parent_ticket_id: String?,
    val ticket_number: Int,
    val total_tickets_in_group: Int,
    val events: Event?,
    val payments: Payment?
)

// Class for event data
data class Event(
    val event_id: String,
    val event_name: String,
    val event_date: String,
    val venue: String,
    val category: String?,
    val event_description: String?,
    val event_image_url: String?
)

// Class for payment data
data class Payment(
    val payment_id: String,
    val amount: String,
    val payment_status: String
)

// Summary information about tickets
data class TicketSummary(
    val total: Int,
    val valid: Int,
    val revoked: Int,
    val events_count: Int
)

// Request models for filtering tickets
data class UserTicketsRequest(
    val user_id: String? = null,
    val status: String? = null,
    val event_id: String? = null,
    val group_by_event: Boolean = false
)