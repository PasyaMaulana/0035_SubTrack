package com.example.subtrack.viewmodel.dashboard

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.model.dto.DashboardData
import com.example.subtrack.model.repository.DashboardRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// State layar dashboard
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val pesan: PesanLayar) : DashboardUiState
}

// Tab Dashboard: ringkasan langganan dan patungan (USER) atau statistik aplikasi (ADMIN)
class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {

    var uiState by mutableStateOf<DashboardUiState>(DashboardUiState.Loading)
        private set

    // Pull to refresh
    var isRefreshing by mutableStateOf(false)
        private set

    // Pesan Snackbar sekali tampil
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    private var jobMuat: Job? = null

    // Dipanggil setiap kali tab Dashboard tampil (buka tab atau kembali dari layar lain),
    // supaya ringkasan selalu mengikuti perubahan data di tab lain. Data lama tetap tampil selama memuat.
    fun tampil() {
        muat()
    }

    // Dipanggil tombol "Coba Lagi"
    fun cobaLagi() {
        uiState = DashboardUiState.Loading
        muat()
    }

    fun refresh() {
        isRefreshing = true
        muat()
    }

    // Pesan dari layar lain (contoh langganan dihapus dari detail yang dibuka lewat dashboard)
    fun terimaHasil(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
    }

    fun hapusPesan() {
        pesan = null
    }

    // Request sebelumnya dibatalkan supaya hasil lama tidak menimpa yang terbaru
    private fun muat() {
        jobMuat?.cancel()
        jobMuat = viewModelScope.launch {
            when (val hasil = dashboardRepository.ringkasan(HARI_TAGIHAN)) {
                is HasilApi.Sukses -> uiState = DashboardUiState.Success(hasil.data)
                is HasilApi.Gagal -> {
                    // Jika data sudah tampil, cukup Snackbar; jika belum, tampilkan ErrorView
                    if (uiState is DashboardUiState.Success) pesan = hasil.kePesanLayar()
                    else uiState = DashboardUiState.Error(hasil.kePesanLayar())
                }
            }
            isRefreshing = false
        }
    }

    companion object {
        // Rentang hari daftar tagihan terdekat (parameter days di GET /dashboard)
        const val HARI_TAGIHAN = 14
    }
}
