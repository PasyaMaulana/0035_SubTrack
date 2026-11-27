package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.subtrack.R

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
