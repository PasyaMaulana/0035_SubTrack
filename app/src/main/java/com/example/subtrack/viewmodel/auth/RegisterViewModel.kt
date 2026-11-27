package com.example.subtrack.viewmodel.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.repository.AuthRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.launch

// State form register.
// errorXxx       = error validasi lokal (@StringRes)
// errorXxxServer = error field dari errors[] backend (teks apa adanya), diutamakan saat ditampilkan
data class RegisterUiState(
    val nama: String = "",
    val email: String = "",
    val password: String = "",
    val konfirmasiPassword: String = "",
    @param:StringRes val errorNama: Int? = null,
    @param:StringRes val errorEmail: Int? = null,
    @param:StringRes val errorPassword: Int? = null,
    @param:StringRes val errorKonfirmasi: Int? = null,
    val errorNamaServer: String? = null,
    val errorEmailServer: String? = null,
    val errorPasswordServer: String? = null,
    val isLoading: Boolean = false,
    val pesanErrorServer: String? = null,
    @param:StringRes val pesanErrorRes: Int? = null,
    val berhasil: Boolean = false
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onNamaChange(value: String) {
        uiState = uiState.copy(nama = value, errorNama = null, errorNamaServer = null)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorEmail = null, errorEmailServer = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorPassword = null, errorPasswordServer = null)
    }

    fun onKonfirmasiChange(value: String) {
        uiState = uiState.copy(konfirmasiPassword = value, errorKonfirmasi = null)
    }

    fun hapusPesanError() {
        uiState = uiState.copy(pesanErrorServer = null, pesanErrorRes = null)
    }

    fun register() {
        if (uiState.isLoading || !validasi()) return

        uiState = uiState.copy(isLoading = true, pesanErrorServer = null, pesanErrorRes = null)
        viewModelScope.launch {
            val hasil = authRepository.register(
                name = uiState.nama.trim(),
                email = uiState.email.trim(),
                password = uiState.password
            )
            uiState = when (hasil) {
                is HasilApi.Sukses -> uiState.copy(isLoading = false, berhasil = true)
                is HasilApi.Gagal -> terapkanError(hasil)
            }
        }
    }

    // errors[] dipetakan ke field; error yang tidak cocok jadi pesan umum.
    // Jika tidak ada error field sama sekali, pakai message server / pesan lokal.
    private fun terapkanError(hasil: HasilApi.Gagal): RegisterUiState {
        val peta = hasil.petakanErrorField(FIELD_FORM)
        val adaErrorField = peta.perField.isNotEmpty() || peta.pesanUmum != null
        return uiState.copy(
            isLoading = false,
            errorNamaServer = peta.perField[FIELD_NAME],
            errorEmailServer = peta.perField[FIELD_EMAIL],
            errorPasswordServer = peta.perField[FIELD_PASSWORD],
            pesanErrorServer = if (adaErrorField) peta.pesanUmum else hasil.pesanServer,
            pesanErrorRes = if (adaErrorField) null else hasil.pesanRes
        )
    }

    // Validasi lokal sebelum request dikirim, mengikuti aturan backend
    private fun validasi(): Boolean {
        val nama = uiState.nama.trim()
        val email = uiState.email.trim()
        val password = uiState.password

        val errorNama = when {
            nama.isEmpty() -> R.string.error_wajib_diisi
            nama.length < NAMA_MIN -> R.string.error_nama_pendek
            nama.length > NAMA_MAKS -> R.string.error_nama_panjang
            else -> null
        }
        val errorEmail = when {
            email.isEmpty() -> R.string.error_wajib_diisi
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_email_tidak_valid
            else -> null
        }
        val errorPassword = when {
            password.isEmpty() -> R.string.error_wajib_diisi
            password.length < PASSWORD_MIN -> R.string.error_password_pendek
            password.none { it.isLetter() } || password.none { it.isDigit() } ->
                R.string.error_password_huruf_angka
            else -> null
        }
        val errorKonfirmasi = when {
            uiState.konfirmasiPassword.isEmpty() -> R.string.error_wajib_diisi
            uiState.konfirmasiPassword != password -> R.string.error_password_tidak_sama
            else -> null
        }

        uiState = uiState.copy(
            errorNama = errorNama,
            errorEmail = errorEmail,
            errorPassword = errorPassword,
            errorKonfirmasi = errorKonfirmasi,
            errorNamaServer = null,
            errorEmailServer = null,
            errorPasswordServer = null
        )
        return listOf(errorNama, errorEmail, errorPassword, errorKonfirmasi).all { it == null }
    }

    private companion object {
        // Batas sama dengan validasi backend dan teks error di strings.xml
        const val NAMA_MIN = 3
        const val NAMA_MAKS = 50
        const val PASSWORD_MIN = 8

        // Nama field sesuai JSON backend
        const val FIELD_NAME = "name"
        const val FIELD_EMAIL = "email"
        const val FIELD_PASSWORD = "password"
        val FIELD_FORM = setOf(FIELD_NAME, FIELD_EMAIL, FIELD_PASSWORD)
    }
}
