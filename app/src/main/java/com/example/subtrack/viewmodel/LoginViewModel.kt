package com.example.subtrack.viewmodel

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.repositori.AuthRepository
import com.example.subtrack.repositori.HasilApi
import kotlinx.coroutines.launch

// State form login.
// errorXxx       = error validasi lokal (@StringRes)
// errorXxxServer = error field dari errors[] backend (teks apa adanya), diutamakan saat ditampilkan
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @param:StringRes val errorEmail: Int? = null,
    @param:StringRes val errorPassword: Int? = null,
    val errorEmailServer: String? = null,
    val errorPasswordServer: String? = null,
    val isLoading: Boolean = false,
    val pesanErrorServer: String? = null,
    @param:StringRes val pesanErrorRes: Int? = null,
    val berhasil: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorEmail = null, errorEmailServer = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorPassword = null, errorPasswordServer = null)
    }

    // Dipanggil layar setelah pesan error ditampilkan (mis. lewat Snackbar)
    fun hapusPesanError() {
        uiState = uiState.copy(pesanErrorServer = null, pesanErrorRes = null)
    }

    fun login() {
        if (uiState.isLoading || !validasi()) return

        uiState = uiState.copy(isLoading = true, pesanErrorServer = null, pesanErrorRes = null)
        viewModelScope.launch {
            val hasil = authRepository.login(uiState.email.trim(), uiState.password)
            uiState = when (hasil) {
                is HasilApi.Sukses -> uiState.copy(isLoading = false, berhasil = true)
                is HasilApi.Gagal -> terapkanError(hasil)
            }
        }
    }

    // errors[] dipetakan ke field; error yang tidak cocok jadi pesan umum.
    // Jika tidak ada error field sama sekali (mis. password salah), pakai message server.
    private fun terapkanError(hasil: HasilApi.Gagal): LoginUiState {
        val peta = hasil.petakanErrorField(FIELD_FORM)
        val adaErrorField = peta.perField.isNotEmpty() || peta.pesanUmum != null
        return uiState.copy(
            isLoading = false,
            errorEmailServer = peta.perField[FIELD_EMAIL],
            errorPasswordServer = peta.perField[FIELD_PASSWORD],
            pesanErrorServer = if (adaErrorField) peta.pesanUmum else hasil.pesanServer,
            pesanErrorRes = if (adaErrorField) null else hasil.pesanRes
        )
    }

    // Validasi lokal sebelum request dikirim
    private fun validasi(): Boolean {
        val email = uiState.email.trim()
        val errorEmail = when {
            email.isEmpty() -> R.string.error_wajib_diisi
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_email_tidak_valid
            else -> null
        }
        val errorPassword = if (uiState.password.isEmpty()) R.string.error_wajib_diisi else null

        uiState = uiState.copy(
            errorEmail = errorEmail,
            errorPassword = errorPassword,
            errorEmailServer = null,
            errorPasswordServer = null
        )
        return errorEmail == null && errorPassword == null
    }

    private companion object {
        // Nama field sesuai JSON backend
        const val FIELD_EMAIL = "email"
        const val FIELD_PASSWORD = "password"
        val FIELD_FORM = setOf(FIELD_EMAIL, FIELD_PASSWORD)
    }
}
