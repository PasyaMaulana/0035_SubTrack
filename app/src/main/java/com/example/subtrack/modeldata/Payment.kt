package com.example.subtrack.modeldata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Catatan pembayaran satu langganan
@Serializable
data class Payment(
    val id: Int,
    @SerialName("subscription_id") val subscriptionId: Int,
    val amount: Long,
    @SerialName("paid_at") val paidAt: String,
    val method: String? = null,
    @SerialName("proof_file_name") val proofFileName: String? = null,
    val note: String? = null
)

// data dari GET /subscriptions/:id/payments: { total, payments }
@Serializable
data class PaymentList(
    val total: Long = 0,
    val payments: List<Payment> = emptyList()
)

// Body POST/PUT pembayaran. proof_file_name = nama file foto di filesDir/bukti/
@Serializable
data class PaymentRequest(
    val amount: Long,
    @SerialName("paid_at") val paidAt: String,
    val method: String? = null,
    @SerialName("proof_file_name") val proofFileName: String? = null,
    val note: String? = null
)
