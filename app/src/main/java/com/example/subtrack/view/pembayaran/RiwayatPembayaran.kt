package com.example.subtrack.view.pembayaran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.subtrack.R
import com.example.subtrack.model.dto.Payment
import com.example.subtrack.model.dto.PaymentList
import com.example.subtrack.util.tanggalTampilan
import com.example.subtrack.util.teksRupiah
import com.example.subtrack.view.components.ConfirmDialog
import com.example.subtrack.view.components.teks
import com.example.subtrack.viewmodel.pembayaran.RiwayatPembayaranUiState
import com.example.subtrack.viewmodel.pembayaran.RiwayatPembayaranViewModel
import java.io.File

// Bagian riwayat pembayaran di layar detail langganan: tombol catat, total, dan daftar.
// Daftar memakai Column biasa (bukan LazyColumn) karena berada di dalam layar detail yang sudah bisa di-scroll.
// onUbah menerima id pembayaran.
@Composable
fun BagianRiwayatPembayaran(
    viewModel: RiwayatPembayaranViewModel,
    onTambah: () -> Unit,
    onUbah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.title_pembayaran),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(onClick = onTambah) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_small))
                )
                Text(
                    text = stringResource(R.string.btn_catat_pembayaran),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))
                )
            }
        }

        when (val state = viewModel.uiState) {
            is RiwayatPembayaranUiState.Loading -> KotakStatus {
                CircularProgressIndicator()
            }
            is RiwayatPembayaranUiState.Error -> KotakStatus {
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
            is RiwayatPembayaranUiState.Success -> IsiRiwayat(
                riwayat = state.riwayat,
                idSedangDihapus = viewModel.idSedangDihapus,
                onLihatFoto = viewModel::lihatFoto,
                onUbah = { onUbah(it.id) },
                onHapus = viewModel::mintaHapus
            )
        }
    }

    if (viewModel.akanDihapus != null) {
        ConfirmDialog(
            pesan = stringResource(R.string.confirm_hapus_pembayaran),
            onKonfirmasi = viewModel::hapus,
            onBatal = viewModel::batalHapus
        )
    }

    viewModel.fotoDilihat?.let { nama ->
        DialogFotoBukti(
            file = remember(nama) { viewModel.fileBukti(nama) },
            onTutup = viewModel::tutupFoto
        )
    }
}

@Composable
private fun IsiRiwayat(
    riwayat: PaymentList,
    idSedangDihapus: Int?,
    onLihatFoto: (Payment) -> Unit,
    onUbah: (Payment) -> Unit,
    onHapus: (Payment) -> Unit
) {
    if (riwayat.payments.isEmpty()) {
        KotakStatus {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_large))
            )
            Text(
                text = stringResource(R.string.empty_pembayaran),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Total dari server (jumlah semua nominal pembayaran langganan ini)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_total_dibayar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.format_jumlah_pembayaran, riwayat.payments.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = teksRupiah(riwayat.total),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        riwayat.payments.forEachIndexed { indeks, pembayaran ->
            ItemPembayaran(
                pembayaran = pembayaran,
                sedangDihapus = pembayaran.id == idSedangDihapus,
                onLihatFoto = { onLihatFoto(pembayaran) },
                onUbah = { onUbah(pembayaran) },
                onHapus = { onHapus(pembayaran) }
            )
            if (indeks < riwayat.payments.lastIndex) HorizontalDivider()
        }
    }
}

// Satu baris pembayaran. Baris yang punya foto bukti bisa diketuk untuk melihat fotonya.
@Composable
private fun ItemPembayaran(
    pembayaran: Payment,
    sedangDihapus: Boolean,
    onLihatFoto: () -> Unit,
    onUbah: () -> Unit,
    onHapus: () -> Unit
) {
    val punyaBukti = pembayaran.namaFileBukti != null
    val tanggal = tanggalTampilan(pembayaran.paidAt)
    val metode = pembayaran.method?.takeIf { it.isNotBlank() }

    ListItem(
        headlineContent = { Text(teksRupiah(pembayaran.amount)) },
        supportingContent = {
            Column {
                Text(
                    if (metode != null) stringResource(R.string.format_tanggal_metode, tanggal, metode) else tanggal
                )
                pembayaran.note?.takeIf { it.isNotBlank() }?.let { catatan ->
                    Text(text = catatan, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        leadingContent = {
            if (punyaBukti) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = stringResource(R.string.cd_ada_bukti),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        },
        trailingContent = {
            if (sedangDihapus) {
                CircularProgressIndicator(
                    strokeWidth = dimensionResource(R.dimen.progress_stroke),
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_medium))
                )
            } else {
                MenuPembayaran(onUbah = onUbah, onHapus = onHapus)
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = if (punyaBukti && !sedangDihapus) {
            Modifier.clickable(onClickLabel = stringResource(R.string.cd_lihat_bukti), onClick = onLihatFoto)
        } else {
            Modifier
        }
    )
}

// Menu titik tiga: ubah atau hapus pembayaran
@Composable
private fun MenuPembayaran(onUbah: () -> Unit, onHapus: () -> Unit) {
    var terbuka by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { terbuka = true }) {
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

// Foto bukti ukuran besar (layar penuh). file null = foto tidak ada di perangkat ini.
@Composable
private fun DialogFotoBukti(file: File?, onTutup: () -> Unit) {
    Dialog(
        onDismissRequest = onTutup,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
                .clickable(onClick = onTutup)
        ) {
            if (file != null) {
                AsyncImage(
                    model = file,
                    contentDescription = stringResource(R.string.cd_bukti_pembayaran),
                    contentScale = ContentScale.Fit,
                    error = rememberVectorPainter(Icons.Filled.BrokenImage),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            } else {
                Text(
                    text = stringResource(R.string.msg_bukti_tidak_ditemukan),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_large))
                )
            }
            IconButton(
                onClick = onTutup,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.padding_small))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_tutup),
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}
