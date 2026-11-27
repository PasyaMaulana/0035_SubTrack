package com.example.subtrack.viewmodel.pembayaran

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Payment
import com.example.subtrack.model.dto.PaymentList
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.PaymentRepository
import com.example.subtrack.util.PenyimpananFotoBukti
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// State riwayat pembayaran satu langganan
sealed interface RiwayatPembayaranUiState {
    data object Loading : RiwayatPembayaranUiState
    data class Success(val riwayat: PaymentList) : RiwayatPembayaranUiState
    data class Error(val pesan: PesanLayar) : RiwayatPembayaranUiState
}

// Riwayat pembayaran yang tampil di layar detail langganan
class RiwayatPembayaranViewModel(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val penyimpananFotoBukti: PenyimpananFotoBukti
) : ViewModel() {

    // id langganan dari argumen rute detail langganan
    val idLangganan: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID_LANGGANAN))

    var uiState by mutableStateOf<RiwayatPembayaranUiState>(RiwayatPembayaranUiState.Loading)
        private set

    // Pembayaran yang menunggu konfirmasi hapus (dialog tampil jika tidak null)
    var akanDihapus by mutableStateOf<Payment?>(null)
        private set

    // id pembayaran yang sedang dihapus (untuk indikator proses di item)
    var idSedangDihapus by mutableStateOf<Int?>(null)
        private set

    // Nama file foto bukti yang sedang dilihat ukuran besar
    var fotoDilihat by mutableStateOf<String?>(null)
        private set

    // Pesan Snackbar sekali tampil
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    init {
        muat()
    }

    fun muat() {
        uiState = RiwayatPembayaranUiState.Loading
        viewModelScope.launch {
            uiState = when (val hasil = paymentRepository.daftarPembayaran(idLangganan)) {
                is HasilApi.Sukses -> RiwayatPembayaranUiState.Success(hasil.data)
                is HasilApi.Gagal -> RiwayatPembayaranUiState.Error(hasil.kePesanLayar())
            }
        }
    }

    // Muat ulang tanpa loading penuh (setelah form pembayaran ditutup atau setelah hapus).
    // Jika gagal, daftar lama tetap tampil dan error muncul di Snackbar.
    fun muatUlang() {
        if (uiState !is RiwayatPembayaranUiState.Success) {
            muat()
            return
        }
        viewModelScope.launch {
            when (val hasil = paymentRepository.daftarPembayaran(idLangganan)) {
                is HasilApi.Sukses -> uiState = RiwayatPembayaranUiState.Success(hasil.data)
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
        }
    }

    fun hapusPesan() {
        pesan = null
    }

    fun mintaHapus(pembayaran: Payment) {
        if (idSedangDihapus == null) akanDihapus = pembayaran
    }

    fun batalHapus() {
        akanDihapus = null
    }

    // Hapus di server; jika berhasil, file foto buktinya di perangkat juga dihapus
    fun hapus() {
        val pembayaran = akanDihapus ?: return
        akanDihapus = null
        idSedangDihapus = pembayaran.id
        viewModelScope.launch {
            when (val hasil = paymentRepository.hapusPembayaran(idLangganan, pembayaran.id)) {
                is HasilApi.Sukses -> {
                    withContext(Dispatchers.IO) { penyimpananFotoBukti.hapus(pembayaran.namaFileBukti) }
                    pesan = PesanLayar(teksRes = R.string.msg_berhasil_dihapus)
                    muatUlang()
                }
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            idSedangDihapus = null
        }
    }

    fun lihatFoto(pembayaran: Payment) {
        fotoDilihat = pembayaran.namaFileBukti
    }

    fun tutupFoto() {
        fotoDilihat = null
    }

    // File foto di perangkat; null jika tidak ditemukan
    fun fileBukti(nama: String): File? = penyimpananFotoBukti.ambilFile(nama)

    companion object {
        // Sama dengan argumen id di rute detail langganan (layar tempat riwayat ini tampil)
        const val ARG_ID_LANGGANAN = "id"
    }
}
