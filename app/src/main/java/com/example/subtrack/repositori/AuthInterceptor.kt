package com.example.subtrack.repositori

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

// Endpoint yang tidak memakai token
private val ENDPOINT_PUBLIK = listOf("auth/register", "auth/login", "auth/refresh")

// true jika request menuju endpoint register, login, atau refresh
fun Request.isEndpointPublik(): Boolean {
    val path = url.encodedPath
    return ENDPOINT_PUBLIK.any { path.endsWith(it) }
}

// Menambahkan header "Authorization: Bearer <accessToken>" ke setiap request yang butuh token
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.isEndpointPublik()) return chain.proceed(request)

        // OkHttp berjalan di thread background, jadi runBlocking aman di sini
        val token = runBlocking { sessionManager.sesiSaatIni()?.accessToken }
        if (token == null) return chain.proceed(request)

        val requestDenganToken = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(requestDenganToken)
    }
}
