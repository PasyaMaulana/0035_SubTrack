package com.example.subtrack.viewmodel.langganan

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.BillingCycle
import com.example.subtrack.model.dto.ServiceSummary
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.dto.SubscriptionRequest
import com.example.subtrack.model.dto.SubscriptionStatus
import com.example.subtrack.model.dto.UpdateSubscriptionRequest
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.ServiceRepository
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.util.hariIniApi
import com.example.subtrack.util.parseTanggalApi
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// Field form langganan beserta nama field JSON-nya di backend (untuk memetakan errors[])
enum class FieldLangganan(val api: String) {
    LAYANAN("service_id"),
    PAKET("plan_name"),
    HARGA("price"),
    SIKLUS("billing_cycle"),
    MULAI("start_date"),
    TAGIHAN("next_billing_date"),
    METODE("payment_method"),
    STATUS("status"),
    CATATAN("notes")
}

// State form tambah/ubah langganan.
// Harga disimpan sebagai teks input (hanya angka), diubah ke Long saat simpan.
// errorLokal  = error validasi lokal per field (@StringRes)
// errorServer = error per field dari errors[] backend (teks apa adanya)
data class FormLanggananUiState(
    val daftarLayanan: List<ServiceSummary> = emptyList(),
    val layananId: Int? = null,
    val namaPaket: String = "",
    val harga: String = "",
    val siklus: BillingCycle = BillingCycle.BULANAN,
    val tanggalMulai: String = "",
    val tagihanBerikutnya: String = "",
    val metodePembayaran: String = "",
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val catatan: String = "",
    val errorLokal: Map<FieldLangganan, Int> = emptyMap(),
    val errorServer: Map<FieldLangganan, String> = emptyMap(),
    val isMenyimpan: Boolean = false,
    // Pesan Snackbar sekali tampil (error simpan)
    val pesan: PesanLayar? = null,
    // Terisi saat simpan berhasil: pesan untuk layar sebelumnya, lalu form ditutup
    @param:StringRes val pesanSelesai: Int? = null
) {
    val layananTerpilih: ServiceSummary? get() = daftarLayanan.find { it.id == layananId }
}

// Status memuat data awal (daftar layanan, dan data lama saat mode ubah)
sealed interface MuatFormLanggananState {
    data object Loading : MuatFormLanggananState
    data object Siap : MuatFormLanggananState
    data class Error(val pesan: PesanLayar) : MuatFormLanggananState
}

