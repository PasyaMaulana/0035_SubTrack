package com.example.subtrack.viewmodel.pembayaran

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.Payment
import com.example.subtrack.model.dto.PaymentRequest
import com.example.subtrack.model.dto.UpdatePaymentRequest
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.PaymentRepository
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.util.PenyimpananFotoBukti
import com.example.subtrack.util.hariIniApi
import com.example.subtrack.util.parseTanggalApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Field form pembayaran beserta nama field JSON-nya di backend (untuk memetakan errors[])
enum class FieldPembayaran(val api: String) {
    NOMINAL("amount"),
    TANGGAL("paid_at"),
    METODE("method"),
    BUKTI("proof_file_name"),
    CATATAN("note")
}

// State form tambah/ubah pembayaran.
// nominal berupa angka murni tanpa titik (titik hanya tampilan di FieldUang).
// fotoBukti = nama file di filesDir/bukti/ yang sedang dipakai form (null = tanpa bukti).
data class FormPembayaranUiState(
    val nominal: String = "",
    val tanggalBayar: String = "",
    val metode: String = "",
    val catatan: String = "",
    val fotoBukti: String? = null,
    val isMemprosesFoto: Boolean = false,
    val errorLokal: Map<FieldPembayaran, Int> = emptyMap(),
    val errorServer: Map<FieldPembayaran, String> = emptyMap(),
    val isMenyimpan: Boolean = false,
    // Pesan Snackbar sekali tampil (error simpan atau gagal menyalin foto)
    val pesan: PesanLayar? = null,
    // Terisi saat simpan berhasil: pesan untuk layar sebelumnya, lalu form ditutup
    @param:StringRes val pesanSelesai: Int? = null
)

// Status memuat data awal (harga langganan saat tambah, data lama saat ubah)
sealed interface MuatFormPembayaranState {
    data object Loading : MuatFormPembayaranState
    data object Siap : MuatFormPembayaranState
    data class Error(val pesan: PesanLayar) : MuatFormPembayaranState
}

