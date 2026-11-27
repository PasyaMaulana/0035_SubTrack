package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.subtrack.R

// Top app bar seragam untuk semua layar.
// onKembali null -> tanpa tombol kembali (layar tab utama). aksi -> tombol di kanan (opsional).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTrackTopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    onKembali: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    aksi: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(titleRes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onKembali != null) {
                IconButton(onClick = onKembali) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_kembali)
                    )
                }
            }
        },
        actions = aksi,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

// Satu tombol ikon untuk slot aksi top app bar, contoh edit atau hapus
@Composable
fun AksiTopAppBar(
    ikon: ImageVector,
    @StringRes contentDescriptionRes: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(imageVector = ikon, contentDescription = stringResource(contentDescriptionRes))
    }
}
