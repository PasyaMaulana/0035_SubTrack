package com.example.subtrack.view.katalog

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.Category
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.FieldPilihanEnum
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.katalog.FormLayananUiState
import com.example.subtrack.viewmodel.katalog.FormLayananViewModel
import com.example.subtrack.viewmodel.katalog.MuatLayananState

// Form tambah/ubah layanan katalog (admin). Mode ubah punya tombol hapus dengan konfirmasi.
// onSelesai menerima pesan (@StringRes) untuk ditampilkan di layar katalog.
@Composable
fun FormLayananScreen(
    onKembali: () -> Unit,
    onSelesai: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormLayananViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    // Simpan/hapus berhasil -> tutup form dan kirim pesan ke katalog
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
                titleRes = if (viewModel.modeUbah) R.string.title_edit_layanan else R.string.title_tambah_layanan,
                onKembali = onKembali
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (val muat = viewModel.muatState) {
            is MuatLayananState.Loading -> LoadingView(modifier = modifierIsi)
            is MuatLayananState.Error -> ErrorView(
                pesan = muat.pesan.teks(),
                onCobaLagi = viewModel::muatDetail,
                modifier = modifierIsi
            )
            is MuatLayananState.Siap -> IsiFormLayanan(
                uiState = uiState,
                modeUbah = viewModel.modeUbah,
                onNamaChange = viewModel::onNamaChange,
                onKategoriChange = viewModel::onKategoriChange,
                onDeskripsiChange = viewModel::onDeskripsiChange,
                onWebsiteChange = viewModel::onWebsiteChange,
                onAktifChange = viewModel::onAktifChange,
                onSimpan = viewModel::simpan,
                onHapus = viewModel::tampilkanDialogHapus,
                modifier = modifierIsi
            )
        }
    }

    if (uiState.tampilDialogHapus) {
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_layanan, uiState.nama),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::tutupDialogHapus
        )
    }
}

@Composable
private fun IsiFormLayanan(
    uiState: FormLayananUiState,
    modeUbah: Boolean,
    onNamaChange: (String) -> Unit,
    onKategoriChange: (Category) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onWebsiteChange: (String) -> Unit,
    onAktifChange: (Boolean) -> Unit,
    onSimpan: () -> Unit,
    onHapus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val aktif = !uiState.sedangProses

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
                onValueChange = onNamaChange,
                labelRes = R.string.label_nama_layanan,
                errorServer = uiState.errorNamaServer,
                errorRes = uiState.errorNama,
                enabled = aktif
            )
            FieldPilihanEnum(
                terpilih = uiState.kategori,
                onPilih = onKategoriChange,
                labelRes = R.string.label_kategori,
                errorServer = uiState.errorKategoriServer,
                errorRes = uiState.errorKategori,
                enabled = aktif
            )
            FieldTeks(
                value = uiState.deskripsi,
                onValueChange = onDeskripsiChange,
                labelRes = R.string.label_deskripsi,
                errorServer = uiState.errorDeskripsiServer,
                errorRes = uiState.errorDeskripsi,
                enabled = aktif,
                singleLine = false,
                minLines = BARIS_DESKRIPSI,
                imeAction = ImeAction.Default
            )
            FieldTeks(
                value = uiState.website,
                onValueChange = onWebsiteChange,
                labelRes = R.string.label_website,
                errorServer = uiState.errorWebsiteServer,
                errorRes = uiState.errorWebsite,
                supportingRes = R.string.hint_website,
                enabled = aktif,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            )
            BarisAktif(aktif = uiState.aktif, onAktifChange = onAktifChange, enabled = aktif)
            TombolUtama(
                teksRes = R.string.btn_simpan,
                isLoading = uiState.isMenyimpan,
                onClick = {
                    focusManager.clearFocus()
                    onSimpan()
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (modeUbah) {
                TombolHapus(
                    teksRes = R.string.btn_hapus,
                    isLoading = uiState.isMenghapus,
                    enabled = aktif,
                    onClick = onHapus
                )
            }
        }
    }
}

// Baris saklar status aktif; seluruh baris bisa diketuk
@Composable
private fun BarisAktif(aktif: Boolean, onAktifChange: (Boolean) -> Unit, enabled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = aktif, enabled = enabled, role = Role.Switch, onValueChange = onAktifChange)
            .padding(vertical = dimensionResource(R.dimen.padding_small))
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.label_aktif), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.label_status_layanan_aktif),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // onCheckedChange null karena klik sudah ditangani toggleable di Row
        Switch(checked = aktif, onCheckedChange = null, enabled = enabled)
    }
}

// Tombol hapus bergaris merah (aksi berbahaya)
@Composable
private fun TombolHapus(
    @StringRes teksRes: Int,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        border = BorderStroke(dimensionResource(R.dimen.border_width), MaterialTheme.colorScheme.error),
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.button_height))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = dimensionResource(R.dimen.progress_stroke),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_medium))
            )
        } else {
            Text(stringResource(teksRes))
        }
    }
}

// Tinggi awal field deskripsi (jumlah baris)
private const val BARIS_DESKRIPSI = 3
