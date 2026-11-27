package com.example.subtrack.viewmodel.katalog

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Category
import com.example.subtrack.model.dto.Service
import com.example.subtrack.model.dto.ServiceRequest
import com.example.subtrack.model.dto.UpdateServiceRequest
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.ServiceRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.launch

// State form tambah/ubah layanan.
// errorXxx       = error validasi lokal (@StringRes)
// errorXxxServer = error field dari errors[] backend (teks apa adanya)
data class FormLayananUiState(
    val nama: String = "",
    val kategori: Category? = null,
    val deskripsi: String = "",
    val website: String = "",
    val aktif: Boolean = true,
    @param:StringRes val errorNama: Int? = null,
    @param:StringRes val errorKategori: Int? = null,
    @param:StringRes val errorDeskripsi: Int? = null,
    @param:StringRes val errorWebsite: Int? = null,
    val errorNamaServer: String? = null,
    val errorKategoriServer: String? = null,
    val errorDeskripsiServer: String? = null,
    val errorWebsiteServer: String? = null,
    val isMenyimpan: Boolean = false,
    val isMenghapus: Boolean = false,
    val tampilDialogHapus: Boolean = false,
    // Pesan Snackbar sekali tampil (error simpan/hapus)
    val pesan: PesanLayar? = null,
    // Terisi saat simpan/hapus berhasil: pesan untuk layar katalog, lalu form ditutup
    @param:StringRes val pesanSelesai: Int? = null
) {
    val sedangProses: Boolean get() = isMenyimpan || isMenghapus
}

// Status memuat data lama saat mode ubah
sealed interface MuatLayananState {
    data object Loading : MuatLayananState
    data object Siap : MuatLayananState
    data class Error(val pesan: PesanLayar) : MuatLayananState
}

