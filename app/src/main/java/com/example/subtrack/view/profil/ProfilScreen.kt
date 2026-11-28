package com.example.subtrack.view.profil

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.BuildConfig
import com.example.subtrack.R
import com.example.subtrack.model.dto.User
import com.example.subtrack.util.tanggalWaktuTampilan
import com.example.subtrack.view.components.BarisInfo
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.ErrorView
import com.example.subtrack.view.components.IsiPenuhBisaRefresh
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.components.RoleChip
import com.example.subtrack.view.components.SubTrackTopAppBar
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.profil.ProfilUiState
import com.example.subtrack.viewmodel.profil.ProfilViewModel

// Tab Profil (USER dan ADMIN): avatar inisial, data akun, tentang aplikasi, dan tombol Keluar.
// Setelah logout sesi dihapus, NavGraph yang memindahkan ke layar login.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Dialog konfirmasi keluar; rememberSaveable supaya tetap terbuka saat layar diputar
    var tampilKonfirmasi by rememberSaveable { mutableStateOf(false) }

    val teksPesan = viewModel.pesan?.teks()
    LaunchedEffect(teksPesan) {
        if (teksPesan != null) {
            snackbarHostState.showSnackbar(teksPesan)
            viewModel.hapusPesan()
        }
    }

    Scaffold(
        topBar = { SubTrackTopAppBar(titleRes = R.string.menu_profil) },
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
                is ProfilUiState.Loading -> LoadingView()
                // Saat profil gagal dimuat, tombol Keluar tetap tersedia supaya user tidak terjebak
                is ProfilUiState.Error -> IsiPenuhBisaRefresh {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ErrorView(
                            pesan = state.pesan.teks(),
                            onCobaLagi = viewModel::cobaLagi,
                            modifier = Modifier.weight(1f)
                        )
                        TombolKeluar(
                            sedangKeluar = viewModel.sedangKeluar,
                            onClick = { tampilKonfirmasi = true },
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                        )
                    }
                }
                is ProfilUiState.Success -> IsiProfil(
                    user = state.user,
                    sedangKeluar = viewModel.sedangKeluar,
                    onKeluar = { tampilKonfirmasi = true }
                )
            }
        }
    }

    if (tampilKonfirmasi) {
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_keluar_message),
            onKonfirmasi = {
                tampilKonfirmasi = false
                viewModel.logout()
            },
            onBatal = { tampilKonfirmasi = false },
            judulRes = R.string.confirm_keluar_title,
            konfirmasiRes = R.string.btn_keluar,
            ikon = Icons.AutoMirrored.Filled.Logout
        )
    }
}

@Composable
private fun IsiProfil(
    user: User,
    sedangKeluar: Boolean,
    onKeluar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // verticalScroll juga membuat pull to refresh bisa ditarik
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        KepalaProfil(user = user)

        BagianInfo(judulRes = R.string.label_informasi_akun) {
            BarisInfo(labelRes = R.string.label_email, nilai = user.email)
            BarisInfo(labelRes = R.string.label_role, nilai = stringResource(user.role.label))
            BarisInfo(
                labelRes = R.string.label_bergabung_sejak,
                nilai = tanggalWaktuTampilan(user.createdAt).ifBlank { stringResource(R.string.nilai_kosong) },
                garisBawah = false
            )
        }

        BagianInfo(judulRes = R.string.label_tentang_aplikasi) {
            BarisInfo(labelRes = R.string.label_nama_aplikasi, nilai = stringResource(R.string.app_name))
            BarisInfo(labelRes = R.string.label_versi, nilai = BuildConfig.VERSION_NAME, garisBawah = false)
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
            )
        }

        TombolKeluar(sedangKeluar = sedangKeluar, onClick = onKeluar)
    }
}

// Avatar inisial, nama, email, dan chip role
@Composable
private fun KepalaProfil(user: User, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.padding_small))
    ) {
        AvatarInisial(nama = user.name)
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        RoleChip(role = user.role)
    }
}

// Lingkaran berisi huruf pertama nama. Hanya hiasan, nama sudah tertulis di bawahnya.
@Composable
private fun AvatarInisial(nama: String, modifier: Modifier = Modifier) {
    val inisial = nama.trim().firstOrNull()?.uppercase() ?: stringResource(R.string.nilai_kosong)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(dimensionResource(R.dimen.avatar_size))
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .clearAndSetSemantics { }
    ) {
        Text(
            text = inisial,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// Kartu berjudul berisi baris-baris info
@Composable
private fun BagianInfo(
    @StringRes judulRes: Int,
    modifier: Modifier = Modifier,
    isi: @Composable () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Text(
                text = stringResource(judulRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small))
            )
            isi()
        }
    }
}

// Tombol Keluar berwarna error; saat logout berjalan diganti indikator loading
@Composable
private fun TombolKeluar(sedangKeluar: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val teksKeluar = stringResource(R.string.msg_keluar)
    Button(
        onClick = onClick,
        enabled = !sedangKeluar,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = dimensionResource(R.dimen.form_max_width))
            .height(dimensionResource(R.dimen.button_height))
    ) {
        if (sedangKeluar) {
            CircularProgressIndicator(
                strokeWidth = dimensionResource(R.dimen.progress_stroke),
                modifier = Modifier
                    .size(dimensionResource(R.dimen.icon_medium))
                    .semantics { contentDescription = teksKeluar }
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_small))
            )
            Text(
                text = stringResource(R.string.btn_keluar),
                modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))
            )
        }
    }
}
