package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.AuthData
import com.example.subtrack.model.dto.LoginRequest
import com.example.subtrack.model.dto.RegisterRequest
import com.example.subtrack.model.dto.User
import com.example.subtrack.model.local.Sesi
import com.example.subtrack.model.local.SessionManager
import com.example.subtrack.model.network.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

// Kontrak repository autentikasi yang dipakai ViewModel
interface AuthRepository {
    // Sesi aktif; null berarti belum login / sesi dihapus
    val sesi: Flow<Sesi?>

    suspend fun login(email: String, password: String): HasilApi<User>

    suspend fun register(name: String, email: String, password: String): HasilApi<User>

    suspend fun logout(): HasilApi<Unit>

    // Data akun yang sedang login dari GET /auth/me (nama, email, role, created_at)
    suspend fun profil(): HasilApi<User>
}

class NetworkAuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val json: Json
) : AuthRepository {

    override val sesi: Flow<Sesi?> = sessionManager.sesi

    override suspend fun login(email: String, password: String): HasilApi<User> =
        simpanJikaSukses(panggilApi(json) { authApi.login(LoginRequest(email, password)) })

    override suspend fun register(name: String, email: String, password: String): HasilApi<User> =
        simpanJikaSukses(panggilApi(json) { authApi.register(RegisterRequest(name, email, password)) })

    // Sesi lokal selalu dihapus, walaupun request logout ke server gagal (mis. offline)
    override suspend fun logout(): HasilApi<Unit> {
        panggilApiTanpaData(json) { authApi.logout() }
        sessionManager.hapusSesi()
        return HasilApi.Sukses(Unit)
    }

    override suspend fun profil(): HasilApi<User> =
        panggilApi(json) { authApi.me() }

    // Login/register berhasil -> simpan token dan profil ke DataStore
    private suspend fun simpanJikaSukses(hasil: HasilApi<AuthData>): HasilApi<User> = when (hasil) {
        is HasilApi.Sukses -> {
            sessionManager.simpanSesi(hasil.data)
            HasilApi.Sukses(hasil.data.user, hasil.pesan)
        }
        is HasilApi.Gagal -> hasil
    }
}
