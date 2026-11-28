package com.example.subtrack.view.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.model.dto.AdminTotals
import com.example.subtrack.model.dto.CategorySpending
import com.example.subtrack.model.dto.DashboardData
import com.example.subtrack.model.dto.GroupStats
import com.example.subtrack.model.dto.PopularService
import com.example.subtrack.model.dto.Role
import com.example.subtrack.model.dto.SpendingStats
import com.example.subtrack.model.dto.SubscriptionStats
import com.example.subtrack.model.dto.UpcomingBill
import com.example.subtrack.util.formatRibuan
import com.example.subtrack.util.parseTanggalApi
import com.example.subtrack.util.tanggalTampilan
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.BarisInfo
import com.example.subtrack.view.components.EmptyView
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.IsiPenuhBisaRefresh
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.view.components.warnaRes
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.dashboard.DashboardUiState
import com.example.subtrack.viewmodel.dashboard.DashboardViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Tab Dashboard. Isi berbeda per role:
// USER  -> sapaan, ringkasan pengeluaran, pengeluaran per kategori, tagihan terdekat, ringkasan patungan
// ADMIN -> sapaan, total data aplikasi, layanan terpopuler
// nama = nama user dari sesi login. pesanHasil = pesan dari layar detail yang dibuka lewat dashboard.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: Role,
    nama: String,
    onDetailLangganan: (Int) -> Unit,
    onKePatungan: () -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Muat setiap kali tab tampil, supaya perubahan dari tab/layar lain langsung terlihat
    LaunchedEffect(Unit) {
        viewModel.tampil()
    }

    LaunchedEffect(pesanHasil) {
        if (pesanHasil != null) {
            viewModel.terimaHasil(pesanHasil)
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
        topBar = { SubTrackTopAppBar(titleRes = R.string.menu_dashboard) },
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
                is DashboardUiState.Loading -> LoadingView()
                is DashboardUiState.Error -> IsiPenuhBisaRefresh {
                    ErrorView(pesan = state.pesan.teks(), onCobaLagi = viewModel::cobaLagi)
                }
                is DashboardUiState.Success -> IsiDashboard(
                    // Role dari respons server diutamakan; role sesi sebagai cadangan
                    role = state.data.role ?: role,
                    nama = nama,
                    data = state.data,
                    onDetailLangganan = onDetailLangganan,
                    onKePatungan = onKePatungan
                )
            }
        }
    }
}

@Composable
private fun IsiDashboard(
    role: Role,
    nama: String,
    data: DashboardData,
    onDetailLangganan: (Int) -> Unit,
    onKePatungan: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Sapaan(
                nama = nama,
                subjudulRes = if (role == Role.ADMIN) R.string.dashboard_subtitle_admin
                else R.string.dashboard_subtitle_user
            )
        }
        when (role) {
            Role.USER -> isiDashboardUser(data, onDetailLangganan, onKePatungan)
            Role.ADMIN -> isiDashboardAdmin(data)
        }
    }
}

// --- Dashboard USER ---

private fun LazyListScope.isiDashboardUser(
    data: DashboardData,
    onDetailLangganan: (Int) -> Unit,
    onKePatungan: () -> Unit
) {
    // Bagian yang tidak dikirim server dianggap bernilai nol
    val langganan = data.subscriptions ?: SubscriptionStats()
    val pengeluaran = data.spending ?: SpendingStats()
    val grup = data.groups ?: GroupStats()

    item { RingkasanUser(langganan = langganan, pengeluaran = pengeluaran) }

    item { JudulBagian(teks = stringResource(R.string.label_per_kategori)) }
    item { KartuPerKategori(daftar = pengeluaran.byCategory) }

    item {
        JudulBagian(teks = stringResource(R.string.format_hari_tagihan, DashboardViewModel.HARI_TAGIHAN))
    }
    item { KartuTagihan(daftar = data.upcomingBills, onDetail = onDetailLangganan) }

    item { JudulBagian(teks = stringResource(R.string.label_grup_patungan)) }
    item { KartuPatungan(grup = grup, onClick = onKePatungan) }
}