class FormLayananViewModel(
    savedStateHandle: SavedStateHandle,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    // id dari argumen navigasi; null = mode tambah
    val idLayanan: Int? = savedStateHandle.get<Int>(ARG_ID)?.takeIf { it > 0 }
    val modeUbah: Boolean get() = idLayanan != null

    var uiState by mutableStateOf(FormLayananUiState())
        private set

    var muatState by mutableStateOf<MuatLayananState>(
        if (idLayanan != null) MuatLayananState.Loading else MuatLayananState.Siap
    )
        private set

    init {
        if (idLayanan != null) muatDetail()
    }

    fun muatDetail() {
        val id = idLayanan ?: return
        muatState = MuatLayananState.Loading
        viewModelScope.launch {
            muatState = when (val hasil = serviceRepository.detailLayanan(id)) {
                is HasilApi.Sukses -> {
                    isiForm(hasil.data)
                    MuatLayananState.Siap
                }
                is HasilApi.Gagal -> MuatLayananState.Error(hasil.kePesanLayar())
            }
        }
    }

    fun onNamaChange(value: String) {
        uiState = uiState.copy(nama = value, errorNama = null, errorNamaServer = null)
    }

    fun onKategoriChange(value: Category) {
        uiState = uiState.copy(kategori = value, errorKategori = null, errorKategoriServer = null)
    }

    fun onDeskripsiChange(value: String) {
        uiState = uiState.copy(deskripsi = value, errorDeskripsi = null, errorDeskripsiServer = null)
    }

    fun onWebsiteChange(value: String) {
        uiState = uiState.copy(website = value, errorWebsite = null, errorWebsiteServer = null)
    }

    fun onAktifChange(value: Boolean) {
        uiState = uiState.copy(aktif = value)
    }

    fun hapusPesan() {
        uiState = uiState.copy(pesan = null)
    }

    fun tampilkanDialogHapus() {
        uiState = uiState.copy(tampilDialogHapus = true)
    }

    fun tutupDialogHapus() {
        uiState = uiState.copy(tampilDialogHapus = false)
    }

    fun simpan() {
        if (uiState.sedangProses || !validasi()) return
        val kategori = uiState.kategori ?: return
        val nama = uiState.nama.trim()
        val deskripsi = uiState.deskripsi.trim()
        val website = normalisasiUrl(uiState.website)

        uiState = uiState.copy(isMenyimpan = true, pesan = null)
        viewModelScope.launch {
            val id = idLayanan
            val hasil = if (id == null) {
                // Tambah: field kosong tidak dikirim (null)
                serviceRepository.tambahLayanan(
                    ServiceRequest(
                        name = nama,
                        category = kategori,
                        description = deskripsi.ifEmpty { null },
                        websiteUrl = website.ifEmpty { null },
                        isActive = uiState.aktif
                    )
                )
            } else {
                // Ubah: "" berarti dikosongkan (repository mengurus website_url)
                serviceRepository.ubahLayanan(
                    id,
                    UpdateServiceRequest(
                        name = nama,
                        category = kategori,
                        description = deskripsi,
                        websiteUrl = website,
                        isActive = uiState.aktif
                    )
                )
            }
            uiState = when (hasil) {
                is HasilApi.Sukses -> uiState.copy(isMenyimpan = false, pesanSelesai = R.string.msg_berhasil_disimpan)
                is HasilApi.Gagal -> terapkanError(hasil).copy(isMenyimpan = false)
            }
        }
    }

    fun hapus() {
        val id = idLayanan ?: return
        if (uiState.sedangProses) return
        uiState = uiState.copy(isMenghapus = true, tampilDialogHapus = false, pesan = null)
        viewModelScope.launch {
            uiState = when (val hasil = serviceRepository.hapusLayanan(id)) {
                is HasilApi.Sukses -> uiState.copy(isMenghapus = false, pesanSelesai = R.string.msg_berhasil_dihapus)
                // Contoh 409: layanan masih dipakai langganan/grup -> pesan server apa adanya
                is HasilApi.Gagal -> uiState.copy(isMenghapus = false, pesan = hasil.kePesanLayar())
            }
        }
    }

    private fun isiForm(service: Service) {
        uiState = uiState.copy(
            nama = service.name,
            kategori = service.category,
            deskripsi = service.description.orEmpty(),
            website = service.websiteUrl.orEmpty(),
            aktif = service.isActive
        )
    }

    // errors[] dipetakan ke field form; sisanya (atau message umum) jadi Snackbar
    private fun terapkanError(hasil: HasilApi.Gagal): FormLayananUiState {
        val peta = hasil.petakanErrorField(FIELD_FORM)
        val adaErrorField = peta.perField.isNotEmpty()
        return uiState.copy(
            errorNamaServer = peta.perField[FIELD_NAMA],
            errorKategoriServer = peta.perField[FIELD_KATEGORI],
            errorDeskripsiServer = peta.perField[FIELD_DESKRIPSI],
            errorWebsiteServer = peta.perField[FIELD_WEBSITE],
            pesan = when {
                peta.pesanUmum != null -> PesanLayar(teksServer = peta.pesanUmum)
                adaErrorField -> null
                else -> hasil.kePesanLayar()
            }
        )
    }

    // Validasi lokal, disamakan dengan aturan backend (service.validation.js)
    private fun validasi(): Boolean {
        val nama = uiState.nama.trim()
        val errorNama = when {
            nama.isEmpty() -> R.string.error_wajib_diisi
            nama.length !in NAMA_MIN..NAMA_MAKS -> R.string.error_nama_layanan_rentang
            else -> null
        }
        val errorKategori = if (uiState.kategori == null) R.string.error_pilih_kategori else null
        val errorDeskripsi =
            if (uiState.deskripsi.trim().length > TEKS_MAKS) R.string.error_deskripsi_panjang else null
        val website = normalisasiUrl(uiState.website)
        val errorWebsite = when {
            website.isEmpty() -> null
            website.length > TEKS_MAKS || !Patterns.WEB_URL.matcher(website).matches() ->
                R.string.error_url_tidak_valid
            else -> null
        }

        uiState = uiState.copy(
            errorNama = errorNama,
            errorKategori = errorKategori,
            errorDeskripsi = errorDeskripsi,
            errorWebsite = errorWebsite,
            errorNamaServer = null,
            errorKategoriServer = null,
            errorDeskripsiServer = null,
            errorWebsiteServer = null
        )
        return listOf(errorNama, errorKategori, errorDeskripsi, errorWebsite).all { it == null }
    }

    // Server mewajibkan URL lengkap dengan skema, jadi "netflix.com" dilengkapi menjadi "https://netflix.com"
    private fun normalisasiUrl(teks: String): String {
        val url = teks.trim()
        return when {
            url.isEmpty() -> ""
            url.contains("://") -> url
            else -> "https://$url"
        }
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh NavGraph
        const val ARG_ID = "id"

        private const val NAMA_MIN = 2
        private const val NAMA_MAKS = 50
        private const val TEKS_MAKS = 255

        // Nama field sesuai JSON backend
        private const val FIELD_NAMA = "name"
        private const val FIELD_KATEGORI = "category"
        private const val FIELD_DESKRIPSI = "description"
        private const val FIELD_WEBSITE = "website_url"
        private val FIELD_FORM = setOf(FIELD_NAMA, FIELD_KATEGORI, FIELD_DESKRIPSI, FIELD_WEBSITE)
    }
}
