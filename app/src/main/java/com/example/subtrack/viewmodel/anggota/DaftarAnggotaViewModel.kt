package com.example.subtrack.viewmodel.anggota

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.DuesStatus
import com.example.subtrack.model.dto.Member
import com.example.subtrack.model.dto.UpdateMemberRequest
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.MemberRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.launch

// State daftar anggota satu grup
sealed interface DaftarAnggotaUiState {
    data object Loading : DaftarAnggotaUiState
    data class Success(val anggota: List<Member>) : DaftarAnggotaUiState
    data class Error(val pesan: PesanLayar) : DaftarAnggotaUiState
}

// Daftar anggota yang tampil di layar detail grup: ubah status iuran langsung dan hapus anggota
class DaftarAnggotaViewModel(
    savedStateHandle: SavedStateHandle,
    private val memberRepository: MemberRepository
) : ViewModel() {

    // id grup dari argumen rute detail grup
    private val idGrup: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID_GRUP))

    var uiState by mutableStateOf<DaftarAnggotaUiState>(DaftarAnggotaUiState.Loading)
        private set

    // Anggota yang menunggu konfirmasi hapus (dialog tampil jika tidak null)
    var akanDihapus by mutableStateOf<Member?>(null)
        private set

    // id anggota yang sedang diproses (ubah status atau hapus), untuk indikator di item
    var idSedangDiproses by mutableStateOf<Int?>(null)
        private set

    // Pesan Snackbar sekali tampil
    var pesan by mutableStateOf<PesanLayar?>(null)
        private set

    // true setelah data anggota berubah di layar ini (status iuran / hapus).
    // Layar detail memantaunya untuk memuat ulang ringkasan iuran grup, lalu memanggil perubahanDiterima().
    var adaPerubahan by mutableStateOf(false)
        private set

    init {
        muat()
    }

    fun muat() {
        uiState = DaftarAnggotaUiState.Loading
        viewModelScope.launch {
            uiState = when (val hasil = memberRepository.daftarAnggota(idGrup)) {
                is HasilApi.Sukses -> DaftarAnggotaUiState.Success(hasil.data)
                is HasilApi.Gagal -> DaftarAnggotaUiState.Error(hasil.kePesanLayar())
            }
        }
    }

    // Muat ulang tanpa loading penuh (setelah form anggota ditutup).
    // Jika gagal, daftar lama tetap tampil dan error muncul di Snackbar.
    fun muatUlang() {
        if (uiState !is DaftarAnggotaUiState.Success) {
            muat()
            return
        }
        viewModelScope.launch {
            when (val hasil = memberRepository.daftarAnggota(idGrup)) {
                is HasilApi.Sukses -> uiState = DaftarAnggotaUiState.Success(hasil.data)
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
        }
    }

    fun hapusPesan() {
        pesan = null
    }

    fun perubahanDiterima() {
        adaPerubahan = false
    }

    // Tandai lunas <-> belum lunas. Tanggal bayar diatur server (hari ini saat lunas, kosong saat belum lunas).
    fun ubahStatus(anggota: Member) {
        if (idSedangDiproses != null) return
        val statusBaru = if (anggota.duesStatus == DuesStatus.LUNAS) DuesStatus.BELUM_LUNAS else DuesStatus.LUNAS
        idSedangDiproses = anggota.id
        viewModelScope.launch {
            val hasil = memberRepository.ubahAnggota(idGrup, anggota.id, UpdateMemberRequest(duesStatus = statusBaru))
            when (hasil) {
                is HasilApi.Sukses -> {
                    gantiAnggota(hasil.data)
                    pesan = PesanLayar(
                        teksRes = if (statusBaru == DuesStatus.LUNAS) R.string.msg_ditandai_lunas
                        else R.string.msg_ditandai_belum_lunas
                    )
                    adaPerubahan = true
                }
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            idSedangDiproses = null
        }
    }

    fun mintaHapus(anggota: Member) {
        if (idSedangDiproses == null) akanDihapus = anggota
    }

    fun batalHapus() {
        akanDihapus = null
    }

    fun hapus() {
        val anggota = akanDihapus ?: return
        akanDihapus = null
        idSedangDiproses = anggota.id
        viewModelScope.launch {
            when (val hasil = memberRepository.hapusAnggota(idGrup, anggota.id)) {
                is HasilApi.Sukses -> {
                    val state = uiState
                    if (state is DaftarAnggotaUiState.Success) {
                        uiState = state.copy(anggota = state.anggota.filterNot { it.id == anggota.id })
                    }
                    pesan = PesanLayar(teksRes = R.string.msg_berhasil_dihapus)
                    adaPerubahan = true
                }
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            idSedangDiproses = null
        }
    }

    // Ganti satu anggota di daftar dengan data terbaru dari server
    private fun gantiAnggota(baru: Member) {
        val state = uiState
        if (state is DaftarAnggotaUiState.Success) {
            uiState = state.copy(anggota = state.anggota.map { if (it.id == baru.id) baru else it })
        }
    }

    companion object {
        // Sama dengan argumen id di rute detail grup (layar tempat daftar ini tampil)
        const val ARG_ID_GRUP = "id"
    }
}
