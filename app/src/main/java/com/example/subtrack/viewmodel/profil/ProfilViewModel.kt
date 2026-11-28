package com.example.subtrack.viewmodel.profil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.model.dto.User
import com.example.subtrack.model.repository.AuthRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// State halaman profil
sealed interface ProfilUiState {
    data object Loading : ProfilUiState
    data class Success(val user: User) : ProfilUiState
    data class Error(val pesan: PesanLayar) : ProfilUiState
}

// Tab Profil (USER dan ADMIN): data akun dari GET /auth/me dan logout
class ProfilViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var uiState by mutableStateOf<ProfilUiState>(ProfilUiState.Loading)
        private set

    // Pull to refresh
    var isRefreshing by mutableStateOf(false)
        private set

    // true selama request logout berjalan (tombol Keluar menampilkan loading)
    var sedangKeluar by mutableStateOf(false)
        private set

    // Pesan Snackbar sekali tampil
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    private var jobMuat: Job? = null

    init {
        muat()
    }

    // Dipanggil tombol "Coba Lagi"
    fun cobaLagi() {
        uiState = ProfilUiState.Loading
        muat()
    }

    fun refresh() {
        isRefreshing = true
        muat()
    }

    fun hapusPesan() {
        pesan = null
    }

    // Logout: repository memberi tahu server lalu menghapus sesi lokal.
    // Sesi terhapus -> NavGraph otomatis pindah ke login dengan back stack dikosongkan.
    fun logout() {
        if (sedangKeluar) return
        sedangKeluar = true
        viewModelScope.launch {
            authRepository.logout()
            sedangKeluar = false
        }
    }

    private fun muat() {
        jobMuat?.cancel()
        jobMuat = viewModelScope.launch {
            when (val hasil = authRepository.profil()) {
                is HasilApi.Sukses -> uiState = ProfilUiState.Success(hasil.data)
                is HasilApi.Gagal -> {
                    // Jika data sudah tampil, cukup Snackbar; jika belum, tampilkan ErrorView
                    if (uiState is ProfilUiState.Success) pesan = hasil.kePesanLayar()
                    else uiState = ProfilUiState.Error(hasil.kePesanLayar())
                }
            }
            isRefreshing = false
        }
    }
}
