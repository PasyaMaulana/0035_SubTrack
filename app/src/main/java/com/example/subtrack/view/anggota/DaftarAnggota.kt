package com.example.subtrack.view.anggota

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.subtrack.R
import com.example.subtrack.model.dto.DuesStatus
import com.example.subtrack.model.dto.Member
import com.example.subtrack.util.tanggalTampilan
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.StatusIuranChip
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.anggota.DaftarAnggotaUiState
import com.example.subtrack.viewmodel.anggota.DaftarAnggotaViewModel

// Bagian anggota di layar detail grup: tombol tambah, keterangan grup penuh, dan daftar anggota.
// Daftar memakai Column biasa (bukan LazyColumn) karena berada di dalam layar detail yang sudah bisa di-scroll.
// jumlahAnggota dan maksAnggota dipakai untuk menonaktifkan tombol tambah saat grup penuh.
// onUbah menerima id anggota.
@Composable
fun BagianAnggota(
    viewModel: DaftarAnggotaViewModel,
    jumlahAnggota: Int,
    maksAnggota: Int,
    onTambah: () -> Unit,
    onUbah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val penuh = jumlahAnggota >= maksAnggota
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.title_anggota),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.format_jumlah_anggota, jumlahAnggota, maksAnggota),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onTambah, enabled = !penuh) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_small))
                )
                Text(
                    text = stringResource(R.string.btn_tambah),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))
                )
            }
        }

        // Keterangan kenapa tombol tambah nonaktif
        if (penuh) KeteranganGrupPenuh(jumlahAnggota = jumlahAnggota, maksAnggota = maksAnggota)

        when (val state = viewModel.uiState) {
            is DaftarAnggotaUiState.Loading -> KotakStatus {
                CircularProgressIndicator()
            }
            is DaftarAnggotaUiState.Error -> KotakStatus {
                Text(
                    text = state.pesan.teks(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(onClick = viewModel::muat) {
                    Text(stringResource(R.string.btn_coba_lagi))
                }
            }
            is DaftarAnggotaUiState.Success -> IsiDaftarAnggota(
                anggota = state.anggota,
                idSedangDiproses = viewModel.idSedangDiproses,
                onUbahStatus = viewModel::ubahStatus,
                onUbah = { onUbah(it.id) },
                onHapus = viewModel::mintaHapus
            )
        }
    }

    viewModel.akanDihapus?.let { anggota ->
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_anggota, anggota.memberName),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::batalHapus
        )
    }
}

@Composable
private fun KeteranganGrupPenuh(jumlahAnggota: Int, maksAnggota: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_small))
        )
        Text(
            text = stringResource(R.string.msg_grup_penuh_tambah, jumlahAnggota, maksAnggota),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IsiDaftarAnggota(
    anggota: List<Member>,
    idSedangDiproses: Int?,
    onUbahStatus: (Member) -> Unit,
    onUbah: (Member) -> Unit,
    onHapus: (Member) -> Unit
) {
    if (anggota.isEmpty()) {
        KotakStatus {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_large))
            )
            Text(
                text = stringResource(R.string.empty_anggota),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        anggota.forEachIndexed { indeks, item ->
            ItemAnggota(
                anggota = item,
                sedangDiproses = item.id == idSedangDiproses,
                // Selama satu anggota diproses, aksi anggota lain ikut dinonaktifkan
                aksiAktif = idSedangDiproses == null,
                onUbahStatus = { onUbahStatus(item) },
                onUbah = { onUbah(item) },
                onHapus = { onHapus(item) }
            )
            if (indeks < anggota.lastIndex) HorizontalDivider()
        }
    }
}

// Satu anggota: nama, kontak, catatan, iuran, status, tombol tandai/batal lunas, dan menu ubah/hapus
@Composable
private fun ItemAnggota(
    anggota: Member,
    sedangDiproses: Boolean,
    aksiAktif: Boolean,
    onUbahStatus: () -> Unit,
    onUbah: () -> Unit,
    onHapus: () -> Unit
) {
    val lunas = anggota.duesStatus == DuesStatus.LUNAS
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.padding_medium),
                top = dimensionResource(R.dimen.padding_small),
                bottom = dimensionResource(R.dimen.padding_small)
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = anggota.memberName, style = MaterialTheme.typography.titleSmall)
                anggota.contact?.takeIf { it.isNotBlank() }?.let { kontak ->
                    Text(
                        text = kontak,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            MenuAnggota(enabled = aksiAktif, onUbah = onUbah, onHapus = onHapus)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = teksRupiah(anggota.shareAmount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            StatusIuranChip(status = anggota.duesStatus)
        }

        if (lunas && anggota.paidAt != null) {
            Text(
                text = stringResource(R.string.format_dibayar_tanggal, tanggalTampilan(anggota.paidAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        anggota.note?.takeIf { it.isNotBlank() }?.let { catatan ->
            Text(
                text = catatan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium))
            )
        }

        Box(modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))) {
            if (sedangDiproses) {
                CircularProgressIndicator(
                    strokeWidth = dimensionResource(R.dimen.progress_stroke),
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .size(dimensionResource(R.dimen.icon_medium))
                )
            } else if (lunas) {
                TombolStatus(Icons.AutoMirrored.Filled.Undo, R.string.btn_batal_lunas, aksiAktif, onUbahStatus)
            } else {
                TombolStatus(Icons.Filled.CheckCircle, R.string.btn_tandai_lunas, aksiAktif, onUbahStatus)
            }
        }
    }
}

// Tombol teks kecil untuk tandai lunas / batal lunas
@Composable
private fun TombolStatus(ikon: ImageVector, @StringRes teksRes: Int, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = ikon,
            contentDescription = null,
            modifier = Modifier
                .padding(end = dimensionResource(R.dimen.padding_small))
                .size(dimensionResource(R.dimen.icon_small))
        )
        Text(stringResource(teksRes))
    }
}

// Menu titik tiga: ubah atau hapus anggota
@Composable
private fun MenuAnggota(enabled: Boolean, onUbah: () -> Unit, onHapus: () -> Unit) {
    var terbuka by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { terbuka = true }, enabled = enabled) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_menu_lainnya))
        }
        DropdownMenu(expanded = terbuka, onDismissRequest = { terbuka = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.btn_edit)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    terbuka = false
                    onUbah()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.btn_hapus), color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    terbuka = false
                    onHapus()
                }
            )
        }
    }
}

// Kotak berbingkai untuk loading, error, dan kosong
@Composable
private fun KotakStatus(isi: @Composable () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            isi()
        }
    }
}
