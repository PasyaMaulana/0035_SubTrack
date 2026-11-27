package com.example.subtrack.di

import android.content.Context
import com.example.subtrack.BuildConfig
import com.example.subtrack.model.local.SessionManager
import com.example.subtrack.model.network.AuthApi
import com.example.subtrack.model.network.AuthInterceptor
import com.example.subtrack.model.network.DashboardApi
import com.example.subtrack.model.network.GroupApi
import com.example.subtrack.model.network.MemberApi
import com.example.subtrack.model.network.PaymentApi
import com.example.subtrack.model.network.ServiceApi
import com.example.subtrack.model.network.SubscriptionApi
import com.example.subtrack.model.network.TokenAuthenticator
import com.example.subtrack.model.repository.AuthRepository
import com.example.subtrack.model.repository.NetworkAuthRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

// Kontainer dependency (manual DI). Repository fitur akan ditambahkan di sini nanti.
interface AppContainer {
    val sessionManager: SessionManager
    val authApi: AuthApi
    val serviceApi: ServiceApi
    val subscriptionApi: SubscriptionApi
    val paymentApi: PaymentApi
    val groupApi: GroupApi
    val memberApi: MemberApi
    val dashboardApi: DashboardApi

    // Repository fitur
    val authRepository: AuthRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val sessionManager = SessionManager(context)

    // ignoreUnknownKeys: field tambahan dari server tidak membuat crash
    // encodeDefaults + explicitNulls=false: nilai default (mis. status ACTIVE) tetap dikirim,
    // sedangkan field opsional yang null tidak dikirim sama sekali
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    // Client dasar: timeout + logging (hanya di build debug)
    private val clientDasar: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_DETIK, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_DETIK, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_DETIK, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    private fun buatRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Retrofit khusus refresh token: tanpa AuthInterceptor dan tanpa authenticator
    private val refreshApi: AuthApi by lazy { buatRetrofit(clientDasar).create<AuthApi>() }

    // Client utama: tambah header Bearer dan refresh otomatis saat 401
    private val clientUtama: OkHttpClient by lazy {
        clientDasar.newBuilder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenAuthenticator(sessionManager, refreshApi))
            .build()
    }

    private val retrofit: Retrofit by lazy { buatRetrofit(clientUtama) }

    override val authApi: AuthApi by lazy { retrofit.create<AuthApi>() }
    override val serviceApi: ServiceApi by lazy { retrofit.create<ServiceApi>() }
    override val subscriptionApi: SubscriptionApi by lazy { retrofit.create<SubscriptionApi>() }
    override val paymentApi: PaymentApi by lazy { retrofit.create<PaymentApi>() }
    override val groupApi: GroupApi by lazy { retrofit.create<GroupApi>() }
    override val memberApi: MemberApi by lazy { retrofit.create<MemberApi>() }
    override val dashboardApi: DashboardApi by lazy { retrofit.create<DashboardApi>() }

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(authApi, sessionManager, json)
    }

    private companion object {
        const val TIMEOUT_DETIK = 30L
    }
}
