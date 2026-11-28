package com.example.subtrack.view.grup

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.BillingCycle
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.FieldPilihan
import com.example.subtrack.view.components.FieldPilihanEnum
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.view.components.FieldUang
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.grup.FieldGrup
import com.example.subtrack.viewmodel.grup.FormGrupViewModel
import com.example.subtrack.viewmodel.grup.MuatFormGrupState

// Form tambah/ubah grup patungan.
// onSelesai menerima pesan (@StringRes) untuk ditampilkan di layar sebelumnya.
@Composable
fun FormGrupScreen(
    onKembali: () -> Unit,
    onSelesai: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormGrupViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                titleRes = if (viewModel.modeUbah) R.string.title_edit_grup else R.string.title_tambah_grup,
                onKembali = onKembali
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (val muat = viewModel.muatState) {
            is MuatFormGrupState.Loading -> LoadingView(modifier = modifierIsi)
            is MuatFormGrupState.Error -> ErrorView(
                pesan = muat.pesan.teks(),
                onCobaLagi = viewModel::muatData,
                modifier = modifierIsi
            )
            is MuatFormGrupState.Siap -> IsiFormGrup(viewModel = viewModel, modifier = modifierIsi)
        }
    }
}

@Composable
private fun IsiFormGrup(viewModel: FormGrupViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    val aktif = !uiState.isMenyimpan
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
            // Langganan di atas: memilihnya mengisi layanan dan total biaya otomatis
            FieldPilihan(
                pilihan = uiState.daftarLangganan,
                terpilih = uiState.langgananTerpilih,
                onPilih = viewModel::onLanggananChange,
                labelRes = R.string.label_langganan_terkait,
                teksPilihan = { langganan ->
                    stringResource(
                        R.string.format_langganan_pilihan,
                        langganan.service?.name ?: stringResource(R.string.nilai_kosong),
                        langganan.planName
                    )
                },
                errorServer = errorServer[FieldGrup.LANGGANAN],
                errorRes = errorLokal[FieldGrup.LANGGANAN],
                supportingRes = R.string.hint_langganan_terkait,
                enabled = aktif,
                opsiKosongRes = R.string.label_tanpa_langganan,
                onPilihKosong = viewModel::onTanpaLangganan
            )
            FieldPilihan(
                pilihan = uiState.daftarLayanan,
                terpilih = uiState.layananTerpilih,
                onPilih = viewModel::onLayananChange,
                labelRes = R.string.label_layanan,
                teksPilihan = { it.name },
                errorServer = errorServer[FieldGrup.LAYANAN],
                errorRes = errorLokal[FieldGrup.LAYANAN],
                supportingRes = if (uiState.daftarLayanan.isEmpty()) R.string.hint_layanan_kosong else null,
                enabled = aktif
            )
            FieldTeks(
                value = uiState.nama,
                onValueChange = viewModel::onNamaChange,
                labelRes = R.string.label_nama_grup,
                errorServer = errorServer[FieldGrup.NAMA],
                errorRes = errorLokal[FieldGrup.NAMA],
                enabled = aktif
            )
            FieldUang(
                value = uiState.totalBiaya,
                onValueChange = viewModel::onTotalBiayaChange,
                labelRes = R.string.label_total_biaya,
                errorServer = errorServer[FieldGrup.TOTAL],
                errorRes = errorLokal[FieldGrup.TOTAL],
                enabled = aktif
            )
            FieldPilihanEnum<BillingCycle>(
                terpilih = uiState.siklus,
                onPilih = viewModel::onSiklusChange,
                labelRes = R.string.label_siklus,
                errorServer = errorServer[FieldGrup.SIKLUS],
                errorRes = errorLokal[FieldGrup.SIKLUS],
                enabled = aktif
            )
            FieldTeks(
                value = uiState.jatuhTempo,
                onValueChange = viewModel::onJatuhTempoChange,
                labelRes = R.string.label_tanggal_jatuh_tempo,
                errorServer = errorServer[FieldGrup.JATUH_TEMPO],
                errorRes = errorLokal[FieldGrup.JATUH_TEMPO],
                supportingRes = R.string.hint_jatuh_tempo,
                enabled = aktif,
                keyboardType = KeyboardType.Number
            )
            FieldTeks(
                value = uiState.maksAnggota,
                onValueChange = viewModel::onMaksAnggotaChange,
                labelRes = R.string.label_maks_anggota,
                errorServer = errorServer[FieldGrup.MAKS_ANGGOTA],
                errorRes = errorLokal[FieldGrup.MAKS_ANGGOTA],
                supportingRes = R.string.hint_maks_anggota,
                enabled = aktif,
                keyboardType = KeyboardType.Number
            )
            FieldTeks(
                value = uiState.deskripsi,
                onValueChange = viewModel::onDeskripsiChange,
                labelRes = R.string.label_deskripsi,
                errorServer = errorServer[FieldGrup.DESKRIPSI],
                errorRes = errorLokal[FieldGrup.DESKRIPSI],
                enabled = aktif,
                singleLine = false,
                minLines = BARIS_DESKRIPSI,
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

// Tinggi awal field deskripsi (jumlah baris)
private const val BARIS_DESKRIPSI = 3
