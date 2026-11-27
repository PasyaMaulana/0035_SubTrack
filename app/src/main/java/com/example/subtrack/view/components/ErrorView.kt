package com.example.subtrack.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.subtrack.R

// Tampilan error penuh layar: ikon, pesan, dan tombol coba lagi (jika onCobaLagi diisi).
// pesan sudah berupa teks: pesan server apa adanya atau stringResource dari @StringRes ViewModel.
@Composable
fun ErrorView(
    pesan: String,
    modifier: Modifier = Modifier,
    onCobaLagi: (() -> Unit)? = null
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
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = stringResource(R.string.cd_ikon_error),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_extra_large))
            )
            Text(
                text = pesan,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (onCobaLagi != null) {
                Button(onClick = onCobaLagi) {
                    Text(stringResource(R.string.btn_coba_lagi))
                }
            }
        }
    }
}
