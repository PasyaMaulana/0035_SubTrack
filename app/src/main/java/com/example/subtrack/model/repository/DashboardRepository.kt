package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.DashboardData
import com.example.subtrack.model.network.DashboardApi
import kotlinx.serialization.json.Json

// Kontrak repository dashboard. Isi data berbeda per role (USER atau ADMIN), ditentukan server dari token.
interface DashboardRepository {
    // hari = rentang hari ke depan untuk daftar tagihan terdekat (khusus USER)
    suspend fun ringkasan(hari: Int): HasilApi<DashboardData>
}

class NetworkDashboardRepository(
    private val dashboardApi: DashboardApi,
    private val json: Json
) : DashboardRepository {

    override suspend fun ringkasan(hari: Int): HasilApi<DashboardData> =
        panggilApi(json) { dashboardApi.getDashboard(hari) }
}
