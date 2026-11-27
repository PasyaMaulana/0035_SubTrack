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
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import kotlinx.coroutines.launch

// State detail langganan
sealed interface DetailLanggananUiState {
    data object Loading : DetailLanggananUiState
    data class Success(val langganan: Subscription) : DetailLanggananUiState
    data class Error(val pesan: PesanLayar) : DetailLanggananUiState
}

class DetailLanggananViewModel(
    savedStateHandle: SavedStateHandle,
    private val subscriptionRepository: SubscriptionRepository
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
            when (val hasil = subscriptionRepository.hapusLangganan(idLangganan)) {
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
