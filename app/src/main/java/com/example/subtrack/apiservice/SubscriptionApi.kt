package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.Subscription
import com.example.subtrack.modeldata.SubscriptionRequest
import com.example.subtrack.modeldata.SubscriptionStatus
import com.example.subtrack.modeldata.UpdateSubscriptionRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// CRUD langganan milik user yang login
interface SubscriptionApi {
    @GET("subscriptions")
    suspend fun getSubscriptions(
        @Query("status") status: SubscriptionStatus? = null,
        @Query("q") q: String? = null
    ): ApiResponse<List<Subscription>>

    @GET("subscriptions/{id}")
    suspend fun getSubscription(@Path("id") id: Int): ApiResponse<Subscription>

    @POST("subscriptions")
    suspend fun createSubscription(@Body body: SubscriptionRequest): ApiResponse<Subscription>

    @PUT("subscriptions/{id}")
    suspend fun updateSubscription(
        @Path("id") id: Int,
        @Body body: UpdateSubscriptionRequest
    ): ApiResponse<Subscription>

    @DELETE("subscriptions/{id}")
    suspend fun deleteSubscription(@Path("id") id: Int): ApiResponse<Unit>
}
