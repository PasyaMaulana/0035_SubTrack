package com.example.subtrack.viewmodel.grup

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.model.dto.Group
import com.example.subtrack.model.repository.GroupRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// State daftar grup patungan
sealed interface GrupUiState {
    data object Loading : GrupUiState
    data class Success(val daftar: List<Group>) : GrupUiState
    data class Error(val pesan: PesanLayar) : GrupUiState
}

// Tab Patungan: daftar grup milik user
class GrupViewModel(private val groupRepository: GroupRepository) : ViewModel() {

    var uiState by mutableStateOf<GrupUiState>(GrupUiState.Loading)
        private set

    // Pull to refresh
    var isRefreshing by mutableStateOf(false)
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
        uiState = GrupUiState.Loading
        muat()
    }

    fun refresh() {
        isRefreshing = true
        muat()
    }

    // Kembali dari detail/form (hapus/simpan berhasil): tampilkan pesan lalu muat ulang
    fun terimaHasil(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
        muat()
    }

    // Data berubah di layar lain (ubah dari detail): muat ulang tanpa pesan
    fun muatUlang() {
        muat()
    }

    fun hapusPesan() {
        pesan = null
    }

    // Request sebelumnya dibatalkan supaya hasil lama tidak menimpa yang terbaru
    private fun muat() {
        jobMuat?.cancel()
        jobMuat = viewModelScope.launch {
            when (val hasil = groupRepository.daftarGrup()) {
                is HasilApi.Sukses -> uiState = GrupUiState.Success(hasil.data)
                is HasilApi.Gagal -> {
                    // Jika data sudah tampil, cukup Snackbar; jika belum, tampilkan ErrorView
                    if (uiState is GrupUiState.Success) pesan = hasil.kePesanLayar()
                    else uiState = GrupUiState.Error(hasil.kePesanLayar())
                }
            }
            isRefreshing = false
        }
    }
}
