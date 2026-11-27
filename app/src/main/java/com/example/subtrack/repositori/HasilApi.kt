package com.example.subtrack.repositori

import androidx.annotation.StringRes
import com.example.subtrack.R
import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.ErrorDetail
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

// Hasil pemanggilan API yang dikembalikan repository ke ViewModel
sealed interface HasilApi<out T> {
    data class Sukses<T>(val data: T, val pesan: String = "") : HasilApi<T>

    // pesanServer: message dari backend (boleh ditampilkan apa adanya)
    // pesanRes: pesan lokal dari strings.xml, dipakai jika server tidak memberi pesan
    // errorField: isi errors[] dari backend, contoh {field:"email", message:"Email sudah dipakai"}
    data class Gagal(
        val pesanServer: String? = null,
        @param:StringRes val pesanRes: Int = R.string.error_umum,
        val errorField: List<ErrorDetail> = emptyList()
    ) : HasilApi<Nothing>
}

// Memanggil endpoint yang mengembalikan data (data wajib ada jika sukses)
suspend fun <T> panggilApi(json: Json, panggil: suspend () -> ApiResponse<T>): HasilApi<T> =
    tangkapError(json) {
        val response = panggil()
        val data = response.data
        if (response.success && data != null) HasilApi.Sukses(data, response.message)
        else response.keGagal()
    }

// Memanggil endpoint yang tidak mengembalikan data (delete, logout)
suspend fun panggilApiTanpaData(json: Json, panggil: suspend () -> ApiResponse<Unit>): HasilApi<Unit> =
    tangkapError(json) {
        val response = panggil()
        if (response.success) HasilApi.Sukses(Unit, response.message) else response.keGagal()
    }

// Mengubah semua jenis error menjadi HasilApi.Gagal supaya aplikasi tidak crash
private suspend fun <T> tangkapError(json: Json, blok: suspend () -> HasilApi<T>): HasilApi<T> =
    try {
        blok()
    } catch (e: CancellationException) {
        throw e // pembatalan coroutine harus diteruskan
    } catch (e: HttpException) {
        bacaErrorBody(json, e)
    } catch (e: SocketTimeoutException) {
        HasilApi.Gagal(pesanRes = R.string.error_timeout)
    } catch (e: IOException) {
        HasilApi.Gagal(pesanRes = R.string.error_jaringan)
    } catch (e: SerializationException) {
        HasilApi.Gagal(pesanRes = R.string.error_umum)
    }

// Membaca body response gagal: { success:false, message, errors:[{field,message}] }
private fun bacaErrorBody(json: Json, e: HttpException): HasilApi.Gagal {
    val pesanCadangan = when (e.code()) {
        401 -> R.string.error_sesi_habis
        403 -> R.string.error_tidak_diizinkan
        404 -> R.string.error_tidak_ditemukan
        else -> R.string.error_umum
    }
    val body = try {
        e.response()?.errorBody()?.string()
    } catch (_: IOException) {
        null
    }
    val response = body?.let {
        try {
            json.decodeFromString<ApiResponse<JsonElement>>(it)
        } catch (_: IllegalArgumentException) {
            null // body bukan JSON (mis. halaman error HTML)
        }
    }
    return response?.keGagal(pesanCadangan) ?: HasilApi.Gagal(pesanRes = pesanCadangan)
}

// Ambil message dan errors dari ApiResponse. Jika message kosong, pakai error field pertama.
private fun ApiResponse<*>.keGagal(@StringRes pesanCadangan: Int = R.string.error_umum): HasilApi.Gagal {
    val errorField = errors.orEmpty().filter { it.message.isNotBlank() }
    val pesan = message.ifBlank { errors?.firstOrNull()?.message.orEmpty() }
    return HasilApi.Gagal(
        pesanServer = pesan.ifBlank { null },
        pesanRes = pesanCadangan,
        errorField = errorField
    )
}
