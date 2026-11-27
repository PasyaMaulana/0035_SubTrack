package com.example.subtrack.repositori

import com.example.subtrack.apiservice.AuthApi
import com.example.subtrack.modeldata.AuthData
import com.example.subtrack.modeldata.LoginRequest
import com.example.subtrack.modeldata.RegisterRequest
import com.example.subtrack.modeldata.User
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

// Kontrak repository autentikasi yang dipakai ViewModel
interface AuthRepository {
    // Sesi aktif; null berarti belum login / sesi dihapus
    val sesi: Flow<Sesi?>

    suspend fun login(email: String, password: String): HasilApi<User>

    suspend fun register(name: String, email: String, password: String): HasilApi<User>

    suspend fun logout(): HasilApi<Unit>
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

    // Login/register berhasil -> simpan token dan profil ke DataStore
    private suspend fun simpanJikaSukses(hasil: HasilApi<AuthData>): HasilApi<User> = when (hasil) {
        is HasilApi.Sukses -> {
            sessionManager.simpanSesi(hasil.data)
            HasilApi.Sukses(hasil.data.user, hasil.pesan)
        }
        is HasilApi.Gagal -> hasil
    }
}
