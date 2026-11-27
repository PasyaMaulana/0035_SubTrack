package com.example.subtrack.model.network

import com.example.subtrack.model.dto.RefreshRequest
import com.example.subtrack.model.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException

// Dipanggil OkHttp saat server membalas 401 (access token kedaluwarsa).
// refreshApi berasal dari Retrofit terpisah TANPA authenticator, supaya tidak terjadi loop.
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val refreshApi: AuthApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 401 dari login/register/refresh berarti kredensial salah, bukan token habis
        if (response.request.isEndpointPublik()) return null

        // Batas percobaan: jika request ini sudah pernah diulang, berhenti
        if (jumlahPercobaan(response) >= BATAS_PERCOBAAN) return null

        // synchronized: jika beberapa request 401 bersamaan, refresh hanya dilakukan sekali
        synchronized(this) {
            val sesi = runBlocking { sessionManager.sesiSaatIni() } ?: return null

            // Token sudah diperbarui oleh request lain, cukup ulang dengan token terbaru
            val tokenLama = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (tokenLama != sesi.accessToken) {
                return response.request.denganToken(sesi.accessToken)
            }

            return try {
                val hasil = runBlocking { refreshApi.refresh(RefreshRequest(sesi.refreshToken)) }
                val token = hasil.data
                if (hasil.success && token != null) {
                    runBlocking { sessionManager.simpanToken(token.accessToken, token.refreshToken) }
                    response.request.denganToken(token.accessToken)
                } else {
                    runBlocking { sessionManager.hapusSesi() }
                    null
                }
            } catch (e: HttpException) {
                // Refresh token ditolak server (kedaluwarsa/dicabut) -> sesi dihapus, kembali ke login
                runBlocking { sessionManager.hapusSesi() }
                null
            } catch (e: IOException) {
                // Gangguan jaringan: sesi dipertahankan, error ditampilkan oleh layar
                null
            } catch (e: Exception) {
                // Error tak terduga (mis. gagal parsing): jangan crash, sesi dipertahankan
                null
            }
        }
    }

    // Menghitung berapa kali request ini sudah dicoba (rantai priorResponse)
    private fun jumlahPercobaan(response: Response): Int {
        var jumlah = 1
        var sebelumnya = response.priorResponse
        while (sebelumnya != null) {
            jumlah++
            sebelumnya = sebelumnya.priorResponse
        }
        return jumlah
    }

    private fun Request.denganToken(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private companion object {
        const val BATAS_PERCOBAAN = 2
    }
}
