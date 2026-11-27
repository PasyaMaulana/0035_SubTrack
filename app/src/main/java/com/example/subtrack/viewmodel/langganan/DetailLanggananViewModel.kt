package com.example.subtrack.viewmodel.langganan

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.PaymentRepository
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.util.PenyimpananFotoBukti
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// State detail langganan
sealed interface DetailLanggananUiState {
    data object Loading : DetailLanggananUiState
    data class Success(val langganan: Subscription) : DetailLanggananUiState
    data class Error(val pesan: PesanLayar) : DetailLanggananUiState
}

class DetailLanggananViewModel(
    savedStateHandle: SavedStateHandle,
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val penyimpananFotoBukti: PenyimpananFotoBukti
) : ViewModel() {

    // id dari argumen navigasi (wajib ada di rute detail)
    private val idLangganan: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID))

    var uiState by mutableStateOf<DetailLanggananUiState>(DetailLanggananUiState.Loading)
        private set

    var tampilDialogHapus by mutableStateOf(false)
        private set
    var isMenghapus by mutableStateOf(false)
        private set

    // Pesan Snackbar sekali tampil (mis. gagal hapus)
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    // Terisi saat hapus berhasil: pesan untuk layar daftar, lalu detail ditutup
    @get:StringRes
    var pesanSelesai by mutableStateOf<Int?>(null)
        private set

    init {
        muat()
    }

    fun muat() {
        uiState = DetailLanggananUiState.Loading
        viewModelScope.launch {
            uiState = when (val hasil = subscriptionRepository.detailLangganan(idLangganan)) {
                is HasilApi.Sukses -> DetailLanggananUiState.Success(hasil.data)
                is HasilApi.Gagal -> DetailLanggananUiState.Error(hasil.kePesanLayar())
            }
        }
    }

    // Kembali dari form ubah: tampilkan pesan lalu muat ulang tanpa layar loading penuh
    fun terimaHasil(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
        viewModelScope.launch {
            when (val hasil = subscriptionRepository.detailLangganan(idLangganan)) {
                is HasilApi.Sukses -> uiState = DetailLanggananUiState.Success(hasil.data)
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
        }
    }

    fun hapusPesan() {
        pesan = null
    }

    fun tampilkanDialogHapus() {
        if (!isMenghapus) tampilDialogHapus = true
    }

    fun tutupDialogHapus() {
        tampilDialogHapus = false
    }

    fun hapus() {
        if (isMenghapus) return
        tampilDialogHapus = false
        isMenghapus = true
        viewModelScope.launch {
            // Server ikut menghapus semua pembayaran (CASCADE), tetapi foto bukti ada di perangkat.
            // Jadi nama fotonya dicatat dulu, sebelum datanya hilang dari server.
            val fotoBukti = daftarFotoBukti()
            when (val hasil = subscriptionRepository.hapusLangganan(idLangganan)) {
                is HasilApi.Sukses -> {
                    // Foto baru dihapus setelah langganan benar-benar terhapus di server
                    withContext(Dispatchers.IO) { fotoBukti.forEach(penyimpananFotoBukti::hapus) }
                    pesanSelesai = R.string.msg_berhasil_dihapus
                }
                // Gagal hapus -> data dan foto tetap ada, tidak ada file yang dihapus
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            isMenghapus = false
        }
    }

    // Nama file foto bukti semua pembayaran langganan ini ("" dan null dilewati).
    // Jika daftar gagal diambil, langganan tetap dihapus; fotonya saja yang tidak ikut terhapus.
    private suspend fun daftarFotoBukti(): List<String> =
        when (val hasil = paymentRepository.daftarPembayaran(idLangganan)) {
            is HasilApi.Sukses -> hasil.data.payments.mapNotNull { it.namaFileBukti }.distinct()
            is HasilApi.Gagal -> emptyList()
        }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID = "id"
    }
}
