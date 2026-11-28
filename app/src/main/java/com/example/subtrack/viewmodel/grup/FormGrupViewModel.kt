package com.example.subtrack.viewmodel.grup

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subtrack.R
import com.example.subtrack.model.dto.BillingCycle
import com.example.subtrack.model.dto.Group
import com.example.subtrack.model.dto.GroupRequest
import com.example.subtrack.model.dto.ServiceSummary
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.dto.UpdateGroupRequest
import com.example.subtrack.model.repository.GroupRepository
import com.example.subtrack.model.repository.HasilApi
import com.example.subtrack.model.repository.ServiceRepository
import com.example.subtrack.model.repository.SubscriptionRepository
import com.example.subtrack.viewmodel.PesanLayar
import com.example.subtrack.viewmodel.kePesanLayar
import com.example.subtrack.viewmodel.petakanErrorField
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

// Field form grup beserta nama field JSON-nya di backend (untuk memetakan errors[])
enum class FieldGrup(val api: String) {
    LAYANAN("service_id"),
    LANGGANAN("subscription_id"),
    NAMA("name"),
    TOTAL("total_cost"),
    SIKLUS("billing_cycle"),
    JATUH_TEMPO("due_day"),
    MAKS_ANGGOTA("max_members"),
    DESKRIPSI("description")
}

// State form tambah/ubah grup.
// Angka (total biaya, jatuh tempo, maks anggota) disimpan sebagai teks input berisi angka saja.
data class FormGrupUiState(
    val daftarLayanan: List<ServiceSummary> = emptyList(),
    val daftarLangganan: List<Subscription> = emptyList(),
    val layananId: Int? = null,
    // null = tanpa langganan terkait
    val langgananId: Int? = null,
    val nama: String = "",
    val totalBiaya: String = "",
    val siklus: BillingCycle = BillingCycle.BULANAN,
    val jatuhTempo: String = "",
    val maksAnggota: String = "",
    val deskripsi: String = "",
    val errorLokal: Map<FieldGrup, Int> = emptyMap(),
    val errorServer: Map<FieldGrup, String> = emptyMap(),
    val isMenyimpan: Boolean = false,
    // Pesan Snackbar sekali tampil (error simpan)
    val pesan: PesanLayar? = null,
    // Terisi saat simpan berhasil: pesan untuk layar sebelumnya, lalu form ditutup
    @param:StringRes val pesanSelesai: Int? = null
) {
    val layananTerpilih: ServiceSummary? get() = daftarLayanan.find { it.id == layananId }
    val langgananTerpilih: Subscription? get() = daftarLangganan.find { it.id == langgananId }
}

// Status memuat data awal (layanan, langganan user, dan data lama saat mode ubah)
sealed interface MuatFormGrupState {
    data object Loading : MuatFormGrupState
    data object Siap : MuatFormGrupState
    data class Error(val pesan: PesanLayar) : MuatFormGrupState
}

class FormGrupViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val serviceRepository: ServiceRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    // id dari argumen navigasi; null = mode tambah
    private val idGrup: Int? = savedStateHandle.get<Int>(ARG_ID)?.takeIf { it > 0 }
    val modeUbah: Boolean get() = idGrup != null

    var uiState by mutableStateOf(FormGrupUiState())
        private set

    var muatState by mutableStateOf<MuatFormGrupState>(MuatFormGrupState.Loading)
        private set

    // Data asli saat mode ubah, dipakai untuk mencari field yang berubah
    private var asal: Group? = null

    init {
        muatData()
    }

    // Layanan aktif, langganan milik user, dan detail grup (mode ubah) dimuat bersamaan
    fun muatData() {
        muatState = MuatFormGrupState.Loading
        viewModelScope.launch {
            val tugasLayanan = async { serviceRepository.daftarLayanan() }
            val tugasLangganan = async { subscriptionRepository.daftarLangganan() }
            val tugasDetail = idGrup?.let { id -> async { groupRepository.detailGrup(id) } }
            val hasilLayanan = tugasLayanan.await()
            val hasilLangganan = tugasLangganan.await()
            val hasilDetail = tugasDetail?.await()

            muatState = when {
                hasilDetail is HasilApi.Gagal -> MuatFormGrupState.Error(hasilDetail.kePesanLayar())
                hasilLayanan is HasilApi.Gagal -> MuatFormGrupState.Error(hasilLayanan.kePesanLayar())
                hasilLangganan is HasilApi.Gagal -> MuatFormGrupState.Error(hasilLangganan.kePesanLayar())
                hasilLayanan is HasilApi.Sukses && hasilLangganan is HasilApi.Sukses -> {
                    val layanan = hasilLayanan.data.map { ServiceSummary(it.id, it.name, it.category) }
                    isiForm(layanan, hasilLangganan.data, (hasilDetail as? HasilApi.Sukses)?.data)
                    MuatFormGrupState.Siap
                }
                else -> MuatFormGrupState.Error(PesanLayar())
            }
        }
    }

    fun onLayananChange(value: ServiceSummary) = ubah(FieldGrup.LAYANAN) { copy(layananId = value.id) }

    // Memilih langganan -> layanan dan total biaya diisi otomatis dari langganan itu.
    // Layanan langganan yang tidak ada di daftar (mis. sudah nonaktif) dimasukkan supaya tetap tampil.
    fun onLanggananChange(value: Subscription) =
        ubah(FieldGrup.LANGGANAN, FieldGrup.LAYANAN, FieldGrup.TOTAL) {
            val layanan = value.service
            val pilihanLayanan = if (layanan != null && daftarLayanan.none { it.id == layanan.id }) {
                listOf(layanan) + daftarLayanan
            } else {
                daftarLayanan
            }
            copy(
                langgananId = value.id,
                daftarLayanan = pilihanLayanan,
                layananId = value.serviceId,
                totalBiaya = value.price.toString()
            )
        }

    fun onTanpaLangganan() = ubah(FieldGrup.LANGGANAN) { copy(langgananId = null) }

    fun onNamaChange(value: String) = ubah(FieldGrup.NAMA) { copy(nama = value) }

    fun onTotalBiayaChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= TOTAL_MAKS_DIGIT) {
            ubah(FieldGrup.TOTAL) { copy(totalBiaya = value) }
        }
    }

    fun onSiklusChange(value: BillingCycle) = ubah(FieldGrup.SIKLUS) { copy(siklus = value) }

    // Jatuh tempo dan maks anggota: angka maksimal 2 digit
    fun onJatuhTempoChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= ANGKA_KECIL_MAKS_DIGIT) {
            ubah(FieldGrup.JATUH_TEMPO) { copy(jatuhTempo = value) }
        }
    }

    fun onMaksAnggotaChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= ANGKA_KECIL_MAKS_DIGIT) {
            ubah(FieldGrup.MAKS_ANGGOTA) { copy(maksAnggota = value) }
        }
    }

    fun onDeskripsiChange(value: String) = ubah(FieldGrup.DESKRIPSI) { copy(deskripsi = value) }

    fun hapusPesan() {
        uiState = uiState.copy(pesan = null)
    }

    fun simpan() {
        if (uiState.isMenyimpan || !validasi()) return
        val form = uiState
        val layananId = form.layananId ?: return
        val total = form.totalBiaya.toLongOrNull() ?: return
        val jatuhTempo = form.jatuhTempo.toIntOrNull() ?: return
        val maksAnggota = form.maksAnggota.toIntOrNull() ?: return

        val id = idGrup
        val asalData = asal
        val requestUbah = if (id != null && asalData != null) {
            buatRequestUbah(form, asalData, layananId, total, jatuhTempo, maksAnggota)
        } else {
            null
        }
        // Mode ubah tanpa perubahan apa pun: tidak perlu request, langsung tutup form
        if (id != null && requestUbah == null) {
            uiState = form.copy(pesanSelesai = R.string.msg_tidak_ada_perubahan)
            return
        }

        uiState = form.copy(isMenyimpan = true, pesan = null)
        viewModelScope.launch {
            val hasil = if (id != null && requestUbah != null) {
                groupRepository.ubahGrup(id, requestUbah)
            } else {
                groupRepository.tambahGrup(
                    GroupRequest(
                        serviceId = layananId,
                        subscriptionId = form.langgananId,
                        name = form.nama.trim(),
                        totalCost = total,
                        billingCycle = form.siklus,
                        dueDay = jatuhTempo,
                        maxMembers = maksAnggota,
                        description = form.deskripsi.trim().ifEmpty { null }
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
    // Langganan yang dilepas dikirim JsonNull, deskripsi yang dikosongkan dikirim "".
    private fun buatRequestUbah(
        form: FormGrupUiState,
        asal: Group,
        layananId: Int,
        total: Long,
        jatuhTempo: Int,
        maksAnggota: Int
    ): UpdateGroupRequest? {
        val nama = form.nama.trim()
        val deskripsi = form.deskripsi.trim()
        val langganan: JsonElement? = when {
            form.langgananId == asal.subscriptionId -> null
            form.langgananId == null -> JsonNull
            else -> JsonPrimitive(form.langgananId)
        }
        val request = UpdateGroupRequest(
            serviceId = layananId.takeIf { it != asal.serviceId },
            subscriptionId = langganan,
            name = nama.takeIf { it != asal.name },
            totalCost = total.takeIf { it != asal.totalCost },
            billingCycle = form.siklus.takeIf { it != asal.billingCycle },
            dueDay = jatuhTempo.takeIf { it != asal.dueDay },
            maxMembers = maksAnggota.takeIf { it != asal.maxMembers },
            description = deskripsi.takeIf { it != asal.description.orEmpty() }
        )
        return request.takeIf { it != UpdateGroupRequest() }
    }

    // Isi form: mode ubah memakai data lama.
    // Layanan lama yang sudah nonaktif tetap dimasukkan ke pilihan supaya tetap tampil.
    private fun isiForm(layanan: List<ServiceSummary>, langganan: List<Subscription>, grup: Group?) {
        asal = grup
        if (grup == null) {
            uiState = uiState.copy(daftarLayanan = layanan, daftarLangganan = langganan)
            return
        }
        val layananLama = grup.service
        val pilihanLayanan = if (layananLama != null && layanan.none { it.id == layananLama.id }) {
            listOf(layananLama) + layanan
        } else {
            layanan
        }
        uiState = uiState.copy(
            daftarLayanan = pilihanLayanan,
            daftarLangganan = langganan,
            layananId = grup.serviceId,
            langgananId = grup.subscriptionId,
            nama = grup.name,
            totalBiaya = grup.totalCost.toString(),
            siklus = grup.billingCycle,
            jatuhTempo = grup.dueDay.toString(),
            maksAnggota = grup.maxMembers.toString(),
            deskripsi = grup.description.orEmpty()
        )
    }

    // Ubah state dan hapus error (lokal dan server) milik field yang diedit
    private fun ubah(vararg field: FieldGrup, perubahan: FormGrupUiState.() -> FormGrupUiState) {
        val baru = uiState.perubahan()
        uiState = baru.copy(
            errorLokal = baru.errorLokal - field.toSet(),
            errorServer = baru.errorServer - field.toSet()
        )
    }

    // errors[] dipetakan ke field form; sisanya (atau message umum) jadi Snackbar
    private fun terapkanError(hasil: HasilApi.Gagal): FormGrupUiState {
        val peta = hasil.petakanErrorField(FieldGrup.entries.map { it.api }.toSet())
        val perField = FieldGrup.entries
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

    // Validasi lokal, disamakan dengan aturan backend (group.validation.js)
    private fun validasi(): Boolean {
        val form = uiState
        val error = mutableMapOf<FieldGrup, Int>()

        if (form.layananId == null) error[FieldGrup.LAYANAN] = R.string.error_pilih_layanan

        val nama = form.nama.trim()
        when {
            nama.isEmpty() -> error[FieldGrup.NAMA] = R.string.error_wajib_diisi
            nama.length !in NAMA_MIN..NAMA_MAKS -> error[FieldGrup.NAMA] = R.string.error_nama_grup_rentang
        }

        val total = form.totalBiaya.toLongOrNull()
        when {
            form.totalBiaya.isEmpty() -> error[FieldGrup.TOTAL] = R.string.error_wajib_diisi
            total == null -> error[FieldGrup.TOTAL] = R.string.error_angka_tidak_valid
            total <= 0 -> error[FieldGrup.TOTAL] = R.string.error_harus_lebih_dari_nol
        }

        val jatuhTempo = form.jatuhTempo.toIntOrNull()
        when {
            form.jatuhTempo.isEmpty() -> error[FieldGrup.JATUH_TEMPO] = R.string.error_wajib_diisi
            jatuhTempo == null || jatuhTempo !in JATUH_TEMPO_MIN..JATUH_TEMPO_MAKS ->
                error[FieldGrup.JATUH_TEMPO] = R.string.error_jatuh_tempo_rentang
        }

        val maks = form.maksAnggota.toIntOrNull()
        when {
            form.maksAnggota.isEmpty() -> error[FieldGrup.MAKS_ANGGOTA] = R.string.error_wajib_diisi
            maks == null || maks !in ANGGOTA_MIN..ANGGOTA_MAKS ->
                error[FieldGrup.MAKS_ANGGOTA] = R.string.error_maks_anggota_rentang
        }

        if (form.deskripsi.trim().length > DESKRIPSI_MAKS) error[FieldGrup.DESKRIPSI] = R.string.error_deskripsi_panjang

        uiState = form.copy(errorLokal = error, errorServer = emptyMap())
        return error.isEmpty()
    }

    companion object {
        // Nama argumen navigasi, dipakai juga oleh Destinasi
        const val ARG_ID = "id"

        private const val NAMA_MIN = 3
        private const val NAMA_MAKS = 50
        private const val TOTAL_MAKS_DIGIT = 12
        private const val ANGKA_KECIL_MAKS_DIGIT = 2
        private const val JATUH_TEMPO_MIN = 1
        private const val JATUH_TEMPO_MAKS = 28
        private const val ANGGOTA_MIN = 2
        private const val ANGGOTA_MAKS = 20
        private const val DESKRIPSI_MAKS = 255
    }
}
