package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.AuthData
import com.example.subtrack.modeldata.LoginRequest
import com.example.subtrack.modeldata.RefreshRequest
import com.example.subtrack.modeldata.RegisterRequest
import com.example.subtrack.modeldata.TokenData
import com.example.subtrack.modeldata.User
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
