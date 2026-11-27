package com.example.subtrack.view.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.subtrack.R

// Kerangka bersama layar login dan register: logo, judul, form, Snackbar error
@Composable
fun KerangkaAuth(
    @StringRes judulRes: Int,
    @StringRes subjudulRes: Int,
    pesanError: String?,
    onPesanErrorTampil: () -> Unit,
    modifier: Modifier = Modifier,
    isi: @Composable ColumnScope.() -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan pesan error umum sekali, lalu hapus dari state ViewModel
    LaunchedEffect(pesanError) {
        if (pesanError != null) {
            snackbarHostState.showSnackbar(pesanError)
            onPesanErrorTampil()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                modifier = Modifier
                    .widthIn(max = dimensionResource(R.dimen.form_max_width))
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Filled.Subscriptions,
                    contentDescription = stringResource(R.string.cd_logo),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.logo_size))
                )
                Text(
                    text = stringResource(judulRes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(subjudulRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_large))
                )
                isi()
            }
        }
    }
}

// Tombol utama form; saat loading tombol nonaktif dan menampilkan indikator putar
@Composable
fun TombolUtama(
    @StringRes teksRes: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.spacing_small))
            .height(dimensionResource(R.dimen.button_height))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = dimensionResource(R.dimen.progress_stroke),
                modifier = Modifier.size(dimensionResource(R.dimen.icon_medium))
            )
        } else {
            Text(stringResource(teksRes))
        }
    }
}

// Tautan teks untuk berpindah antara login dan register
@Composable
fun TautanAuth(
    @StringRes teksRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(stringResource(teksRes))
    }
}
