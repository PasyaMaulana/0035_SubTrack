package com.example.subtrack.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Layanan di katalog (Netflix, Spotify, dll)
@Serializable
data class Service(
    val id: Int,
    val name: String,
    val category: Category,
    val description: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

// Ringkasan layanan yang ikut di response langganan dan grup: {id, name, category}
@Serializable
data class ServiceSummary(
    val id: Int,
    val name: String,
    val category: Category
)

// Body POST /services (khusus ADMIN)
@Serializable
data class ServiceRequest(
    val name: String,
    val category: Category,
    val description: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

// Body PUT /services/:id. Semua field opsional: hanya field yang diisi (bukan null) yang dikirim.
@Serializable
data class UpdateServiceRequest(
    val name: String? = null,
    val category: Category? = null,
    val description: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)
