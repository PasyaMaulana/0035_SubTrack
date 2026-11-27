package com.example.subtrack.modeldata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Grup patungan untuk berbagi biaya langganan
@Serializable
data class Group(
    val id: Int,
    @SerialName("service_id") val serviceId: Int,
    @SerialName("subscription_id") val subscriptionId: Int? = null,
    val name: String,
    @SerialName("total_cost") val totalCost: Long,
    @SerialName("billing_cycle") val billingCycle: BillingCycle,
    @SerialName("due_day") val dueDay: Int,
    @SerialName("max_members") val maxMembers: Int,
    val description: String? = null,
    val service: ServiceSummary? = null,
    val subscription: SubscriptionSummary? = null,
    val summary: GroupSummary = GroupSummary()
)

// Ringkasan iuran grup yang dihitung server
@Serializable
data class GroupSummary(
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("paid_count") val paidCount: Int = 0,
    @SerialName("suggested_share") val suggestedShare: Long = 0,
    val collected: Long = 0,
    val remaining: Long = 0
)

// Body POST /groups. due_day 1-28, max_members 2-20.
@Serializable
data class GroupRequest(
    @SerialName("service_id") val serviceId: Int,
    @SerialName("subscription_id") val subscriptionId: Int? = null,
    val name: String,
    @SerialName("total_cost") val totalCost: Long,
    @SerialName("billing_cycle") val billingCycle: BillingCycle,
    @SerialName("due_day") val dueDay: Int,
    @SerialName("max_members") val maxMembers: Int,
    val description: String? = null
)

// Body PUT /groups/:id. Semua field opsional: hanya field yang diisi (bukan null) yang dikirim.
@Serializable
data class UpdateGroupRequest(
    @SerialName("service_id") val serviceId: Int? = null,
    @SerialName("subscription_id") val subscriptionId: Int? = null,
    val name: String? = null,
    @SerialName("total_cost") val totalCost: Long? = null,
    @SerialName("billing_cycle") val billingCycle: BillingCycle? = null,
    @SerialName("due_day") val dueDay: Int? = null,
    @SerialName("max_members") val maxMembers: Int? = null,
    val description: String? = null
)
