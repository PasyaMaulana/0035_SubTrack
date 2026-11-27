package com.example.subtrack.model.network

import com.example.subtrack.model.dto.ApiResponse
import com.example.subtrack.model.dto.AuthData
import com.example.subtrack.model.dto.LoginRequest
import com.example.subtrack.model.dto.RefreshRequest
import com.example.subtrack.model.dto.RegisterRequest
import com.example.subtrack.model.dto.TokenData
import com.example.subtrack.model.dto.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Endpoint autentikasi. register, login, refresh tidak butuh token.
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<AuthData>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<AuthData>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiResponse<TokenData>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @GET("auth/me")
    suspend fun me(): ApiResponse<User>
}
