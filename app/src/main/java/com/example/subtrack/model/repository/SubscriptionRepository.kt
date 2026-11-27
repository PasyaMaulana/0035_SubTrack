package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.dto.SubscriptionRequest
import com.example.subtrack.model.dto.SubscriptionStatus
import com.example.subtrack.model.dto.UpdateSubscriptionRequest
import com.example.subtrack.model.network.SubscriptionApi
import kotlinx.serialization.json.Json

// Kontrak repository langganan milik user yang login
interface SubscriptionRepository {
    // status dan q null = tanpa filter. q mencari berdasarkan nama layanan.
    suspend fun daftarLangganan(status: SubscriptionStatus? = null, q: String? = null): HasilApi<List<Subscription>>

    suspend fun detailLangganan(id: Int): HasilApi<Subscription>

    suspend fun tambahLangganan(request: SubscriptionRequest): HasilApi<Subscription>

    // Field null tidak diubah; untuk mengosongkan teks kirim ""
    suspend fun ubahLangganan(id: Int, request: UpdateSubscriptionRequest): HasilApi<Subscription>

    // Riwayat pembayaran ikut terhapus di server (CASCADE)
    suspend fun hapusLangganan(id: Int): HasilApi<Unit>
}

class NetworkSubscriptionRepository(
    private val subscriptionApi: SubscriptionApi,
    private val json: Json
) : SubscriptionRepository {

    override suspend fun daftarLangganan(status: SubscriptionStatus?, q: String?): HasilApi<List<Subscription>> =
        panggilApi(json) {
            subscriptionApi.getSubscriptions(status = status, q = q?.trim()?.ifEmpty { null })
        }

    override suspend fun detailLangganan(id: Int): HasilApi<Subscription> =
        panggilApi(json) { subscriptionApi.getSubscription(id) }

    override suspend fun tambahLangganan(request: SubscriptionRequest): HasilApi<Subscription> =
        panggilApi(json) { subscriptionApi.createSubscription(request) }

    override suspend fun ubahLangganan(id: Int, request: UpdateSubscriptionRequest): HasilApi<Subscription> =
        panggilApi(json) { subscriptionApi.updateSubscription(id, request) }

    override suspend fun hapusLangganan(id: Int): HasilApi<Unit> =
        panggilApiTanpaData(json) { subscriptionApi.deleteSubscription(id) }
}
