package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.subtrack.R

// Dialog konfirmasi. Default untuk hapus data; bisa dipakai juga untuk keluar akun
// dengan mengganti judulRes, konfirmasiRes, dan ikon.
// pesan berupa teks karena sering memuat nama data, contoh
// stringResource(R.string.confirm_hapus_langganan, langganan.planName)
@Composable
fun ConfirmDialog(
    pesan: String,
    onKonfirmasi: () -> Unit,
    onBatal: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes judulRes: Int = R.string.confirm_hapus_title,
    @StringRes konfirmasiRes: Int = R.string.btn_hapus,
    ikon: ImageVector = Icons.Filled.DeleteOutline,
    // Tombol konfirmasi berwarna error untuk aksi yang tidak bisa dibatalkan
    berbahaya: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onBatal,
        icon = { Icon(imageVector = ikon, contentDescription = null) },
        title = { Text(stringResource(judulRes)) },
        text = { Text(pesan) },
        confirmButton = {
            TextButton(onClick = onKonfirmasi) {
                Text(
                    text = stringResource(konfirmasiRes),
                    color = if (berbahaya) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onBatal) {
                Text(stringResource(R.string.btn_batal))
            }
        },
        modifier = modifier
    )
}
