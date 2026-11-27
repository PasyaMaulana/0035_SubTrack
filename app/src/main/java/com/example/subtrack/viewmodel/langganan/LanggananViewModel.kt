package com.example.subtrack.viewmodel.langganan

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.dto.SubscriptionStatus
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// State daftar langganan
sealed interface LanggananUiState {
    data object Loading : LanggananUiState
    data class Success(val daftar: List<Subscription>) : LanggananUiState
    data class Error(val pesan: PesanLayar) : LanggananUiState
}

@OptIn(FlowPreview::class)
class LanggananViewModel(private val subscriptionRepository: SubscriptionRepository) : ViewModel() {

    var uiState by mutableStateOf<LanggananUiState>(LanggananUiState.Loading)
        private set

    // Input pencarian (nama layanan) dan filter status (null = semua status)
    var kataKunci by mutableStateOf("")
        private set
    var status by mutableStateOf<SubscriptionStatus?>(null)
        private set

    // isRefreshing = pull to refresh; sedangMemuat = muat ulang karena cari/filter (data lama tetap tampil)
    var isRefreshing by mutableStateOf(false)
        private set
    var sedangMemuat by mutableStateOf(false)
        private set

    // Pesan Snackbar sekali tampil
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    private var jobMuat: Job? = null

    init {
        // Pencarian di-debounce supaya request tidak dikirim di setiap ketikan.
        // Teks kosong (awal dibuka atau pencarian dihapus) langsung dimuat tanpa jeda.
        val kataKunciFlow = snapshotFlow { kataKunci.trim() }
            .debounce { if (it.isEmpty()) 0L else DEBOUNCE_MS }
            .distinctUntilChanged()
        val statusFlow = snapshotFlow { status }

        viewModelScope.launch {
            combine(kataKunciFlow, statusFlow) { q, s -> q to s }
                .distinctUntilChanged()
                .collect { (q, s) -> muat(q, s) }
        }
    }

    fun onKataKunciChange(value: String) {
        kataKunci = value
    }

    fun onStatusChange(value: SubscriptionStatus?) {
        status = value
    }

    // Dipanggil tombol "Coba Lagi"
    fun cobaLagi() {
        uiState = LanggananUiState.Loading
        muat()
    }

    fun refresh() {
        isRefreshing = true
        muat()
    }

    // Kembali dari detail/form (hapus/simpan berhasil): tampilkan pesan lalu muat ulang daftar
    fun terimaHasil(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
        muat()
    }

    // Data berubah di layar lain (ubah dari detail): muat ulang tanpa pesan, daftar lama tetap tampil
    fun muatUlang() {
        muat()
    }

    fun hapusPesan() {
        pesan = null
    }

    // Request sebelumnya dibatalkan supaya hasil lama tidak menimpa hasil pencarian terbaru
    private fun muat(q: String = kataKunci.trim(), s: SubscriptionStatus? = status) {
        jobMuat?.cancel()
        sedangMemuat = uiState is LanggananUiState.Success && !isRefreshing
        jobMuat = viewModelScope.launch {
            when (val hasil = subscriptionRepository.daftarLangganan(status = s, q = q)) {
                is HasilApi.Sukses -> uiState = LanggananUiState.Success(hasil.data)
                is HasilApi.Gagal -> {
                    // Jika data sudah tampil, cukup Snackbar; jika belum, tampilkan ErrorView
                    if (uiState is LanggananUiState.Success) pesan = hasil.kePesanLayar()
                    else uiState = LanggananUiState.Error(hasil.kePesanLayar())
                }
            }
            isRefreshing = false
            sedangMemuat = false
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
