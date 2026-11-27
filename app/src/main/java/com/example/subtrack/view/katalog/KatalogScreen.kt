package com.example.subtrack.view.katalog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.Category
import com.example.subtrack.model.dto.Service
import com.example.subtrack.view.components.BarisFilterChip
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.EmptyView
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.IsiPenuhBisaRefresh
import com.example.subtrack.view.components.KolomCari
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.StatusLayananChip
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.katalog.KatalogUiState
import com.example.subtrack.viewmodel.katalog.KatalogViewModel

// Tab Katalog (admin): cari, filter kategori, daftar layanan, tambah/ubah/hapus.
// pesanHasil = pesan dari form setelah simpan/hapus berhasil (dikirim lewat navigasi).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogScreen(
    onTambah: () -> Unit,
    onUbah: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KatalogViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Hasil dari form -> tampilkan pesan dan muat ulang daftar
    LaunchedEffect(pesanHasil) {
        if (pesanHasil != null) {
            viewModel.terimaHasilForm(pesanHasil)
            onPesanHasilDiterima()
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
        topBar = { SubTrackTopAppBar(titleRes = R.string.menu_katalog) },
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
                hintRes = R.string.hint_cari_layanan
            )
            BarisFilterChip(
                pilihan = Category.entries,
                terpilih = viewModel.kategori,
                onPilih = viewModel::onKategoriChange
            )
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = viewModel.uiState) {
                    is KatalogUiState.Loading -> LoadingView()
                    is KatalogUiState.Error -> IsiPenuhBisaRefresh {
                        ErrorView(pesan = state.pesan.teks(), onCobaLagi = viewModel::cobaLagi)
                    }
                    is KatalogUiState.Success -> {
                        if (state.daftar.isEmpty()) {
                            IsiPenuhBisaRefresh { KatalogKosong(viewModel.kataKunci, viewModel.kategori, onTambah) }
                        } else {
                            DaftarLayanan(
                                daftar = state.daftar,
                                onUbah = onUbah,
                                onHapus = viewModel::konfirmasiHapus
                            )
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

    viewModel.layananDihapus?.let { layanan ->
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_layanan, layanan.name),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::batalHapus
        )
    }
}

@Composable
private fun DaftarLayanan(
    daftar: List<Service>,
    onUbah: (Int) -> Unit,
    onHapus: (Service) -> Unit,
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
        items(daftar, key = { it.id }) { layanan ->
            KartuLayanan(
                layanan = layanan,
                onClick = { onUbah(layanan.id) },
                onHapus = { onHapus(layanan) }
            )
        }
    }
}

// Kartu satu layanan: nama, kategori, deskripsi singkat, status aktif, tombol hapus
@Composable
private fun KartuLayanan(
    layanan: Service,
    onClick: () -> Unit,
    onHapus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_medium),
                top = dimensionResource(R.dimen.padding_medium),
                bottom = dimensionResource(R.dimen.padding_medium)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = layanan.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(layanan.category.label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!layanan.description.isNullOrBlank()) {
                    Text(
                        text = layanan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            StatusLayananChip(aktif = layanan.isActive)
            IconButton(onClick = onHapus) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_hapus),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Kosong karena hasil cari/filter tidak ada, atau katalog memang belum berisi
@Composable
private fun KatalogKosong(kataKunci: String, kategori: Category?, onTambah: () -> Unit) {
    if (kataKunci.isNotBlank() || kategori != null) {
        EmptyView(pesanRes = R.string.empty_hasil_cari, ikon = Icons.Filled.SearchOff)
    } else {
        EmptyView(
            pesanRes = R.string.empty_katalog,
            ikon = Icons.Filled.Storefront,
            aksiRes = R.string.btn_tambah,
            onAksi = onTambah
        )
    }
}
