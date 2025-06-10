package com.dicoding.ticketingsystem.DataSource.Response

// Base response class that wraps API responses
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)

// ===== ENHANCED EVENTS RESPONSES =====

// Updated EventsResponse with pagination and filters
data class EventsResponse(
    val status: String,
    val message: String,
    val data: EventsData
)

data class EventsData(
    val events: List<Event>,
    val pagination: PaginationInfo? = null,
    val filters_applied: FiltersApplied? = null,
    val cached_until: String? = null
)

// Enhanced Event class with pricing and availability
data class Event(
    val id: String,                    // event_id (mapped from backend)
    val event_id: String? = null,      // Keep for backward compatibility
    val name: String,                  // event_name (mapped from backend)
    val event_name: String? = null,    // Keep for backward compatibility
    val date: String,                  // event_date
    val event_date: String? = null,    // Keep for backward compatibility
    val venue: String,
    val description: String?,          // event_description
    val event_description: String? = null, // Keep for backward compatibility
    val image: String?,                // event_image_url (mapped from backend)
    val event_image_url: String? = null, // Keep for backward compatibility
    val category: String?,

    // 🆕 NEW PRICING & AVAILABILITY FIELDS
    val price: Double = 0.0,           // ticket_price
    val total: Int = 0,                // total_tickets
    val available: Int = 0,            // available_tickets
    val sold: Int = 0,                 // calculated: total - available
    val sold_percentage: Int = 0,      // calculated percentage
    val status: String = "available",  // available/limited/sold_out
    val currency: String = "USD",      // currency code
    val is_sold_out: Boolean = false   // calculated boolean
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hasMore: Boolean,
    val hasPrev: Boolean
)

data class FiltersApplied(
    val upcoming: Boolean,
    val past: Boolean,
    val category: String? = null,
    val price_range: PriceRange? = null,
    val available_only: Boolean
)

data class PriceRange(
    val min: Double? = null,
    val max: Double? = null
)

// ===== ENHANCED USER TICKETS RESPONSES =====

// Enhanced UserTicketsResponse with new fields
data class UserTicketsResponse(
    val total_tickets: Int,
    val ticket_groups: List<TicketGroup>,
    val standalone_tickets: List<Ticket>,
    val summary: TicketSummary,

    // 🆕 NEW FIELDS FOR MOBILE OPTIMIZATION
    val last_sync: String? = null,
    val next_sync_recommended: String? = null,
    val filters_applied: TicketFiltersApplied? = null
)

// Enhanced TicketGroup with summary
data class TicketGroup(
    val parent: Ticket,
    val children: List<Ticket>,
    val total_in_group: Int,

    // 🆕 NEW GROUP SUMMARY
    val group_summary: GroupSummary? = null
)

data class GroupSummary(
    val total_paid: Double,
    val all_valid: Boolean,
    val event_name: String
)

// Enhanced Ticket class with mobile-optimized fields
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

    // Enhanced nested objects
    val events: Event? = null,          // Keep as Event (now enhanced)
    val payments: Payment? = null,

    // 🆕 NEW MOBILE-OPTIMIZED FIELDS
    val event: EventInfo? = null,       // Simplified event info for mobile
    val ticket_info: TicketInfo? = null,
    val purchase_info: PurchaseInfo? = null,
    val qr_code: String? = null,        // Base64 embedded QR code
    val qr_data: String? = null,        // QR data for scanning
    val status: String? = null,         // Alias for ticket_status
    val validity: TicketValidity? = null,
    val actions: TicketActions? = null
)

// Mobile-optimized event info
data class EventInfo(
    val id: String,
    val name: String,
    val date: String,
    val venue: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double = 0.0,
    val image: String? = null,
    val image_thumb: String? = null     // Mobile-optimized thumbnail
)

// Ticket-specific information
data class TicketInfo(
    val number: Int,
    val total_in_group: Int,
    val is_parent: Boolean,
    val parent_id: String? = null,
    val blockchain_id: String? = null,
    val nft_token_id: String? = null
)

