package com.example.subtrack.model.network

import com.example.subtrack.model.dto.ApiResponse
import com.example.subtrack.model.dto.DashboardData
import retrofit2.http.GET
import retrofit2.http.Query

// Ringkasan dashboard. days = rentang hari untuk daftar tagihan terdekat.
interface DashboardApi {
    @GET("dashboard")
    suspend fun getDashboard(@Query("days") days: Int = 14): ApiResponse<DashboardData>
}
