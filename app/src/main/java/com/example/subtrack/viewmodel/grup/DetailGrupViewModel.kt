package com.example.subtrack.viewmodel.grup

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Group
import com.example.subtrack.model.repository.GroupRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.launch

// State detail grup
sealed interface DetailGrupUiState {
    data object Loading : DetailGrupUiState
    data class Success(val grup: Group) : DetailGrupUiState
    data class Error(val pesan: PesanLayar) : DetailGrupUiState
}

class DetailGrupViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository
) : ViewModel() {

    // id dari argumen navigasi (wajib ada di rute detail)
    private val idGrup: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID))

    var uiState by mutableStateOf<DetailGrupUiState>(DetailGrupUiState.Loading)
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
        uiState = DetailGrupUiState.Loading
        viewModelScope.launch {
            uiState = when (val hasil = groupRepository.detailGrup(idGrup)) {
                is HasilApi.Sukses -> DetailGrupUiState.Success(hasil.data)
                is HasilApi.Gagal -> DetailGrupUiState.Error(hasil.kePesanLayar())
            }
        }
    }

    // Kembali dari form ubah: tampilkan pesan lalu muat ulang tanpa layar loading penuh
    fun terimaHasil(@StringRes pesanRes: Int) {
        pesan = PesanLayar(teksRes = pesanRes)
        viewModelScope.launch {
            when (val hasil = groupRepository.detailGrup(idGrup)) {
                is HasilApi.Sukses -> uiState = DetailGrupUiState.Success(hasil.data)
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
            when (val hasil = groupRepository.hapusGrup(idGrup)) {
                is HasilApi.Sukses -> pesanSelesai = R.string.msg_berhasil_dihapus
                is HasilApi.Gagal -> pesan = hasil.kePesanLayar()
            }
            isMenghapus = false
        }
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID = "id"
    }
}
