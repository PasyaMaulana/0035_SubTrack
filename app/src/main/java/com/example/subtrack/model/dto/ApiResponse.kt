package com.example.subtrack.model.dto

import kotlinx.serialization.Serializable

// Bungkus response standar backend: { success, message, data, errors }
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null,
    val errors: List<ErrorDetail>? = null
)

// Detail error validasi per field, contoh { field: "email", message: "Email sudah dipakai" }
@Serializable
data class ErrorDetail(
    val field: String? = null,
    val message: String = ""
)
