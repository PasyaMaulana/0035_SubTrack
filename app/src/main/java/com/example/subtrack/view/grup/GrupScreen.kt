package com.example.subtrack.view.grup

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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.subtrack.model.dto.Group
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.EmptyView
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.IsiPenuhBisaRefresh
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.grup.GrupUiState
import com.example.subtrack.viewmodel.grup.GrupViewModel

// Tab Patungan (user): daftar grup patungan dengan progres iuran.
// pesanHasil = pesan dari detail/form setelah hapus/simpan berhasil (dikirim lewat navigasi).
// perluMuatUlang = grup diubah dari layar detail, daftar dimuat ulang tanpa pesan.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrupScreen(
    onTambah: () -> Unit,
    onDetail: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    perluMuatUlang: Boolean,
    onMuatUlangDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GrupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
        topBar = { SubTrackTopAppBar(titleRes = R.string.menu_grup) },
        floatingActionButton = {
            FloatingActionButton(onClick = onTambah) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_tambah))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = viewModel.uiState) {
                is GrupUiState.Loading -> LoadingView()
                is GrupUiState.Error -> IsiPenuhBisaRefresh {
                    ErrorView(pesan = state.pesan.teks(), onCobaLagi = viewModel::cobaLagi)
                }
                is GrupUiState.Success -> {
                    if (state.daftar.isEmpty()) {
                        IsiPenuhBisaRefresh {
                            EmptyView(
                                pesanRes = R.string.empty_grup,
                                ikon = Icons.Filled.Groups,
                                aksiRes = R.string.btn_tambah,
                                onAksi = onTambah
                            )
                        }
                    } else {
                        DaftarGrup(daftar = state.daftar, onDetail = onDetail)
                    }
                }
            }
        }
    }
}

@Composable
private fun DaftarGrup(
    daftar: List<Group>,
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
        items(daftar, key = { it.id }) { grup ->
            KartuGrup(grup = grup, onClick = { onDetail(grup.id) })
        }
    }
}

// Kartu satu grup: nama, layanan, total biaya per siklus, jumlah anggota, progres iuran
@Composable
private fun KartuGrup(
    grup: Group,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grup.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = grup.service?.name ?: stringResource(R.string.nilai_kosong),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.format_jumlah_anggota, grup.summary.memberCount, grup.maxMembers),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(
                    R.string.format_harga_siklus,
                    teksRupiah(grup.totalCost),
                    stringResource(grup.billingCycle.label)
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            ProgresIuran(grup = grup)
        }
    }
}
