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
    val standalone_tickets: List<TicketData>,
    val summary: TicketSummary,
    val user_info: UserInfo,
    val last_sync: String,
    val next_sync_recommended: String,
    val filters_applied: TicketFiltersApplied
)

data class TicketGroup(
    val parent: TicketData,
    val children: List<TicketData>,
    val total_in_group: Int,
    val group_summary: GroupSummary
)

data class GroupSummary(
    val total_paid: Double,
    val all_valid: Boolean,
    val event_name: String
)

data class TicketData(
    val ticket_id: String,
    val event: EventInfo,
    val ticket_info: TicketInfo,
    val purchase_info: PurchaseInfo,
    val qr_code: String?,
    val qr_data: String,
    val status: String,
    val validity: TicketValidity,
    val actions: TicketActions,
    val user_metadata: UserMetadata
)

data class EventInfo(
    val id: String,
    val name: String,
    val date: String,
    val venue: String,
    val description: String?,
    val category: String?,
    val price: Double,
    val image: String?,
    val image_thumb: String?
)

data class TicketInfo(
    val number: Int,
    val total_in_group: Int,
    val is_parent: Boolean,
    val parent_id: String?,
    val blockchain_id: String?,
    val nft_token_id: Long?
)

data class PurchaseInfo(
    val payment_id: String?,
    val amount_paid: Double,
    val purchase_date: String,
    val payment_status: String?
)

data class TicketValidity(
    val is_valid: Boolean,
    val is_upcoming: Boolean,
    val days_till_event: Int?,
    val can_be_used: Boolean,
    val last_checked: String,
    val blockchain_verified: Boolean
)

data class TicketActions(
    val can_transfer: Boolean,
    val can_refund: Boolean,
    val can_download: Boolean,
    val can_share: Boolean
)

data class UserMetadata(
    val auth_user_id: String,
    val internal_user_id: String,
    val user_email: String,
    val user_name: String,
    val verification_status: String
)

data class TicketSummary(
    val total: Int,
    val valid: Int,
    val revoked: Int,
    val used: Int,
    val upcoming_events: Int,
    val past_events: Int,
    val total_spent: Double,
    val events_count: Int
)

data class UserInfo(
    val auth_id: String,
    val internal_id: String,
    val email: String,
    val name: String,
    val verification_status: String,
    val auth_provider: String
)

data class TicketFiltersApplied(
    val status: String?,
    val event_id: String?,
    val upcoming_only: Boolean,
    val include_qr: Boolean
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

// Legacy Ticket class for backward compatibility
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
    val events: Event? = null,
    val payments: Payment? = null
)

data class Payment(
    val payment_id: String,
    val amount: String,
    val payment_status: String,
    val created_at: String? = null,
    val paypal_order_id: String? = null,
    val paypal_transaction_id: String? = null,
    val payment_method: String? = null
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