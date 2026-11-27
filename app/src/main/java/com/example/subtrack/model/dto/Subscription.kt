package com.example.subtrack.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Langganan milik user. Tanggal berformat YYYY-MM-DD, harga dalam rupiah.
@Serializable
data class Subscription(
    val id: Int,
    @SerialName("service_id") val serviceId: Int,
    @SerialName("plan_name") val planName: String,
    val price: Long,
    @SerialName("billing_cycle") val billingCycle: BillingCycle,
    @SerialName("start_date") val startDate: String,
    @SerialName("next_billing_date") val nextBillingDate: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val notes: String? = null,
    val service: ServiceSummary? = null
)

// Ringkasan langganan yang ikut di response grup
@Serializable
data class SubscriptionSummary(
    val id: Int,
    @SerialName("plan_name") val planName: String? = null,
    val price: Long? = null,
    @SerialName("billing_cycle") val billingCycle: BillingCycle? = null,
    val status: SubscriptionStatus? = null
)

// Body POST /subscriptions. next_billing_date boleh null agar dihitung server.
@Serializable
data class SubscriptionRequest(
    @SerialName("service_id") val serviceId: Int,
    @SerialName("plan_name") val planName: String,
    val price: Long,
    @SerialName("billing_cycle") val billingCycle: BillingCycle,
    @SerialName("start_date") val startDate: String,
    @SerialName("next_billing_date") val nextBillingDate: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val notes: String? = null
)

// Body PUT /subscriptions/:id. Semua field opsional: hanya field yang diisi (bukan null) yang dikirim.
@Serializable
data class UpdateSubscriptionRequest(
    @SerialName("service_id") val serviceId: Int? = null,
    @SerialName("plan_name") val planName: String? = null,
    val price: Long? = null,
    @SerialName("billing_cycle") val billingCycle: BillingCycle? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("next_billing_date") val nextBillingDate: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val status: SubscriptionStatus? = null,
    val notes: String? = null
)
