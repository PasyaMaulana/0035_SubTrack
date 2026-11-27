package com.example.subtrack.view.langganan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.BillingCycle
import com.example.subtrack.model.dto.SubscriptionStatus
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.FieldPilihan
import com.example.subtrack.view.components.FieldPilihanEnum
import com.example.subtrack.view.components.FieldTanggal
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.view.components.FieldUang
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.langganan.FieldLangganan
import com.example.subtrack.viewmodel.langganan.FormLanggananViewModel
import com.example.subtrack.viewmodel.langganan.MuatFormLanggananState

// Form tambah/ubah langganan (user). Status hanya bisa diubah di mode ubah;
// saat tambah, langganan baru selalu berstatus aktif.
// onSelesai menerima pesan (@StringRes) untuk ditampilkan di layar sebelumnya.
@Composable
fun FormLanggananScreen(
    onKembali: () -> Unit,
    onSelesai: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormLanggananViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Simpan berhasil -> tutup form dan kirim pesan ke layar sebelumnya
    LaunchedEffect(uiState.pesanSelesai) {
        uiState.pesanSelesai?.let(onSelesai)
    }

    val teksPesan = uiState.pesan?.teks()
    LaunchedEffect(teksPesan) {
        if (teksPesan != null) {
            snackbarHostState.showSnackbar(teksPesan)
            viewModel.hapusPesan()
        }
    }

    Scaffold(
        topBar = {
            SubTrackTopAppBar(
                titleRes = if (viewModel.modeUbah) R.string.title_edit_langganan else R.string.title_tambah_langganan,
                onKembali = onKembali
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (val muat = viewModel.muatState) {
            is MuatFormLanggananState.Loading -> LoadingView(modifier = modifierIsi)
            is MuatFormLanggananState.Error -> ErrorView(
                pesan = muat.pesan.teks(),
                onCobaLagi = viewModel::muatData,
                modifier = modifierIsi
            )
            is MuatFormLanggananState.Siap -> IsiFormLangganan(viewModel = viewModel, modifier = modifierIsi)
        }
    }
}

@Composable
private fun IsiFormLangganan(viewModel: FormLanggananViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    val aktif = !uiState.isMenyimpan
    // Error yang ditampilkan per field: server didahulukan oleh komponen field
    val errorServer = uiState.errorServer
    val errorLokal = uiState.errorLokal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.widthIn(max = dimensionResource(R.dimen.form_max_width))
        ) {
            FieldPilihan(
                pilihan = uiState.daftarLayanan,
                terpilih = uiState.layananTerpilih,
                onPilih = viewModel::onLayananChange,
                labelRes = R.string.label_layanan,
                teksPilihan = { it.name },
                errorServer = errorServer[FieldLangganan.LAYANAN],
                errorRes = errorLokal[FieldLangganan.LAYANAN],
                supportingRes = if (uiState.daftarLayanan.isEmpty()) R.string.hint_layanan_kosong else null,
                enabled = aktif
            )
            FieldTeks(
                value = uiState.namaPaket,
                onValueChange = viewModel::onNamaPaketChange,
                labelRes = R.string.label_nama_paket,
                errorServer = errorServer[FieldLangganan.PAKET],
                errorRes = errorLokal[FieldLangganan.PAKET],
                enabled = aktif
            )
            FieldUang(
                value = uiState.harga,
                onValueChange = viewModel::onHargaChange,
                labelRes = R.string.label_harga,
                errorServer = errorServer[FieldLangganan.HARGA],
                errorRes = errorLokal[FieldLangganan.HARGA],
                supportingRes = R.string.hint_harga,
                enabled = aktif
            )
            FieldPilihanEnum<BillingCycle>(
                terpilih = uiState.siklus,
                onPilih = viewModel::onSiklusChange,
                labelRes = R.string.label_siklus,
                errorServer = errorServer[FieldLangganan.SIKLUS],
                errorRes = errorLokal[FieldLangganan.SIKLUS],
                enabled = aktif
            )
            FieldTanggal(
                value = uiState.tanggalMulai,
                onValueChange = viewModel::onTanggalMulaiChange,
                labelRes = R.string.label_tanggal_mulai,
                errorServer = errorServer[FieldLangganan.MULAI],
                errorRes = errorLokal[FieldLangganan.MULAI],
                enabled = aktif
            )
            // Saat tambah boleh dikosongkan (dihitung server). Saat ubah server tidak menghitung ulang,
            // jadi tanggal lama wajib tetap ada dan tidak bisa dikosongkan.
            FieldTanggal(
                value = uiState.tagihanBerikutnya,
                onValueChange = viewModel::onTagihanBerikutnyaChange,
                labelRes = R.string.label_tagihan_berikutnya,
                errorServer = errorServer[FieldLangganan.TAGIHAN],
                errorRes = errorLokal[FieldLangganan.TAGIHAN],
                supportingRes = if (viewModel.modeUbah) null else R.string.hint_tagihan_berikutnya,
                enabled = aktif,
                bolehKosong = !viewModel.modeUbah
            )
            FieldTeks(
                value = uiState.metodePembayaran,
                onValueChange = viewModel::onMetodePembayaranChange,
                labelRes = R.string.label_metode_pembayaran,
                errorServer = errorServer[FieldLangganan.METODE],
                errorRes = errorLokal[FieldLangganan.METODE],
                supportingRes = R.string.hint_metode_pembayaran,
                enabled = aktif
            )
            if (viewModel.modeUbah) {
                FieldPilihanEnum<SubscriptionStatus>(
                    terpilih = uiState.status,
                    onPilih = viewModel::onStatusChange,
                    labelRes = R.string.label_status,
                    errorServer = errorServer[FieldLangganan.STATUS],
                    errorRes = errorLokal[FieldLangganan.STATUS],
                    enabled = aktif
                )
            }
            FieldTeks(
                value = uiState.catatan,
                onValueChange = viewModel::onCatatanChange,
                labelRes = R.string.label_catatan,
                errorServer = errorServer[FieldLangganan.CATATAN],
                errorRes = errorLokal[FieldLangganan.CATATAN],
                enabled = aktif,
                singleLine = false,
                minLines = BARIS_CATATAN,
                imeAction = ImeAction.Default
            )
            TombolUtama(
                teksRes = R.string.btn_simpan,
                isLoading = uiState.isMenyimpan,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.simpan()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Tinggi awal field catatan (jumlah baris)
private const val BARIS_CATATAN = 3
