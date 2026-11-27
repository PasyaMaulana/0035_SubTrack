package com.example.subtrack.view.langganan

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.util.tanggalTampilan
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.AksiTopAppBar
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.StatusLanggananChip
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.view.pembayaran.BagianRiwayatPembayaran
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.langganan.DetailLanggananUiState
import com.example.subtrack.viewmodel.langganan.DetailLanggananViewModel
import com.example.subtrack.viewmodel.pembayaran.RiwayatPembayaranViewModel

// Detail satu langganan: ringkasan, informasi lengkap, dan riwayat pembayaran.
// onUbah menerima id langganan; onSelesai menerima pesan (@StringRes) setelah hapus berhasil.
// onTambahPembayaran menerima id langganan; onUbahPembayaran menerima id langganan dan id pembayaran.
// pesanHasil = pesan dari form (langganan/pembayaran) yang baru ditutup; data dimuat ulang lalu pesan ditampilkan.
@Composable
fun DetailLanggananScreen(
    onKembali: () -> Unit,
    onUbah: (Int) -> Unit,
    onTambahPembayaran: (Int) -> Unit,
    onUbahPembayaran: (Int, Int) -> Unit,
    onSelesai: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailLanggananViewModel = viewModel(factory = AppViewModelProvider.Factory),
    riwayatViewModel: RiwayatPembayaranViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.uiState

    // Hapus berhasil -> tutup detail dan kirim pesan ke daftar
    LaunchedEffect(viewModel.pesanSelesai) {
        viewModel.pesanSelesai?.let(onSelesai)
    }

    // Kembali dari form ubah langganan atau form pembayaran -> tampilkan pesan, muat ulang detail dan riwayat
    LaunchedEffect(pesanHasil) {
        if (pesanHasil != null) {
            viewModel.terimaHasil(pesanHasil)
            riwayatViewModel.muatUlang()
            onPesanHasilDiterima()
        }
    }

    val teksPesan = viewModel.pesan?.teks()
    LaunchedEffect(teksPesan) {
        if (teksPesan != null) {
            snackbarHostState.showSnackbar(teksPesan)
            viewModel.hapusPesan()
        }
    }

    // Pesan dari riwayat pembayaran (hapus berhasil/gagal)
    val teksPesanRiwayat = riwayatViewModel.pesan?.teks()
    LaunchedEffect(teksPesanRiwayat) {
        if (teksPesanRiwayat != null) {
            snackbarHostState.showSnackbar(teksPesanRiwayat)
            riwayatViewModel.hapusPesan()
        }
    }

    Scaffold(
        topBar = {
            SubTrackTopAppBar(
                titleRes = R.string.title_detail_langganan,
                onKembali = onKembali,
                aksi = {
                    // Tombol aksi hanya muncul setelah data berhasil dimuat
                    if (state is DetailLanggananUiState.Success) {
                        AksiTopAppBar(
                            ikon = Icons.Filled.Edit,
                            contentDescriptionRes = R.string.cd_edit,
                            onClick = { onUbah(state.langganan.id) },
                            enabled = !viewModel.isMenghapus
                        )
                        if (viewModel.isMenghapus) {
                            CircularProgressIndicator(
                                strokeWidth = dimensionResource(R.dimen.progress_stroke),
                                modifier = Modifier
                                    .padding(dimensionResource(R.dimen.padding_medium))
                                    .size(dimensionResource(R.dimen.icon_small))
                            )
                        } else {
                            AksiTopAppBar(
                                ikon = Icons.Filled.DeleteOutline,
                                contentDescriptionRes = R.string.cd_hapus,
                                onClick = viewModel::tampilkanDialogHapus
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val modifierIsi = Modifier.padding(innerPadding)
        when (state) {
            is DetailLanggananUiState.Loading -> LoadingView(modifier = modifierIsi)
            is DetailLanggananUiState.Error -> ErrorView(
                pesan = state.pesan.teks(),
                onCobaLagi = viewModel::muat,
                modifier = modifierIsi
            )
            is DetailLanggananUiState.Success -> IsiDetail(
                langganan = state.langganan,
                riwayatPembayaran = {
                    BagianRiwayatPembayaran(
                        viewModel = riwayatViewModel,
                        onTambah = { onTambahPembayaran(state.langganan.id) },
                        onUbah = { idPembayaran -> onUbahPembayaran(state.langganan.id, idPembayaran) }
                    )
                },
                modifier = modifierIsi
            )
        }
    }

    val langganan = (state as? DetailLanggananUiState.Success)?.langganan
    if (viewModel.tampilDialogHapus && langganan != null) {
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_langganan, langganan.service?.name ?: langganan.planName),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::tutupDialogHapus
        )
    }
}

@Composable
private fun IsiDetail(
    langganan: Subscription,
    riwayatPembayaran: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
            modifier = Modifier.widthIn(max = dimensionResource(R.dimen.form_max_width))
        ) {
            KartuRingkasan(langganan)
            KartuInformasi(langganan)
            riwayatPembayaran()
        }
    }
}

// Ringkasan di atas: nama layanan, kategori, status, dan harga per siklus
@Composable
private fun KartuRingkasan(langganan: Subscription) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = langganan.service?.name ?: stringResource(R.string.nilai_kosong),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    langganan.service?.let { layanan ->
                        Text(
                            text = stringResource(layanan.category.label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusLanggananChip(status = langganan.status)
            }
            Text(
                text = stringResource(
                    R.string.format_harga_siklus,
                    teksRupiah(langganan.price),
                    stringResource(langganan.billingCycle.label)
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Semua field langganan dalam bentuk baris label: nilai
@Composable
private fun KartuInformasi(langganan: Subscription) {
    val kosong = stringResource(R.string.nilai_kosong)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Text(
                text = stringResource(R.string.label_info_langganan),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small))
            )
            BarisInfo(R.string.label_nama_paket, langganan.planName)
            BarisInfo(R.string.label_harga, teksRupiah(langganan.price))
            BarisInfo(R.string.label_siklus, stringResource(langganan.billingCycle.label))
            BarisInfo(R.string.label_status, stringResource(langganan.status.label))
            BarisInfo(R.string.label_tanggal_mulai, tanggalTampilan(langganan.startDate))
            BarisInfo(
                R.string.label_tagihan_berikutnya,
                langganan.nextBillingDate?.let { tanggalTampilan(it) } ?: kosong
            )
            BarisInfo(
                R.string.label_metode_pembayaran,
                langganan.paymentMethod?.takeIf { it.isNotBlank() } ?: kosong
            )
            BarisInfo(
                R.string.label_catatan,
                langganan.notes?.takeIf { it.isNotBlank() } ?: kosong,
                garisBawah = false
            )
        }
    }
}

@Composable
private fun BarisInfo(@StringRes labelRes: Int, nilai: String, garisBawah: Boolean = true) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small))
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    if (garisBawah) HorizontalDivider()
}