// Purchase information
data class PurchaseInfo(
    val payment_id: String,
    val amount_paid: Double,
    val purchase_date: String,
    val payment_status: String
)

// Ticket validity information
data class TicketValidity(
    val is_valid: Boolean,
    val is_upcoming: Boolean,
    val days_till_event: Int? = null,
    val can_be_used: Boolean,
    val last_checked: String,
    val blockchain_verified: Boolean = false
)

// Available actions for ticket
data class TicketActions(
    val can_transfer: Boolean = false,
    val can_refund: Boolean = false,
    val can_download: Boolean = true,
    val can_share: Boolean = true
)

// Enhanced Payment class
data class Payment(
    val payment_id: String,
    val amount: String,
    val payment_status: String,

    // 🆕 NEW PAYMENT FIELDS
    val created_at: String? = null,
    val paypal_order_id: String? = null,
    val paypal_transaction_id: String? = null,
    val payment_method: String? = null
)

// Enhanced TicketSummary
data class TicketSummary(
    val total: Int,
    val valid: Int,
    val revoked: Int,
    val events_count: Int,

    // 🆕 NEW SUMMARY FIELDS
    val used: Int = 0,
    val upcoming_events: Int = 0,
    val past_events: Int = 0,
    val total_spent: Double = 0.0
)

// Filters applied to ticket request
data class TicketFiltersApplied(
    val status: String? = null,
    val event_id: String? = null,
    val upcoming_only: Boolean = false,
    val include_qr: Boolean = false
)

// ===== NEW PURCHASE FLOW RESPONSES =====

// Response for ticket purchase initiation
data class PurchaseResponse(
    val status: String,
    val message: String,
    val data: PurchaseData
)

data class PurchaseData(
    val purchase_id: String,
    val payment_id: String,
    val summary: PurchaseSummary,
    val payment: PaymentData,
    val reservation: ReservationData,
    val next_steps: List<String>? = null
)

data class PurchaseSummary(
    val event_id: String,
    val event_name: String,
    val event_date: String,
    val venue: String,
    val quantity: Int,
    val unit_price: Double,
    val total_amount: Double,
    val currency: String
)

data class PaymentData(
    val paypal_order_id: String,
    val checkout_url: String,
    val mobile_deep_links: MobileDeepLinks,
    val return_urls: ReturnUrls
)

data class MobileDeepLinks(
    val ios: String,
    val android: String,
    val fallback: String
)

data class ReturnUrls(
    val success: String,
    val cancel: String
)

data class ReservationData(
    val expires_at: String,
    val reservation_id: String,
    val tickets_reserved: Int
)

// ===== PAYMENT VERIFICATION RESPONSE =====

data class PaymentVerificationResponse(
    val status: String,
    val message: String,
    val data: PaymentVerificationData
)

data class PaymentVerificationData(
    val payment_id: String,
    val payment_status: String,
    val total_amount: Double,
    val currency: String,
    val created_at: String,
    val paypal_order_id: String? = null,
    val paypal_transaction_id: String? = null,
    val tickets_ready: Boolean = false,
    val tickets_count: Int = 0,
    val tickets: List<Ticket>? = null,
    val event_info: EventInfo? = null,
    val receipt: ReceiptInfo? = null,
    val push_notification: PushNotificationData? = null
)

data class ReceiptInfo(
    val receipt_id: String,
    val download_url: String
)

data class PushNotificationData(
    val title: String,
    val body: String,
    val data: Map<String, String>? = null
)

// ===== REQUEST MODELS =====

// Enhanced request for ticket purchase
data class PurchaseRequest(
    val event_id: String,
    val quantity: Int,
    val device_info: DeviceInfo? = null
)

data class DeviceInfo(
    val platform: String,      // "android" or "ios"
    val app_version: String,
    val device_id: String
)

// Request models for filtering tickets (keep existing)
data class UserTicketsRequest(
    val user_id: String? = null,
    val status: String? = null,
    val event_id: String? = null,
    val group_by_event: Boolean = false,
    val include_qr: Boolean = true,
    val upcoming_only: Boolean = false
)