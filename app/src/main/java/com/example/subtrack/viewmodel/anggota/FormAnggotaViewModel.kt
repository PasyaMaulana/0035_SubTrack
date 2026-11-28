package com.example.subtrack.viewmodel.anggota

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.DuesStatus
import com.example.subtrack.model.dto.Member
import com.example.subtrack.model.dto.MemberRequest
import com.example.subtrack.model.dto.UpdateMemberRequest
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.MemberRepository
import com.example.subtrack.util.hariIniApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.launch

// Field form anggota beserta nama field JSON-nya di backend (untuk memetakan errors[])
enum class FieldAnggota(val api: String) {
    NAMA("member_name"),
    KONTAK("contact"),
    IURAN("share_amount"),
    STATUS("dues_status"),
    TANGGAL("paid_at"),
    CATATAN("note")
}

// State form tambah/ubah anggota.
// iuran berupa angka murni tanpa titik; kosong saat tambah = dibagi rata oleh server.
// tanggalBayar hanya dipakai jika status LUNAS; kosong = diisi tanggal hari ini.
data class FormAnggotaUiState(
    val nama: String = "",
    val kontak: String = "",
    val iuran: String = "",
    val status: DuesStatus = DuesStatus.BELUM_LUNAS,
    val tanggalBayar: String = "",
    val catatan: String = "",
    val errorLokal: Map<FieldAnggota, Int> = emptyMap(),
    val errorServer: Map<FieldAnggota, String> = emptyMap(),
    val isMenyimpan: Boolean = false,
    // Pesan Snackbar sekali tampil (contoh error 409 grup penuh)
    val pesan: PesanLayar? = null,
    // Terisi saat simpan berhasil: pesan untuk layar detail grup, lalu form ditutup
    @param:StringRes val pesanSelesai: Int? = null
)

// Status memuat data lama (hanya mode ubah)
sealed interface MuatFormAnggotaState {
    data object Loading : MuatFormAnggotaState
    data object Siap : MuatFormAnggotaState
    data class Error(val pesan: PesanLayar) : MuatFormAnggotaState
}

