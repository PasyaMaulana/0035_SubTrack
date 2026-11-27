package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.Payment
import com.example.subtrack.modeldata.PaymentList
import com.example.subtrack.modeldata.PaymentRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Riwayat pembayaran, nested di bawah satu langganan
interface PaymentApi {
    @GET("subscriptions/{subscriptionId}/payments")
    suspend fun getPayments(@Path("subscriptionId") subscriptionId: Int): ApiResponse<PaymentList>

    @GET("subscriptions/{subscriptionId}/payments/{paymentId}")
    suspend fun getPayment(
        @Path("subscriptionId") subscriptionId: Int,
        @Path("paymentId") paymentId: Int
    ): ApiResponse<Payment>

    @POST("subscriptions/{subscriptionId}/payments")
    suspend fun createPayment(
        @Path("subscriptionId") subscriptionId: Int,
        @Body body: PaymentRequest
    ): ApiResponse<Payment>

    @PUT("subscriptions/{subscriptionId}/payments/{paymentId}")
    suspend fun updatePayment(
        @Path("subscriptionId") subscriptionId: Int,
        @Path("paymentId") paymentId: Int,
        @Body body: PaymentRequest
    ): ApiResponse<Payment>

    @DELETE("subscriptions/{subscriptionId}/payments/{paymentId}")
    suspend fun deletePayment(
        @Path("subscriptionId") subscriptionId: Int,
        @Path("paymentId") paymentId: Int
    ): ApiResponse<Unit>
}
