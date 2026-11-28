package com.example.subtrack.view.grup

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.Group
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.anggota.BagianAnggota
import com.example.subtrack.view.components.AksiTopAppBar
import com.example.subtrack.view.components.BarisInfo
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.anggota.DaftarAnggotaUiState
import com.example.subtrack.viewmodel.anggota.DaftarAnggotaViewModel
import com.example.subtrack.viewmodel.grup.DetailGrupUiState
import com.example.subtrack.viewmodel.grup.DetailGrupViewModel

// Detail satu grup patungan: ringkasan iuran, informasi grup, dan daftar anggota.
// onUbah dan onTambahAnggota menerima id grup; onUbahAnggota menerima id grup dan id anggota.
// onAnggotaBerubah dipanggil saat status iuran diubah atau anggota dihapus langsung di layar ini.
// onSelesai menerima pesan (@StringRes) setelah hapus grup berhasil.
// pesanHasil = pesan dari form (grup/anggota) yang baru ditutup; detail dan anggota dimuat ulang lalu pesan ditampilkan.
@Composable
fun DetailGrupScreen(
    onKembali: () -> Unit,
    onUbah: (Int) -> Unit,
    onTambahAnggota: (Int) -> Unit,
    onUbahAnggota: (Int, Int) -> Unit,
    onAnggotaBerubah: () -> Unit,
    onSelesai: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailGrupViewModel = viewModel(factory = AppViewModelProvider.Factory),
    anggotaViewModel: DaftarAnggotaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state = viewModel.uiState

    // Hapus berhasil -> tutup detail dan kirim pesan ke daftar
    LaunchedEffect(viewModel.pesanSelesai) {
        viewModel.pesanSelesai?.let(onSelesai)
    }

    // Kembali dari form ubah grup atau form anggota -> tampilkan pesan, muat ulang detail dan anggota
    LaunchedEffect(pesanHasil) {
        if (pesanHasil != null) {
            viewModel.terimaHasil(pesanHasil)
            anggotaViewModel.muatUlang()
            onPesanHasilDiterima()
        }
    }

    // Anggota berubah di layar ini (status iuran / hapus) -> muat ulang summary supaya progres iuran ikut berubah
    LaunchedEffect(anggotaViewModel.adaPerubahan) {
        if (anggotaViewModel.adaPerubahan) {
            viewModel.muatUlang()
            onAnggotaBerubah()
            anggotaViewModel.perubahanDiterima()
        }
    }

    val teksPesan = viewModel.pesan?.teks()
    LaunchedEffect(teksPesan) {
        if (teksPesan != null) {
            snackbarHostState.showSnackbar(teksPesan)
            viewModel.hapusPesan()
        }
    }

    // Pesan dari daftar anggota (ubah status atau hapus)
    val teksPesanAnggota = anggotaViewModel.pesan?.teks()
    LaunchedEffect(teksPesanAnggota) {
        if (teksPesanAnggota != null) {
            snackbarHostState.showSnackbar(teksPesanAnggota)
            anggotaViewModel.hapusPesan()
        }
    }

    Scaffold(
        topBar = {
            SubTrackTopAppBar(
                titleRes = R.string.title_detail_grup,
                onKembali = onKembali,
                aksi = {
                    // Tombol aksi hanya muncul setelah data berhasil dimuat
                    if (state is DetailGrupUiState.Success) {
                        AksiTopAppBar(
                            ikon = Icons.Filled.Edit,
                            contentDescriptionRes = R.string.cd_edit,
                            onClick = { onUbah(state.grup.id) },
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
            is DetailGrupUiState.Loading -> LoadingView(modifier = modifierIsi)
            is DetailGrupUiState.Error -> ErrorView(
                pesan = state.pesan.teks(),
                onCobaLagi = viewModel::muat,
                modifier = modifierIsi
            )
            is DetailGrupUiState.Success -> IsiDetailGrup(
                grup = state.grup,
                bagianAnggota = {
                    // Jumlah anggota dari daftar terbaru; selama daftar belum termuat pakai summary grup
                    val daftar = anggotaViewModel.uiState as? DaftarAnggotaUiState.Success
                    BagianAnggota(
                        viewModel = anggotaViewModel,
                        jumlahAnggota = daftar?.anggota?.size ?: state.grup.summary.memberCount,
                        maksAnggota = state.grup.maxMembers,
                        onTambah = { onTambahAnggota(state.grup.id) },
                        onUbah = { idAnggota -> onUbahAnggota(state.grup.id, idAnggota) }
                    )
                },
                modifier = modifierIsi
            )
        }
    }

    val grup = (state as? DetailGrupUiState.Success)?.grup
    if (viewModel.tampilDialogHapus && grup != null) {
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_grup, grup.name),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::tutupDialogHapus
        )
    }
}

@Composable
private fun IsiDetailGrup(
    grup: Group,
    bagianAnggota: @Composable () -> Unit,
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
            KartuRingkasanIuran(grup)
            KartuInformasiGrup(grup)
            bagianAnggota()
        }
    }
}

// Ringkasan di atas: nama grup, layanan, progres iuran, lalu iuran saran, terkumpul, dan sisa dari summary
@Composable
private fun KartuRingkasanIuran(grup: Group) {
    val ringkasan = grup.summary
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(text = grup.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = grup.service?.name ?: stringResource(R.string.nilai_kosong),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ProgresIuran(grup = grup)
            Text(
                text = stringResource(R.string.label_ringkasan_iuran),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
            )
            Column {
                BarisInfo(R.string.label_iuran_disarankan, teksRupiah(ringkasan.suggestedShare))
                BarisInfo(R.string.label_terkumpul, teksRupiah(ringkasan.collected))
                BarisInfo(R.string.label_sisa, teksRupiah(ringkasan.remaining), garisBawah = false)
            }
        }
    }
}

// Semua field grup dalam bentuk baris label: nilai
@Composable
private fun KartuInformasiGrup(grup: Group) {
    val kosong = stringResource(R.string.nilai_kosong)
    // Langganan terkait ditampilkan nama paketnya (response grup tidak menyertakan harga)
    val langganan = grup.subscription?.let { it.planName ?: kosong } ?: stringResource(R.string.label_tanpa_langganan)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Text(
                text = stringResource(R.string.label_info_grup),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small))
            )
            BarisInfo(R.string.label_layanan, grup.service?.name ?: kosong)
            BarisInfo(R.string.label_langganan_terkait_singkat, langganan)
            BarisInfo(R.string.label_total_biaya, teksRupiah(grup.totalCost))
            BarisInfo(R.string.label_siklus, stringResource(grup.billingCycle.label))
            BarisInfo(R.string.label_jatuh_tempo, stringResource(R.string.format_tanggal_tiap_bulan, grup.dueDay))
            BarisInfo(
                R.string.label_jumlah_anggota,
                stringResource(R.string.format_jumlah_anggota, grup.summary.memberCount, grup.maxMembers)
            )
            BarisInfo(
                R.string.label_deskripsi,
                grup.description?.takeIf { it.isNotBlank() } ?: kosong,
                garisBawah = false
            )
        }
    }
}