// Kartu besar estimasi per bulan, lalu dua kartu kecil: dibayar bulan ini dan langganan aktif
@Composable
private fun RingkasanUser(
    langganan: SubscriptionStats,
    pengeluaran: SpendingStats,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                Text(
                    text = stringResource(R.string.label_estimasi_bulanan),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.format_per_bulan, teksRupiah(pengeluaran.monthlyEstimate)),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        BarisDuaKartu(
            kiri = {
                KartuStatistik(
                    labelRes = R.string.label_dibayar_bulan_ini,
                    nilai = teksRupiah(pengeluaran.paidThisMonth),
                    modifier = it
                )
            },
            kanan = {
                KartuStatistik(
                    labelRes = R.string.label_langganan_aktif,
                    nilai = formatRibuan(langganan.active.toLong()),
                    keterangan = stringResource(R.string.format_dari_total, formatRibuan(langganan.total.toLong())),
                    modifier = it
                )
            }
        )
    }
}

// Bar horizontal pengeluaran per kategori. Bar terpanjang = kategori dengan pengeluaran terbesar.
@Composable
private fun KartuPerKategori(daftar: List<CategorySpending>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        if (daftar.isEmpty()) {
            EmptyView(pesanRes = R.string.empty_per_kategori, ikon = Icons.Filled.BarChart)
            return@Card
        }
        val urut = daftar.sortedByDescending { it.amount }
        val maks = urut.first().amount
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            urut.forEach { BarKategori(item = it, maks = maks) }
        }
    }
}

@Composable
private fun BarKategori(item: CategorySpending, maks: Long, modifier: Modifier = Modifier) {
    val bentuk = RoundedCornerShape(dimensionResource(R.dimen.radius_small))
    // Nilai 0..1 panjang bar; maks 0 dianggap tidak ada pengeluaran
    val rasio = if (maks <= 0) 0f else (item.amount.toFloat() / maks).coerceIn(0f, 1f)
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row {
            Text(
                text = stringResource(item.category.label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = teksRupiah(item.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Jalur bar (latar) lalu isi bar selebar rasio, warna sesuai kategori
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.chart_bar_height))
                .clip(bentuk)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(rasio)
                    .fillMaxHeight()
                    .clip(bentuk)
                    .background(colorResource(item.category.warnaRes()))
            )
        }
    }
}

// Daftar tagihan dalam rentang hari ke depan; tap baris membuka detail langganan
@Composable
private fun KartuTagihan(
    daftar: List<UpcomingBill>,
    onDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        if (daftar.isEmpty()) {
            EmptyView(
                pesan = stringResource(R.string.empty_tagihan_hari, DashboardViewModel.HARI_TAGIHAN),
                ikon = Icons.Filled.EventAvailable
            )
            return@Card
        }
        daftar.forEachIndexed { indeks, tagihan ->
            if (indeks > 0) HorizontalDivider()
            // id tagihan = id langganan
            BarisTagihan(tagihan = tagihan, onClick = { onDetail(tagihan.id) })
        }
    }
}

