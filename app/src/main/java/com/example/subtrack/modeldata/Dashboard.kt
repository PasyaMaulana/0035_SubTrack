package com.example.subtrack.modeldata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data dari GET /dashboard. Isinya berbeda per role:
// USER  -> subscriptions, spending, upcoming_bills, groups
// ADMIN -> totals, popular_services
// Karena itu semua bagian dibuat nullable.
@Serializable
data class DashboardData(
    val subscriptions: SubscriptionStats? = null,
    val spending: SpendingStats? = null,
    @SerialName("upcoming_bills") val upcomingBills: List<Subscription> = emptyList(),
    val groups: GroupStats? = null,
    val totals: AdminTotals? = null,
    @SerialName("popular_services") val popularServices: List<PopularService> = emptyList()
)

// --- Dashboard USER ---

@Serializable
data class SubscriptionStats(
    val total: Int = 0,
    val active: Int = 0,
    val paused: Int = 0,
    val cancelled: Int = 0
)

@Serializable
data class SpendingStats(
    @SerialName("monthly_estimate") val monthlyEstimate: Long = 0,
    @SerialName("paid_this_month") val paidThisMonth: Long = 0,
    @SerialName("by_category") val byCategory: List<CategorySpending> = emptyList()
)

// Satu baris pengeluaran per kategori
@Serializable
data class CategorySpending(
    val category: Category,
    val total: Long = 0
)

@Serializable
data class GroupStats(
    val total: Int = 0,
    @SerialName("unpaid_members") val unpaidMembers: Int = 0,
    @SerialName("outstanding_dues") val outstandingDues: Long = 0
)

// --- Dashboard ADMIN ---

@Serializable
data class AdminTotals(
    val users: Int = 0,
    val services: Int = 0,
    @SerialName("active_services") val activeServices: Int = 0,
    val subscriptions: Int = 0,
    val groups: Int = 0
)

// Layanan dengan pelanggan terbanyak
@Serializable
data class PopularService(
    val id: Int,
    val name: String,
    val category: Category,
    @SerialName("subscription_count") val subscriptionCount: Int = 0
)
