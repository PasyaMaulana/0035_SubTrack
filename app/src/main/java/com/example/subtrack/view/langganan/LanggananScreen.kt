package com.example.subtrack.view.langganan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.Subscription
import com.example.subtrack.model.dto.SubscriptionStatus
import com.example.subtrack.util.hariIniApi
import com.example.subtrack.util.tanggalTampilan
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.AksiTopAppBar
import com.example.subtrack.view.components.BarisFilterChip
import com.example.subtrack.view.components.EmptyView
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.IsiPenuhBisaRefresh
import com.example.subtrack.view.components.KolomCari
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.StatusLanggananChip
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.langganan.LanggananUiState
import com.example.subtrack.viewmodel.langganan.LanggananViewModel

// Tab Langganan (user): cari nama layanan, filter status, daftar langganan.
// pesanHasil = pesan dari detail/form setelah hapus/simpan berhasil (dikirim lewat navigasi).
// perluMuatUlang = langganan diubah dari layar detail, daftar dimuat ulang tanpa pesan.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanggananScreen(
    onTambah: () -> Unit,
    onDetail: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    perluMuatUlang: Boolean,
    onMuatUlangDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LanggananViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Laporan CSV selesai dibuat -> buka chooser untuk dibagikan (WhatsApp, email, Drive, dll.)
    val laporanSiap = viewModel.laporanSiap
    LaunchedEffect(laporanSiap) {
        if (laporanSiap != null) {
            if (bagikanLaporan(context, laporanSiap)) viewModel.laporanDitangani()
            else viewModel.gagalMembagikan()
        }
    }

    // Hasil dari detail/form -> tampilkan pesan dan muat ulang daftar
    LaunchedEffect(pesanHasil) {
        if (pesanHasil != null) {
            viewModel.terimaHasil(pesanHasil)
            onPesanHasilDiterima()
        }
    }

    LaunchedEffect(perluMuatUlang) {
        if (perluMuatUlang) {
            viewModel.muatUlang()
            onMuatUlangDiterima()
        }
    }

    // Snackbar sekali tampil, lalu pesan dihapus dari ViewModel
    val teksPesan = viewModel.pesan?.teks()
    LaunchedEffect(teksPesan) {
        if (teksPesan != null) {
            snackbarHostState.showSnackbar(teksPesan)
            viewModel.hapusPesan()
        }
    }

    Scaffold(
        topBar = {
            SubTrackTopAppBar(
                titleRes = R.string.menu_langganan,
                aksi = { TombolEkspor(sedangEkspor = viewModel.sedangEkspor, onEkspor = viewModel::ekspor) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTambah) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_tambah))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            KolomCari(
                value = viewModel.kataKunci,
                onValueChange = viewModel::onKataKunciChange,
                hintRes = R.string.hint_cari_langganan
            )
            BarisFilterChip(
                pilihan = SubscriptionStatus.entries,
                terpilih = viewModel.status,
                onPilih = viewModel::onStatusChange
            )
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = viewModel.uiState) {
                    is LanggananUiState.Loading -> LoadingView()
                    is LanggananUiState.Error -> IsiPenuhBisaRefresh {
                        ErrorView(pesan = state.pesan.teks(), onCobaLagi = viewModel::cobaLagi)
                    }
                    is LanggananUiState.Success -> {
                        if (state.daftar.isEmpty()) {
                            IsiPenuhBisaRefresh {
                                LanggananKosong(viewModel.kataKunci, viewModel.status, onTambah)
                            }
                        } else {
                            DaftarLangganan(daftar = state.daftar, onDetail = onDetail)
                        }
                    }
                }
                // Indikator tipis saat memuat hasil cari/filter, daftar lama tetap tampil
                if (viewModel.sedangMemuat) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

// Tombol ekspor CSV di TopAppBar; selama laporan disiapkan diganti indikator loading
@Composable
private fun TombolEkspor(sedangEkspor: Boolean, onEkspor: () -> Unit) {
    if (sedangEkspor) {
        val teksMemuat = stringResource(R.string.msg_menyiapkan_laporan)
        // Ukuran sama dengan IconButton supaya TopAppBar tidak bergeser
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(dimensionResource(R.dimen.button_height))
        ) {
            CircularProgressIndicator(
                strokeWidth = dimensionResource(R.dimen.progress_stroke),
                modifier = Modifier
                    .size(dimensionResource(R.dimen.icon_medium))
                    .semantics { contentDescription = teksMemuat }
            )
        }
    } else {
        AksiTopAppBar(
            ikon = Icons.Filled.FileDownload,
            contentDescriptionRes = R.string.cd_ekspor,
            onClick = onEkspor
        )
    }
}

// Buka chooser Intent.ACTION_SEND untuk file CSV. false jika tidak ada aplikasi penerima.
private fun bagikanLaporan(context: Context, uri: Uri): Boolean {
    val kirim = Intent(Intent.ACTION_SEND).apply {
        type = TIPE_CSV
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.format_subjek_laporan, hariIniApi()))
        // Izin baca sementara untuk aplikasi penerima
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        context.startActivity(Intent.createChooser(kirim, context.getString(R.string.title_bagikan_laporan)))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

private const val TIPE_CSV = "text/csv"

@Composable
private fun DaftarLangganan(
    daftar: List<Subscription>,
    onDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = dimensionResource(R.dimen.padding_medium),
            end = dimensionResource(R.dimen.padding_medium),
            top = dimensionResource(R.dimen.padding_small),
            bottom = dimensionResource(R.dimen.list_bottom_padding_fab)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier.fillMaxSize()
    ) {
        items(daftar, key = { it.id }) { langganan ->
            KartuLangganan(langganan = langganan, onClick = { onDetail(langganan.id) })
        }
    }
}

// Kartu satu langganan: layanan, paket, harga/siklus, tagihan berikutnya, status
@Composable
private fun KartuLangganan(
    langganan: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = langganan.service?.name ?: stringResource(R.string.nilai_kosong),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = langganan.planName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.format_harga_siklus,
                        teksRupiah(langganan.price),
                        stringResource(langganan.billingCycle.label)
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                langganan.nextBillingDate?.let { tanggal ->
                    Text(
                        text = stringResource(R.string.format_tagihan_berikutnya, tanggalTampilan(tanggal)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusLanggananChip(status = langganan.status)
        }
    }
}

// Kosong karena hasil cari/filter tidak ada, atau user memang belum punya langganan
@Composable
private fun LanggananKosong(kataKunci: String, status: SubscriptionStatus?, onTambah: () -> Unit) {
    if (kataKunci.isNotBlank() || status != null) {
        EmptyView(pesanRes = R.string.empty_hasil_cari, ikon = Icons.Filled.SearchOff)
    } else {
        EmptyView(
            pesanRes = R.string.empty_langganan,
            ikon = Icons.Filled.Subscriptions,
            aksiRes = R.string.btn_tambah,
            onAksi = onTambah
        )
    }
}
