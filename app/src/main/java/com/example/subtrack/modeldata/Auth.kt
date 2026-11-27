package com.example.subtrack.modeldata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data akun yang sedang login
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: Role = Role.USER,
    @SerialName("created_at") val createdAt: String? = null
)

// Body POST /auth/register
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Body POST /auth/login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// Body POST /auth/refresh (nama field camelCase sesuai kontrak API)
@Serializable
data class RefreshRequest(
    val refreshToken: String
)

// data dari register dan login
@Serializable
data class AuthData(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

// data dari refresh: pasangan token baru (refresh token dirotasi)
@Serializable
data class TokenData(
    val accessToken: String,
    val refreshToken: String
)