class FormAnggotaViewModel(
    savedStateHandle: SavedStateHandle,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val idGrup: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID_GRUP))

    // null = mode tambah
    private val idAnggota: Int? = savedStateHandle.get<Int>(ARG_ID_ANGGOTA)?.takeIf { it > 0 }
    val modeUbah: Boolean get() = idAnggota != null

    var uiState by mutableStateOf(FormAnggotaUiState())
        private set

    var muatState by mutableStateOf<MuatFormAnggotaState>(
        if (modeUbah) MuatFormAnggotaState.Loading else MuatFormAnggotaState.Siap
    )
        private set

    // Data asli saat mode ubah (untuk mencari field yang berubah)
    private var asal: Member? = null

    init {
        if (modeUbah) muatData()
    }

    fun muatData() {
        val id = idAnggota ?: return
        muatState = MuatFormAnggotaState.Loading
        viewModelScope.launch {
            muatState = when (val hasil = memberRepository.detailAnggota(idGrup, id)) {
                is HasilApi.Sukses -> {
                    isiDariAnggota(hasil.data)
                    MuatFormAnggotaState.Siap
                }
                is HasilApi.Gagal -> MuatFormAnggotaState.Error(hasil.kePesanLayar())
            }
        }
    }

    fun onNamaChange(value: String) = ubah(FieldAnggota.NAMA) { copy(nama = value) }

    fun onKontakChange(value: String) = ubah(FieldAnggota.KONTAK) { copy(kontak = value) }

    // Hanya angka; panjang dibatasi sesuai kolom DECIMAL(12,0) di backend
    fun onIuranChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= IURAN_MAKS_DIGIT) {
            ubah(FieldAnggota.IURAN) { copy(iuran = value) }
        }
    }

    // Belum lunas tidak punya tanggal bayar (server juga mengosongkannya)
    fun onStatusChange(value: DuesStatus) = ubah(FieldAnggota.STATUS, FieldAnggota.TANGGAL) {
        copy(status = value, tanggalBayar = if (value == DuesStatus.LUNAS) tanggalBayar else "")
    }

    fun onTanggalBayarChange(value: String) = ubah(FieldAnggota.TANGGAL) { copy(tanggalBayar = value) }

    fun onCatatanChange(value: String) = ubah(FieldAnggota.CATATAN) { copy(catatan = value) }

    fun hapusPesan() {
        uiState = uiState.copy(pesan = null)
    }

    fun simpan() {
        if (uiState.isMenyimpan || !validasi()) return
        val form = uiState

        val id = idAnggota
        val asalData = asal
        val requestUbah = if (id != null && asalData != null) buatRequestUbah(form, asalData) else null
        // Mode ubah tanpa perubahan: langsung tutup form
        if (id != null && requestUbah == null) {
            uiState = form.copy(pesanSelesai = R.string.msg_tidak_ada_perubahan)
            return
        }

        uiState = form.copy(isMenyimpan = true, pesan = null)
        viewModelScope.launch {
            val hasil = if (id != null && requestUbah != null) {
                memberRepository.ubahAnggota(idGrup, id, requestUbah)
            } else {
                memberRepository.tambahAnggota(
                    idGrup,
                    MemberRequest(
                        memberName = form.nama.trim(),
                        contact = form.kontak.trim().ifEmpty { null },
                        // null = dibagi rata oleh server
                        shareAmount = form.iuran.toLongOrNull(),
                        duesStatus = form.status,
                        paidAt = form.tanggalBayar.takeIf { form.status == DuesStatus.LUNAS && it.isNotEmpty() },
                        note = form.catatan.trim().ifEmpty { null }
                    )
                )
            }
            uiState = when (hasil) {
                is HasilApi.Sukses -> uiState.copy(isMenyimpan = false, pesanSelesai = R.string.msg_berhasil_disimpan)
                is HasilApi.Gagal -> terapkanError(hasil).copy(isMenyimpan = false)
            }
        }
    }

    private fun isiDariAnggota(anggota: Member) {
        asal = anggota
        uiState = uiState.copy(
            nama = anggota.memberName,
            kontak = anggota.contact.orEmpty(),
            iuran = anggota.shareAmount.takeIf { it > 0 }?.toString().orEmpty(),
            status = anggota.duesStatus,
            tanggalBayar = anggota.paidAt?.take(PANJANG_TANGGAL).orEmpty(),
            catatan = anggota.note.orEmpty()
        )
    }

    // Hanya field yang berubah yang diisi. Teks yang dikosongkan dikirim "" (aturan PUT backend).
    // Lunas dengan tanggal kosong dikirim tanggal hari ini; belum lunas tidak mengirim tanggal.
    private fun buatRequestUbah(form: FormAnggotaUiState, asal: Member): UpdateMemberRequest? {
        val nama = form.nama.trim()
        val kontak = form.kontak.trim()
        val catatan = form.catatan.trim()
        val tanggalAsal = asal.paidAt?.take(PANJANG_TANGGAL)
        val tanggal = if (form.status == DuesStatus.LUNAS) form.tanggalBayar.ifEmpty { hariIniApi() } else null
        val request = UpdateMemberRequest(
            memberName = nama.takeIf { it != asal.memberName },
            contact = kontak.takeIf { it != asal.contact.orEmpty() },
            shareAmount = form.iuran.toLongOrNull()?.takeIf { it != asal.shareAmount },
            duesStatus = form.status.takeIf { it != asal.duesStatus },
            paidAt = tanggal?.takeIf { it != tanggalAsal },
            note = catatan.takeIf { it != asal.note.orEmpty() }
        )
        return request.takeIf { it != UpdateMemberRequest() }
    }

    // Ubah state dan hapus error (lokal dan server) milik field yang diedit
    private fun ubah(vararg field: FieldAnggota, perubahan: FormAnggotaUiState.() -> FormAnggotaUiState) {
        val baru = uiState.perubahan()
        uiState = baru.copy(
            errorLokal = baru.errorLokal - field.toSet(),
            errorServer = baru.errorServer - field.toSet()
        )
    }

    // errors[] dipetakan ke field form; sisanya (atau message umum) jadi Snackbar.
    // 409 = grup sudah penuh: pesan server ditampilkan di Snackbar.
    private fun terapkanError(hasil: HasilApi.Gagal): FormAnggotaUiState {
        val peta = hasil.petakanErrorField(FieldAnggota.entries.map { it.api }.toSet())
        val perField = FieldAnggota.entries
            .mapNotNull { field -> peta.perField[field.api]?.let { field to it } }
            .toMap()
        val pesanGagal = when {
            hasil.kodeHttp == KODE_GRUP_PENUH ->
                PesanLayar(teksServer = hasil.pesanServer, teksRes = R.string.error_grup_penuh)
            peta.pesanUmum != null -> PesanLayar(teksServer = peta.pesanUmum)
            perField.isNotEmpty() -> null
            else -> hasil.kePesanLayar()
        }
        return uiState.copy(errorServer = perField, pesan = pesanGagal)
    }

    // Validasi lokal, disamakan dengan member.validation.js di backend
    private fun validasi(): Boolean {
        val form = uiState
        val error = mutableMapOf<FieldAnggota, Int>()

        val nama = form.nama.trim()
        when {
            nama.isEmpty() -> error[FieldAnggota.NAMA] = R.string.error_wajib_diisi
            nama.length !in NAMA_MIN..NAMA_MAKS -> error[FieldAnggota.NAMA] = R.string.error_nama_anggota_rentang
        }

        if (form.kontak.trim().length > KONTAK_MAKS) error[FieldAnggota.KONTAK] = R.string.error_kontak_panjang

        // Iuran opsional saat tambah (dibagi rata), wajib saat ubah karena server tidak bisa mengosongkannya
        val iuran = form.iuran.toLongOrNull()
        when {
            form.iuran.isEmpty() -> if (modeUbah) error[FieldAnggota.IURAN] = R.string.error_wajib_diisi
            iuran == null -> error[FieldAnggota.IURAN] = R.string.error_angka_tidak_valid
            iuran <= 0 -> error[FieldAnggota.IURAN] = R.string.error_harus_lebih_dari_nol
        }

        if (form.catatan.trim().length > CATATAN_MAKS) error[FieldAnggota.CATATAN] = R.string.error_catatan_panjang

        uiState = form.copy(errorLokal = error, errorServer = emptyMap())
        return error.isEmpty()
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID_GRUP = "idGrup"
        const val ARG_ID_ANGGOTA = "idAnggota"

        private const val NAMA_MIN = 2
        private const val NAMA_MAKS = 50
        private const val KONTAK_MAKS = 100
        private const val CATATAN_MAKS = 255
        private const val IURAN_MAKS_DIGIT = 12
        private const val PANJANG_TANGGAL = 10
        private const val KODE_GRUP_PENUH = 409
    }
}
