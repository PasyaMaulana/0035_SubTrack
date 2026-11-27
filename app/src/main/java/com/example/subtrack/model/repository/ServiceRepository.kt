package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.Category
import com.example.subtrack.model.dto.Service
import com.example.subtrack.model.dto.ServiceRequest
import com.example.subtrack.model.dto.UpdateServiceRequest
import com.example.subtrack.model.network.ServiceApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

// Kontrak repository katalog layanan yang dipakai ViewModel
interface ServiceRepository {
    // q dan category null = tanpa filter
    suspend fun daftarLayanan(q: String? = null, category: Category? = null): HasilApi<List<Service>>

    suspend fun detailLayanan(id: Int): HasilApi<Service>

    suspend fun tambahLayanan(request: ServiceRequest): HasilApi<Service>

    // Field null tidak diubah. websiteUrl = "" berarti URL dikosongkan.
    suspend fun ubahLayanan(id: Int, request: UpdateServiceRequest): HasilApi<Service>

    suspend fun hapusLayanan(id: Int): HasilApi<Unit>
}

class NetworkServiceRepository(
    private val serviceApi: ServiceApi,
    private val json: Json
) : ServiceRepository {

    override suspend fun daftarLayanan(q: String?, category: Category?): HasilApi<List<Service>> =
        panggilApi(json) { serviceApi.getServices(q = q?.trim()?.ifEmpty { null }, category = category) }

    override suspend fun detailLayanan(id: Int): HasilApi<Service> =
        panggilApi(json) { serviceApi.getService(id) }

    override suspend fun tambahLayanan(request: ServiceRequest): HasilApi<Service> =
        panggilApi(json) { serviceApi.createService(request) }

    override suspend fun ubahLayanan(id: Int, request: UpdateServiceRequest): HasilApi<Service> =
        panggilApi(json) { serviceApi.updateService(id, bodyUbah(request)) }

    override suspend fun hapusLayanan(id: Int): HasilApi<Unit> =
        panggilApiTanpaData(json) { serviceApi.deleteService(id) }

    // Server memvalidasi website_url sebagai URL, jadi "" ditolak. Untuk mengosongkan URL
    // harus dikirim null secara eksplisit, padahal Json (explicitNulls = false) membuang null.
    // Karena itu body diubah ke JsonObject lalu website_url diganti JsonNull.
    private fun bodyUbah(request: UpdateServiceRequest): JsonObject {
        val body = json.encodeToJsonElement(request).jsonObject
        return if (request.websiteUrl == "") JsonObject(body + (FIELD_WEBSITE to JsonNull)) else body
    }

    private companion object {
        const val FIELD_WEBSITE = "website_url"
    }
}