class FormPembayaranViewModel(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val penyimpananFotoBukti: PenyimpananFotoBukti
) : ViewModel() {

    private val idLangganan: Int = checkNotNull(savedStateHandle.get<Int>(ARG_ID_LANGGANAN))

    // null = mode tambah
    private val idPembayaran: Int? = savedStateHandle.get<Int>(ARG_ID_PEMBAYARAN)?.takeIf { it > 0 }
    val modeUbah: Boolean get() = idPembayaran != null

    var uiState by mutableStateOf(FormPembayaranUiState())
        private set

    var muatState by mutableStateOf<MuatFormPembayaranState>(MuatFormPembayaranState.Loading)
        private set

    // Data asli saat mode ubah (untuk mencari field yang berubah dan foto lama)
    private var asal: Payment? = null

    // Foto yang baru disalin di form ini dan belum tersimpan ke server.
    // Dihapus jika diganti, dihapus, atau form ditutup tanpa menyimpan.
    private var fotoBaru: String? = null

    init {
        muatData()
    }

    fun muatData() {
        muatState = MuatFormPembayaranState.Loading
        viewModelScope.launch {
            val id = idPembayaran
            muatState = if (id != null) {
                when (val hasil = paymentRepository.detailPembayaran(idLangganan, id)) {
                    is HasilApi.Sukses -> {
                        isiDariPembayaran(hasil.data)
                        MuatFormPembayaranState.Siap
                    }
                    is HasilApi.Gagal -> MuatFormPembayaranState.Error(hasil.kePesanLayar())
                }
            } else {
                // Tambah: nominal dan metode default dari data langganan
                when (val hasil = subscriptionRepository.detailLangganan(idLangganan)) {
                    is HasilApi.Sukses -> {
                        uiState = uiState.copy(
                            nominal = hasil.data.price.takeIf { it > 0 }?.toString().orEmpty(),
                            tanggalBayar = hariIniApi(),
                            metode = hasil.data.paymentMethod.orEmpty()
                        )
                        MuatFormPembayaranState.Siap
                    }
                    is HasilApi.Gagal -> MuatFormPembayaranState.Error(hasil.kePesanLayar())
                }
            }
        }
    }

    // Hanya angka; panjang dibatasi sesuai kolom DECIMAL(12,0) di backend
    fun onNominalChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= NOMINAL_MAKS_DIGIT) {
            ubah(FieldPembayaran.NOMINAL) { copy(nominal = value) }
        }
    }

    fun onTanggalBayarChange(value: String) = ubah(FieldPembayaran.TANGGAL) { copy(tanggalBayar = value) }

    fun onMetodeChange(value: String) = ubah(FieldPembayaran.METODE) { copy(metode = value) }

    fun onCatatanChange(value: String) = ubah(FieldPembayaran.CATATAN) { copy(catatan = value) }

    // Hasil Photo Picker: salin ke filesDir/bukti/. uri null = pengguna batal memilih.
    fun onFotoDipilih(uri: Uri?) {
        if (uri == null || uiState.isMemprosesFoto) return
        uiState = uiState.copy(isMemprosesFoto = true)
        viewModelScope.launch {
            val nama = penyimpananFotoBukti.simpan(uri)
            if (nama == null) {
                uiState = uiState.copy(isMemprosesFoto = false, pesan = PesanLayar(teksRes = R.string.error_foto_gagal))
                return@launch
            }
            // Foto baru sebelumnya (belum tersimpan) sudah tidak dipakai -> hapus.
            // Foto lama dari server baru dihapus setelah simpan berhasil.
            hapusFotoBaru()
            fotoBaru = nama
            ubah(FieldPembayaran.BUKTI) { copy(fotoBukti = nama, isMemprosesFoto = false) }
        }
    }

    fun hapusFoto() {
        hapusFotoBaru()
        ubah(FieldPembayaran.BUKTI) { copy(fotoBukti = null) }
    }

    // File untuk pratinjau; null jika tidak ada di perangkat ini
    fun fileBukti(nama: String): File? = penyimpananFotoBukti.ambilFile(nama)

    fun hapusPesan() {
        uiState = uiState.copy(pesan = null)
    }

    fun simpan() {
        if (uiState.isMenyimpan || uiState.isMemprosesFoto || !validasi()) return
        val form = uiState
        val nominal = form.nominal.toLongOrNull() ?: return

        val id = idPembayaran
        val asalData = asal
        val requestUbah = if (id != null && asalData != null) buatRequestUbah(form, asalData, nominal) else null
        // Mode ubah tanpa perubahan: langsung tutup form
        if (id != null && requestUbah == null) {
            uiState = form.copy(pesanSelesai = R.string.msg_tidak_ada_perubahan)
            return
        }

        uiState = form.copy(isMenyimpan = true, pesan = null)
        viewModelScope.launch {
            val hasil = if (id != null && requestUbah != null) {
                paymentRepository.ubahPembayaran(idLangganan, id, requestUbah)
            } else {
                paymentRepository.tambahPembayaran(
                    idLangganan,
                    PaymentRequest(
                        amount = nominal,
                        paidAt = form.tanggalBayar,
                        method = form.metode.trim().ifEmpty { null },
                        proofFileName = form.fotoBukti,
                        note = form.catatan.trim().ifEmpty { null }
                    )
                )
            }
            uiState = when (hasil) {
                is HasilApi.Sukses -> {
                    // Foto baru sekarang milik data di server; foto lama yang diganti/dihapus dibuang
                    fotoBaru = null
                    val fotoLama = asalData?.namaFileBukti
                    if (fotoLama != null && fotoLama != form.fotoBukti) {
                        withContext(Dispatchers.IO) { penyimpananFotoBukti.hapus(fotoLama) }
                    }
                    uiState.copy(isMenyimpan = false, pesanSelesai = R.string.msg_berhasil_disimpan)
                }
                is HasilApi.Gagal -> terapkanError(hasil).copy(isMenyimpan = false)
            }
        }
    }

    // Form ditutup tanpa menyimpan -> foto yang sempat disalin tidak dipakai, hapus
    override fun onCleared() {
        hapusFotoBaru()
        super.onCleared()
    }

    private fun hapusFotoBaru() {
        penyimpananFotoBukti.hapus(fotoBaru)
        fotoBaru = null
    }

    private fun isiDariPembayaran(pembayaran: Payment) {
        asal = pembayaran
        uiState = uiState.copy(
            nominal = pembayaran.amount.toString(),
            tanggalBayar = pembayaran.paidAt.take(PANJANG_TANGGAL),
            metode = pembayaran.method.orEmpty(),
            catatan = pembayaran.note.orEmpty(),
            fotoBukti = pembayaran.namaFileBukti
        )
    }

    // Hanya field yang berubah yang diisi. Teks yang dikosongkan dikirim "" (aturan PUT backend).
    private fun buatRequestUbah(form: FormPembayaranUiState, asal: Payment, nominal: Long): UpdatePaymentRequest? {
        val metode = form.metode.trim()
        val catatan = form.catatan.trim()
        val foto = form.fotoBukti.orEmpty()
        val request = UpdatePaymentRequest(
            amount = nominal.takeIf { it != asal.amount },
            paidAt = form.tanggalBayar.takeIf { it != asal.paidAt.take(PANJANG_TANGGAL) },
            method = metode.takeIf { it != asal.method.orEmpty() },
            proofFileName = foto.takeIf { it != asal.namaFileBukti.orEmpty() },
            note = catatan.takeIf { it != asal.note.orEmpty() }
        )
        return request.takeIf { it != UpdatePaymentRequest() }
    }

    // Ubah state dan hapus error (lokal dan server) milik field yang diedit
    private fun ubah(vararg field: FieldPembayaran, perubahan: FormPembayaranUiState.() -> FormPembayaranUiState) {
        val baru = uiState.perubahan()
        uiState = baru.copy(
            errorLokal = baru.errorLokal - field.toSet(),
            errorServer = baru.errorServer - field.toSet()
        )
    }

    // errors[] dipetakan ke field form; sisanya (atau message umum) jadi Snackbar
    private fun terapkanError(hasil: HasilApi.Gagal): FormPembayaranUiState {
        val peta = hasil.petakanErrorField(FieldPembayaran.entries.map { it.api }.toSet())
        val perField = FieldPembayaran.entries
            .mapNotNull { field -> peta.perField[field.api]?.let { field to it } }
            .toMap()
        // Error foto tidak punya field teks, jadi tampil sebagai Snackbar
        val errorBukti = perField[FieldPembayaran.BUKTI]
        return uiState.copy(
            errorServer = perField,
            pesan = when {
                peta.pesanUmum != null -> PesanLayar(teksServer = peta.pesanUmum)
                errorBukti != null -> PesanLayar(teksServer = errorBukti)
                perField.isNotEmpty() -> null
                else -> hasil.kePesanLayar()
            }
        )
    }

    // Validasi lokal, disamakan dengan payment.validation.js di backend
    private fun validasi(): Boolean {
        val form = uiState
        val error = mutableMapOf<FieldPembayaran, Int>()

        val nominal = form.nominal.toLongOrNull()
        when {
            form.nominal.isEmpty() -> error[FieldPembayaran.NOMINAL] = R.string.error_wajib_diisi
            nominal == null -> error[FieldPembayaran.NOMINAL] = R.string.error_angka_tidak_valid
            nominal <= 0 -> error[FieldPembayaran.NOMINAL] = R.string.error_harus_lebih_dari_nol
        }

        if (parseTanggalApi(form.tanggalBayar) == null) error[FieldPembayaran.TANGGAL] = R.string.error_wajib_diisi

        if (form.metode.trim().length > METODE_MAKS) error[FieldPembayaran.METODE] = R.string.error_metode_panjang
        if (form.catatan.trim().length > CATATAN_MAKS) error[FieldPembayaran.CATATAN] = R.string.error_catatan_panjang

        uiState = form.copy(errorLokal = error, errorServer = emptyMap())
        return error.isEmpty()
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID_LANGGANAN = "idLangganan"
        const val ARG_ID_PEMBAYARAN = "idPembayaran"

        private const val METODE_MAKS = 30
        private const val CATATAN_MAKS = 255
        private const val NOMINAL_MAKS_DIGIT = 12
        private const val PANJANG_TANGGAL = 10
    }
}