class FormLanggananViewModel(
    savedStateHandle: SavedStateHandle,
    private val subscriptionRepository: SubscriptionRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    // id dari argumen navigasi; null = mode tambah
    private val idLangganan: Int? = savedStateHandle.get<Int>(ARG_ID)?.takeIf { it > 0 }
    val modeUbah: Boolean get() = idLangganan != null

    var uiState by mutableStateOf(FormLanggananUiState())
        private set

    var muatState by mutableStateOf<MuatFormLanggananState>(MuatFormLanggananState.Loading)
        private set

    // Data asli saat mode ubah, dipakai untuk mencari field yang berubah
    private var asal: Subscription? = null

    init {
        muatData()
    }

    // Daftar layanan aktif dan detail langganan (mode ubah) dimuat bersamaan
    fun muatData() {
        muatState = MuatFormLanggananState.Loading
        viewModelScope.launch {
            val tugasLayanan = async { serviceRepository.daftarLayanan() }
            val tugasDetail = idLangganan?.let { id -> async { subscriptionRepository.detailLangganan(id) } }
            val hasilLayanan = tugasLayanan.await()
            val hasilDetail = tugasDetail?.await()

            muatState = when {
                hasilDetail is HasilApi.Gagal -> MuatFormLanggananState.Error(hasilDetail.kePesanLayar())
                hasilLayanan is HasilApi.Gagal -> MuatFormLanggananState.Error(hasilLayanan.kePesanLayar())
                hasilLayanan is HasilApi.Sukses -> {
                    val daftar = hasilLayanan.data.map { ServiceSummary(it.id, it.name, it.category) }
                    isiForm(daftar, (hasilDetail as? HasilApi.Sukses)?.data)
                    MuatFormLanggananState.Siap
                }
                else -> MuatFormLanggananState.Error(PesanLayar())
            }
        }
    }

    fun onLayananChange(value: ServiceSummary) = ubah(FieldLangganan.LAYANAN) { copy(layananId = value.id) }

    fun onNamaPaketChange(value: String) = ubah(FieldLangganan.PAKET) { copy(namaPaket = value) }

    // Input harga hanya menerima angka; panjang dibatasi supaya tidak melebihi batas Long
    fun onHargaChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= HARGA_MAKS_DIGIT) {
            ubah(FieldLangganan.HARGA) { copy(harga = value) }
        }
    }

    fun onSiklusChange(value: BillingCycle) = ubah(FieldLangganan.SIKLUS) { copy(siklus = value) }

    // Tanggal mulai berubah -> error urutan tanggal di tagihan berikutnya ikut dihapus
    fun onTanggalMulaiChange(value: String) = ubah(FieldLangganan.MULAI, FieldLangganan.TAGIHAN) {
        copy(tanggalMulai = value)
    }

    fun onTagihanBerikutnyaChange(value: String) = ubah(FieldLangganan.TAGIHAN) { copy(tagihanBerikutnya = value) }

    fun onMetodePembayaranChange(value: String) = ubah(FieldLangganan.METODE) { copy(metodePembayaran = value) }

    fun onStatusChange(value: SubscriptionStatus) = ubah(FieldLangganan.STATUS) { copy(status = value) }

    fun onCatatanChange(value: String) = ubah(FieldLangganan.CATATAN) { copy(catatan = value) }

    fun hapusPesan() {
        uiState = uiState.copy(pesan = null)
    }

    fun simpan() {
        if (uiState.isMenyimpan || !validasi()) return
        val form = uiState
        val layananId = form.layananId ?: return
        val harga = form.harga.toLongOrNull() ?: return

        val id = idLangganan
        val asalData = asal
        // Mode ubah tanpa perubahan apa pun: tidak perlu request, langsung tutup form
        val requestUbah = if (id != null && asalData != null) buatRequestUbah(form, asalData, layananId, harga) else null
        if (id != null && requestUbah == null) {
            uiState = form.copy(pesanSelesai = R.string.msg_tidak_ada_perubahan)
            return
        }

        uiState = form.copy(isMenyimpan = true, pesan = null)
        viewModelScope.launch {
            val hasil = if (id != null && requestUbah != null) {
                subscriptionRepository.ubahLangganan(id, requestUbah)
            } else {
                // Tambah: field opsional yang kosong tidak dikirim (null),
                // next_billing_date kosong -> dihitung server dari tanggal mulai + 1 siklus
                subscriptionRepository.tambahLangganan(
                    SubscriptionRequest(
                        serviceId = layananId,
                        planName = form.namaPaket.trim(),
                        price = harga,
                        billingCycle = form.siklus,
                        startDate = form.tanggalMulai,
                        nextBillingDate = form.tagihanBerikutnya.ifEmpty { null },
                        paymentMethod = form.metodePembayaran.trim().ifEmpty { null },
                        notes = form.catatan.trim().ifEmpty { null }
                    )
                )
            }
            uiState = when (hasil) {
                is HasilApi.Sukses -> uiState.copy(isMenyimpan = false, pesanSelesai = R.string.msg_berhasil_disimpan)
                is HasilApi.Gagal -> terapkanError(hasil).copy(isMenyimpan = false)
            }
        }
    }

    // Hanya field yang berubah yang diisi; field lain null sehingga tidak ikut dikirim.
    // Teks opsional yang dikosongkan dikirim "" (aturan PUT backend). Null = tidak ada perubahan.
    private fun buatRequestUbah(
        form: FormLanggananUiState,
        asal: Subscription,
        layananId: Int,
        harga: Long
    ): UpdateSubscriptionRequest? {
        val paket = form.namaPaket.trim()
        val metode = form.metodePembayaran.trim()
        val catatan = form.catatan.trim()
        val request = UpdateSubscriptionRequest(
            serviceId = layananId.takeIf { it != asal.serviceId },
            planName = paket.takeIf { it != asal.planName },
            price = harga.takeIf { it != asal.price },
            billingCycle = form.siklus.takeIf { it != asal.billingCycle },
            startDate = form.tanggalMulai.takeIf { it != asal.startDate },
            nextBillingDate = form.tagihanBerikutnya.takeIf { it.isNotEmpty() && it != asal.nextBillingDate },
            paymentMethod = metode.takeIf { it != asal.paymentMethod.orEmpty() },
            status = form.status.takeIf { it != asal.status },
            notes = catatan.takeIf { it != asal.notes.orEmpty() }
        )
        return request.takeIf { it != UpdateSubscriptionRequest() }
    }

    // Isi form: mode tambah memakai tanggal hari ini, mode ubah memakai data lama.
    // Layanan lama yang sudah nonaktif tetap dimasukkan ke pilihan supaya tetap tampil.
    private fun isiForm(daftar: List<ServiceSummary>, langganan: Subscription?) {
        asal = langganan
        if (langganan == null) {
            uiState = uiState.copy(daftarLayanan = daftar, tanggalMulai = hariIniApi())
            return
        }
        val layananLama = langganan.service
        val pilihan = if (layananLama != null && daftar.none { it.id == layananLama.id }) {
            listOf(layananLama) + daftar
        } else {
            daftar
        }
        uiState = uiState.copy(
            daftarLayanan = pilihan,
            layananId = langganan.serviceId,
            namaPaket = langganan.planName,
            harga = langganan.price.toString(),
            siklus = langganan.billingCycle,
            tanggalMulai = langganan.startDate.take(PANJANG_TANGGAL),
            tagihanBerikutnya = langganan.nextBillingDate?.take(PANJANG_TANGGAL).orEmpty(),
            metodePembayaran = langganan.paymentMethod.orEmpty(),
            status = langganan.status,
            catatan = langganan.notes.orEmpty()
        )
    }

    // Ubah state dan hapus error (lokal dan server) milik field yang diedit
    private fun ubah(vararg field: FieldLangganan, perubahan: FormLanggananUiState.() -> FormLanggananUiState) {
        val baru = uiState.perubahan()
        uiState = baru.copy(
            errorLokal = baru.errorLokal - field.toSet(),
            errorServer = baru.errorServer - field.toSet()
        )
    }

    // errors[] dipetakan ke field form; sisanya (atau message umum) jadi Snackbar
    private fun terapkanError(hasil: HasilApi.Gagal): FormLanggananUiState {
        val peta = hasil.petakanErrorField(FieldLangganan.entries.map { it.api }.toSet())
        val perField = FieldLangganan.entries
            .mapNotNull { field -> peta.perField[field.api]?.let { field to it } }
            .toMap()
        return uiState.copy(
            errorServer = perField,
            pesan = when {
                peta.pesanUmum != null -> PesanLayar(teksServer = peta.pesanUmum)
                perField.isNotEmpty() -> null
                else -> hasil.kePesanLayar()
            }
        )
    }

    // Validasi lokal, disamakan dengan aturan backend (subscription.validation.js)
    private fun validasi(): Boolean {
        val form = uiState
        val error = mutableMapOf<FieldLangganan, Int>()

        if (form.layananId == null) error[FieldLangganan.LAYANAN] = R.string.error_pilih_layanan

        val paket = form.namaPaket.trim()
        when {
            paket.isEmpty() -> error[FieldLangganan.PAKET] = R.string.error_wajib_diisi
            paket.length > PAKET_MAKS -> error[FieldLangganan.PAKET] = R.string.error_nama_paket_panjang
        }

        // Harga bilangan bulat >= 0 (input sudah hanya angka, jadi tidak mungkin negatif)
        when {
            form.harga.isEmpty() -> error[FieldLangganan.HARGA] = R.string.error_wajib_diisi
            form.harga.toLongOrNull() == null -> error[FieldLangganan.HARGA] = R.string.error_angka_tidak_valid
        }

        val mulai = parseTanggalApi(form.tanggalMulai)
        if (mulai == null) error[FieldLangganan.MULAI] = R.string.error_wajib_diisi

        // Tanggal tagihan opsional, tetapi tidak boleh sebelum tanggal mulai
        val tagihan = parseTanggalApi(form.tagihanBerikutnya)
        if (mulai != null && tagihan != null && tagihan.isBefore(mulai)) {
            error[FieldLangganan.TAGIHAN] = R.string.error_tagihan_sebelum_mulai
        }

        if (form.metodePembayaran.trim().length > METODE_MAKS) {
            error[FieldLangganan.METODE] = R.string.error_metode_panjang
        }
        if (form.catatan.trim().length > CATATAN_MAKS) {
            error[FieldLangganan.CATATAN] = R.string.error_catatan_panjang
        }

        uiState = form.copy(errorLokal = error, errorServer = emptyMap())
        return error.isEmpty()
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID = "id"

        private const val PAKET_MAKS = 50
        private const val METODE_MAKS = 30
        private const val CATATAN_MAKS = 255
        private const val HARGA_MAKS_DIGIT = 12
        private const val PANJANG_TANGGAL = 10
    }
}