@Composable
private fun BarisTagihan(
    tagihan: UpcomingBill,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sisaHari = teksSisaHari(tagihan.nextBillingDate)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.cd_lihat_detail), onClick = onClick)
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tagihan.serviceName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.format_paket_harga, tagihan.planName, teksRupiah(tagihan.price)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = tanggalTampilan(tagihan.nextBillingDate).ifBlank { stringResource(R.string.nilai_kosong) },
                style = MaterialTheme.typography.labelLarge
            )
            if (sisaHari != null) {
                Text(
                    text = sisaHari,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        // Ikon penunjuk saja, aksi sudah dijelaskan onClickLabel
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// "Hari ini", "Besok", atau "N hari lagi"; null jika tanggal kosong atau sudah lewat
@Composable
private fun teksSisaHari(tanggalApi: String?): String? {
    val tanggal = parseTanggalApi(tanggalApi) ?: return null
    val hari = ChronoUnit.DAYS.between(LocalDate.now(), tanggal)
    return when {
        hari < 0 -> null
        hari == 0L -> stringResource(R.string.label_hari_ini)
        hari == 1L -> stringResource(R.string.label_besok)
        else -> stringResource(R.string.format_hari_lagi, hari.toInt())
    }
}

// Ringkasan patungan; tap kartu pindah ke tab Patungan
@Composable
private fun KartuPatungan(grup: GroupStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            BarisInfo(labelRes = R.string.label_total_grup, nilai = formatRibuan(grup.total.toLong()))
            BarisInfo(
                labelRes = R.string.label_anggota_belum_bayar,
                nilai = formatRibuan(grup.unpaidMembers.toLong())
            )
            BarisInfo(
                labelRes = R.string.label_iuran_tertunggak,
                nilai = teksRupiah(grup.outstandingDues),
                garisBawah = false
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    text = stringResource(R.string.label_lihat_patungan),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// --- Dashboard ADMIN ---

private fun LazyListScope.isiDashboardAdmin(data: DashboardData) {
    val total = data.totals ?: AdminTotals()

    item { RingkasanAdmin(total = total) }

    item { JudulBagian(teks = stringResource(R.string.label_layanan_populer)) }
    item { KartuLayananPopuler(daftar = data.popularServices) }
}

// Empat kartu: total user, layanan aktif dari total, total langganan, total grup
@Composable
private fun RingkasanAdmin(total: AdminTotals, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier.fillMaxWidth()
    ) {
        BarisDuaKartu(
            kiri = {
                KartuStatistik(
                    labelRes = R.string.label_total_pengguna,
                    nilai = formatRibuan(total.users.toLong()),
                    modifier = it
                )
            },
            kanan = {
                KartuStatistik(
                    labelRes = R.string.label_layanan_aktif,
                    nilai = formatRibuan(total.activeServices.toLong()),
                    keterangan = stringResource(R.string.format_dari_total, formatRibuan(total.services.toLong())),
                    modifier = it
                )
            }
        )
        BarisDuaKartu(
            kiri = {
                KartuStatistik(
                    labelRes = R.string.label_total_langganan,
                    nilai = formatRibuan(total.subscriptions.toLong()),
                    modifier = it
                )
            },
            kanan = {
                KartuStatistik(
                    labelRes = R.string.label_total_grup,
                    nilai = formatRibuan(total.groups.toLong()),
                    modifier = it
                )
            }
        )
    }
}

// Daftar layanan dengan pelanggan terbanyak, diberi nomor urut
@Composable
private fun KartuLayananPopuler(daftar: List<PopularService>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        if (daftar.isEmpty()) {
            EmptyView(pesanRes = R.string.empty_layanan_populer, ikon = Icons.Filled.Star)
            return@Card
        }
        daftar.forEachIndexed { indeks, layanan ->
            if (indeks > 0) HorizontalDivider()
            BarisLayananPopuler(peringkat = indeks + 1, layanan = layanan)
        }
    }
}

@Composable
private fun BarisLayananPopuler(peringkat: Int, layanan: PopularService, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(
            text = stringResource(R.string.format_peringkat, peringkat),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layanan.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(layanan.category.label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.format_jumlah_pelanggan, layanan.subscriptionCount),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// --- Komponen bersama dashboard ---

@Composable
private fun Sapaan(nama: String, @StringRes subjudulRes: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.format_halo, nama),
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(subjudulRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JudulBagian(teks: String, modifier: Modifier = Modifier) {
    Text(
        text = teks,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = dimensionResource(R.dimen.padding_small))
    )
}

// Dua kartu berdampingan dengan lebar dan tinggi sama
@Composable
private fun BarisDuaKartu(
    kiri: @Composable (Modifier) -> Unit,
    kanan: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        kiri(Modifier.weight(1f).fillMaxHeight())
        kanan(Modifier.weight(1f).fillMaxHeight())
    }
}

// Kartu satu angka statistik: label, nilai besar, dan keterangan opsional
@Composable
private fun KartuStatistik(
    @StringRes labelRes: Int,
    nilai: String,
    modifier: Modifier = Modifier,
    keterangan: String? = null
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = nilai,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (keterangan != null) {
                Text(
                    text = keterangan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
