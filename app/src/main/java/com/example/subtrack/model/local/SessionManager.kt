package com.example.subtrack.model.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.subtrack.model.dto.AuthData
import com.example.subtrack.model.dto.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Satu file DataStore untuk seluruh aplikasi
private val Context.dataStoreSesi: DataStore<Preferences> by preferencesDataStore(name = "sesi")

// Data sesi user yang sedang login
data class Sesi(
    val accessToken: String,
    val refreshToken: String,
    val role: Role,
    val nama: String,
    val email: String
)

// Menyimpan token dan profil singkat user di DataStore Preferences
class SessionManager(context: Context) {

    private val dataStore = context.applicationContext.dataStoreSesi

    private object Kunci {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val ROLE = stringPreferencesKey("role")
        val NAMA = stringPreferencesKey("nama")
        val EMAIL = stringPreferencesKey("email")
    }

    // Sesi aktif, null jika belum login atau sesi sudah dihapus
    val sesi: Flow<Sesi?> = dataStore.data.map { pref ->
        val access = pref[Kunci.ACCESS_TOKEN]
        val refresh = pref[Kunci.REFRESH_TOKEN]
        if (access.isNullOrEmpty() || refresh.isNullOrEmpty()) {
            null
        } else {
            Sesi(
                accessToken = access,
                refreshToken = refresh,
                role = Role.entries.firstOrNull { it.name == pref[Kunci.ROLE] } ?: Role.USER,
                nama = pref[Kunci.NAMA].orEmpty(),
                email = pref[Kunci.EMAIL].orEmpty()
            )
        }
    }

    // Ambil sesi saat ini sekali (dipakai interceptor dan authenticator)
    suspend fun sesiSaatIni(): Sesi? = sesi.first()

    // Simpan sesi lengkap setelah login atau register
    suspend fun simpanSesi(data: AuthData) {
        dataStore.edit { pref ->
            pref[Kunci.ACCESS_TOKEN] = data.accessToken
            pref[Kunci.REFRESH_TOKEN] = data.refreshToken
            pref[Kunci.ROLE] = data.user.role.name
            pref[Kunci.NAMA] = data.user.name
            pref[Kunci.EMAIL] = data.user.email
        }
    }

    // Simpan pasangan token baru hasil refresh (refresh token dirotasi)
    suspend fun simpanToken(accessToken: String, refreshToken: String) {
        dataStore.edit { pref ->
            pref[Kunci.ACCESS_TOKEN] = accessToken
            pref[Kunci.REFRESH_TOKEN] = refreshToken
        }
    }

    // Hapus semua data sesi (logout atau refresh gagal)
    suspend fun hapusSesi() {
        dataStore.edit { it.clear() }
    }
}
