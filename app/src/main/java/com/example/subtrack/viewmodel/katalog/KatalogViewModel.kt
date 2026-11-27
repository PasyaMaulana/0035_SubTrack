package com.example.subtrack.viewmodel.katalog

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Category
import com.example.subtrack.model.dto.Service
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.ServiceRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// State daftar katalog
sealed interface KatalogUiState {
    data object Loading : KatalogUiState
    data class Success(val daftar: List<Service>) : KatalogUiState
    data class Error(val pesan: PesanLayar) : KatalogUiState
}

@OptIn(FlowPreview::class)
class KatalogViewModel(private val serviceRepository: ServiceRepository) : ViewModel() {

    var uiState by mutableStateOf<KatalogUiState>(KatalogUiState.Loading)
        private set

    // Input pencarian dan filter kategori (null = semua kategori)
    var kataKunci by mutableStateOf("")
        private set
    var kategori by mutableStateOf<Category?>(null)
        private set

    // isRefreshing = pull to refresh; sedangMemuat = muat ulang karena cari/filter (data lama tetap tampil)
    var isRefreshing by mutableStateOf(false)
        private set
    var sedangMemuat by mutableStateOf(false)
        private set

    // Layanan yang menunggu konfirmasi hapus (dialog tampil jika tidak null)
    var layananDihapus by mutableStateOf<Service?>(null)
        private set
    var sedangMenghapus by mutableStateOf(false)
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
        val kategoriFlow = snapshotFlow { kategori }

        viewModelScope.launch {
            combine(kataKunciFlow, kategoriFlow) { q, k -> q to k }
                .distinctUntilChanged()
                .collect { (q, k) -> muat(q, k) }
        }
    }

    fun onKataKunciChange(value: String) {
        kataKunci = value
    }

    fun onKategoriChange(value: Category?) {
        kategori = value
    }

    // Dipanggil tombol "Coba Lagi"
    fun cobaLagi() {
        uiState = KatalogUiState.Loading
        muat()
    }

    fun refresh() {
        isRefreshing = true
        muat()
    }

    // Kembali dari form (simpan/hapus berhasil): tampilkan pesan lalu muat ulang daftar
    fun terimaHasilForm(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
        muat()
    }

    fun hapusPesan() {
        pesan = null
    }

    fun konfirmasiHapus(service: Service) {
        layananDihapus = service
    }

    fun batalHapus() {
        if (!sedangMenghapus) layananDihapus = null
    }

    fun hapus() {
        val target = layananDihapus ?: return
        if (sedangMenghapus) return
        sedangMenghapus = true
        viewModelScope.launch {
            when (val hasil = serviceRepository.hapusLayanan(target.id)) {
                is HasilApi.Sukses -> {
                    // Hapus dari daftar yang tampil tanpa memuat ulang semuanya
                    (uiState as? KatalogUiState.Success)?.let { state ->
                        uiState = state.copy(daftar = state.daftar.filterNot { it.id == target.id })
                    }
                    pesan = PesanLayar(teksRes = R.string.msg_berhasil_dihapus)
                }
                // Contoh 409: layanan masih dipakai langganan/grup -> pesan server apa adanya
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            sedangMenghapus = false
            layananDihapus = null
        }
    }

    // Request sebelumnya dibatalkan supaya hasil lama tidak menimpa hasil pencarian terbaru
    private fun muat(q: String = kataKunci.trim(), k: Category? = kategori) {
        jobMuat?.cancel()
        sedangMemuat = uiState is KatalogUiState.Success && !isRefreshing
        jobMuat = viewModelScope.launch {
            val hasil = serviceRepository.daftarLayanan(q = q, category = k)
            when (hasil) {
                is HasilApi.Sukses -> uiState = KatalogUiState.Success(hasil.data)
                is HasilApi.Gagal -> {
                    // Jika data sudah tampil, cukup Snackbar; jika belum, tampilkan ErrorView
                    if (uiState is KatalogUiState.Success) pesan = hasil.kePesanLayar()
                    else uiState = KatalogUiState.Error(hasil.kePesanLayar())
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
