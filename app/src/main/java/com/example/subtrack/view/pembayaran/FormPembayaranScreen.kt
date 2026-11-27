package com.example.subtrack.view.pembayaran

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.subtrack.R
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.FieldTanggal
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.view.components.FieldUang
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.pembayaran.FieldPembayaran
import com.example.subtrack.viewmodel.pembayaran.FormPembayaranViewModel
import com.example.subtrack.viewmodel.pembayaran.MuatFormPembayaranState

// Form tambah/ubah catatan pembayaran satu langganan, termasuk foto bukti opsional.
// onSelesai menerima pesan (@StringRes) untuk ditampilkan di layar detail langganan.
@Composable
fun FormPembayaranScreen(
    onKembali: () -> Unit,
    onSelesai: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormPembayaranViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

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
                titleRes = if (viewModel.modeUbah) R.string.title_edit_pembayaran else R.string.title_tambah_pembayaran,
                onKembali = onKembali
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (val muat = viewModel.muatState) {
            is MuatFormPembayaranState.Loading -> LoadingView(modifier = modifierIsi)
            is MuatFormPembayaranState.Error -> ErrorView(
                pesan = muat.pesan.teks(),
                onCobaLagi = viewModel::muatData,
                modifier = modifierIsi
            )
            is MuatFormPembayaranState.Siap -> IsiFormPembayaran(viewModel = viewModel, modifier = modifierIsi)
        }
    }
}

@Composable
private fun IsiFormPembayaran(viewModel: FormPembayaranViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current
    val aktif = !uiState.isMenyimpan
    val errorServer = uiState.errorServer
    val errorLokal = uiState.errorLokal

    // Photo Picker sistem: tidak butuh izin baca penyimpanan
    val pemilihFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.onFotoDipilih(uri)
    }
    val bukaPemilihFoto = {
        pemilihFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

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
            FieldUang(
                value = uiState.nominal,
                onValueChange = viewModel::onNominalChange,
                labelRes = R.string.label_nominal,
                errorServer = errorServer[FieldPembayaran.NOMINAL],
                errorRes = errorLokal[FieldPembayaran.NOMINAL],
                supportingRes = if (viewModel.modeUbah) null else R.string.hint_nominal_default,
                enabled = aktif
            )
            FieldTanggal(
                value = uiState.tanggalBayar,
                onValueChange = viewModel::onTanggalBayarChange,
                labelRes = R.string.label_tanggal_bayar,
                errorServer = errorServer[FieldPembayaran.TANGGAL],
                errorRes = errorLokal[FieldPembayaran.TANGGAL],
                enabled = aktif
            )
            FieldTeks(
                value = uiState.metode,
                onValueChange = viewModel::onMetodeChange,
                labelRes = R.string.label_metode_pembayaran,
                errorServer = errorServer[FieldPembayaran.METODE],
                errorRes = errorLokal[FieldPembayaran.METODE],
                supportingRes = R.string.hint_metode_pembayaran,
                enabled = aktif
            )
            FieldTeks(
                value = uiState.catatan,
                onValueChange = viewModel::onCatatanChange,
                labelRes = R.string.label_catatan,
                errorServer = errorServer[FieldPembayaran.CATATAN],
                errorRes = errorLokal[FieldPembayaran.CATATAN],
                enabled = aktif,
                singleLine = false,
                minLines = BARIS_CATATAN,
                imeAction = ImeAction.Default
            )
            BagianFotoBukti(
                namaFoto = uiState.fotoBukti,
                fileFoto = { viewModel.fileBukti(it) },
                isMemproses = uiState.isMemprosesFoto,
                enabled = aktif,
                onPilih = bukaPemilihFoto,
                onHapus = viewModel::hapusFoto
            )
            TombolUtama(
                teksRes = R.string.btn_simpan,
                isLoading = uiState.isMenyimpan,
                // Simpan diabaikan ViewModel selama foto masih disalin
                onClick = {
                    focusManager.clearFocus()
                    viewModel.simpan()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Foto bukti: tombol pilih jika belum ada, atau pratinjau dengan tombol ganti dan hapus
@Composable
private fun BagianFotoBukti(
    namaFoto: String?,
    fileFoto: (String) -> java.io.File?,
    isMemproses: Boolean,
    enabled: Boolean,
    onPilih: () -> Unit,
    onHapus: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
        Text(
            text = stringResource(R.string.label_bukti),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(R.string.hint_bukti),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when {
            isMemproses -> OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.proof_image_height))
                ) {
                    CircularProgressIndicator()
                }
            }
            namaFoto != null -> {
                val file = remember(namaFoto) { fileFoto(namaFoto) }
                PratinjauFoto(file = file)
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                    OutlinedButton(onClick = onPilih, enabled = enabled) {
                        IkonTombol(Icons.Filled.Image)
                        Text(stringResource(R.string.btn_ganti_foto))
                    }
                    TextButton(
                        onClick = onHapus,
                        enabled = enabled,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        IkonTombol(Icons.Filled.DeleteOutline)
                        Text(stringResource(R.string.btn_hapus_foto))
                    }
                }
            }
            else -> OutlinedButton(onClick = onPilih, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                IkonTombol(Icons.Filled.AddPhotoAlternate)
                Text(stringResource(R.string.btn_pilih_foto))
            }
        }
    }
}

// Pratinjau foto dari filesDir/bukti/. file null = foto lama tidak ada di perangkat ini.
@Composable
private fun PratinjauFoto(file: java.io.File?) {
    val bentuk = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    OutlinedCard(shape = bentuk, modifier = Modifier.fillMaxWidth()) {
        if (file != null) {
            AsyncImage(
                model = file,
                contentDescription = stringResource(R.string.cd_bukti_pembayaran),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Filled.BrokenImage),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.proof_image_height))
                    .clip(bentuk)
            )
        } else {
            Text(
                text = stringResource(R.string.msg_bukti_tidak_ditemukan),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
            )
        }
    }
}

@Composable
private fun IkonTombol(ikon: ImageVector) {
    Icon(
        imageVector = ikon,
        contentDescription = null,
        modifier = Modifier
            .padding(end = dimensionResource(R.dimen.padding_small))
            .size(dimensionResource(R.dimen.icon_small))
    )
}

// Tinggi awal field catatan (jumlah baris)
private const val BARIS_CATATAN = 3
