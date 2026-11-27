package com.example.subtrack.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.model.dto.Role
import com.example.subtrack.model.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Status sesi yang dipakai NavGraph untuk menentukan layar
sealed interface StatusSesi {
    // Sesi masih dibaca dari DataStore (saat aplikasi baru dibuka)
    data object Memuat : StatusSesi

    // Tidak ada sesi: belum login, logout, atau refresh token gagal
    data object BelumLogin : StatusSesi

    data class SudahLogin(val role: Role, val nama: String) : StatusSesi
}

// Mengamati sesi dari AuthRepository. Setiap perubahan sesi (login, logout,
// sesi dihapus TokenAuthenticator) langsung terlihat di sini.
class SesiViewModel(authRepository: AuthRepository) : ViewModel() {

    val status: StateFlow<StatusSesi> = authRepository.sesi
        .map { sesi ->
            if (sesi == null) StatusSesi.BelumLogin
            else StatusSesi.SudahLogin(role = sesi.role, nama = sesi.nama)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StatusSesi.Memuat
        )
}
