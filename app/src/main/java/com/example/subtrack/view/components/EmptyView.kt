package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.subtrack.R

// Tampilan saat data kosong: ikon, pesan, dan tombol aksi opsional (contoh "Tambah").
// Tombol muncul hanya jika aksiRes dan onAksi sama-sama diisi.
@Composable
fun EmptyView(
    @StringRes pesanRes: Int,
    modifier: Modifier = Modifier,
    ikon: ImageVector = Icons.Filled.Inbox,
    @StringRes aksiRes: Int? = null,
    onAksi: (() -> Unit)? = null
) {
    EmptyView(pesan = stringResource(pesanRes), modifier = modifier, ikon = ikon, aksiRes = aksiRes, onAksi = onAksi)
}

// Versi dengan teks jadi, untuk pesan berformat (contoh "Tidak ada tagihan dalam 14 hari ke depan")
@Composable
fun EmptyView(
    pesan: String,
    modifier: Modifier = Modifier,
    ikon: ImageVector = Icons.Filled.Inbox,
    @StringRes aksiRes: Int? = null,
    onAksi: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_large))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
            modifier = Modifier.widthIn(max = dimensionResource(R.dimen.status_view_max_width))
        ) {
            // Ikon hanya hiasan, teks di bawahnya sudah menjelaskan
            Icon(
                imageVector = ikon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_extra_large))
            )
            Text(
                text = pesan,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (aksiRes != null && onAksi != null) {
                FilledTonalButton(onClick = onAksi) {
                    Text(stringResource(aksiRes))
                }
            }
        }
    }
}
