package com.example.subtrack.view.anggota

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
import com.example.subtrack.model.dto.DuesStatus
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.FieldPilihanEnum
import com.example.subtrack.view.components.FieldTanggal
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.view.components.FieldUang
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.anggota.FieldAnggota
import com.example.subtrack.viewmodel.anggota.FormAnggotaViewModel
import com.example.subtrack.viewmodel.anggota.MuatFormAnggotaState

// Form tambah/ubah anggota grup patungan.
// onSelesai menerima pesan (@StringRes) untuk ditampilkan di layar detail grup.
@Composable
fun FormAnggotaScreen(
    onKembali: () -> Unit,
    onSelesai: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormAnggotaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.pesanSelesai) {
        uiState.pesanSelesai?.let(onSelesai)
    }

    // Error simpan, termasuk 409 grup penuh dari server
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
                titleRes = if (viewModel.modeUbah) R.string.title_edit_anggota else R.string.title_tambah_anggota,
                onKembali = onKembali
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (val muat = viewModel.muatState) {
            is MuatFormAnggotaState.Loading -> LoadingView(modifier = modifierIsi)
            is MuatFormAnggotaState.Error -> ErrorView(
                pesan = muat.pesan.teks(),
                onCobaLagi = viewModel::muatData,
                modifier = modifierIsi
            )
            is MuatFormAnggotaState.Siap -> IsiFormAnggota(viewModel = viewModel, modifier = modifierIsi)
        }
    }
}

@Composable
private fun IsiFormAnggota(viewModel: FormAnggotaViewModel, modifier: Modifier = Modifier) {
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
            FieldTeks(
                value = uiState.nama,
                onValueChange = viewModel::onNamaChange,
                labelRes = R.string.label_nama_anggota,
                errorServer = errorServer[FieldAnggota.NAMA],
                errorRes = errorLokal[FieldAnggota.NAMA],
                enabled = aktif
            )
            FieldTeks(
                value = uiState.kontak,
                onValueChange = viewModel::onKontakChange,
                labelRes = R.string.label_kontak,
                errorServer = errorServer[FieldAnggota.KONTAK],
                errorRes = errorLokal[FieldAnggota.KONTAK],
                supportingRes = R.string.hint_kontak,
                enabled = aktif
            )
            // Saat tambah boleh kosong: server memakai total biaya dibagi maksimal anggota
            FieldUang(
                value = uiState.iuran,
                onValueChange = viewModel::onIuranChange,
                labelRes = R.string.label_iuran,
                errorServer = errorServer[FieldAnggota.IURAN],
                errorRes = errorLokal[FieldAnggota.IURAN],
                supportingRes = if (viewModel.modeUbah) null else R.string.hint_iuran,
                enabled = aktif
            )
            FieldPilihanEnum(
                terpilih = uiState.status,
                onPilih = viewModel::onStatusChange,
                labelRes = R.string.label_status_iuran,
                errorServer = errorServer[FieldAnggota.STATUS],
                enabled = aktif
            )
            // Tanggal bayar hanya berlaku untuk anggota yang sudah lunas
            if (uiState.status == DuesStatus.LUNAS) {
                FieldTanggal(
                    value = uiState.tanggalBayar,
                    onValueChange = viewModel::onTanggalBayarChange,
                    labelRes = R.string.label_tanggal_bayar,
                    errorServer = errorServer[FieldAnggota.TANGGAL],
                    errorRes = errorLokal[FieldAnggota.TANGGAL],
                    supportingRes = R.string.hint_tanggal_bayar_anggota,
                    enabled = aktif,
                    bolehKosong = true
                )
            }
            FieldTeks(
                value = uiState.catatan,
                onValueChange = viewModel::onCatatanChange,
                labelRes = R.string.label_catatan,
                errorServer = errorServer[FieldAnggota.CATATAN],
                errorRes = errorLokal[FieldAnggota.CATATAN],
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
