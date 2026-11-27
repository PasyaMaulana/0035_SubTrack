package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.example.subtrack.R
import com.example.subtrack.model.dto.PunyaLabel

// Kotak pencarian di atas daftar, dengan tombol hapus teks
@Composable
fun KolomCari(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes hintRes: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(hintRes)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_cari)) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_hapus_pencarian))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
    )
}

// Baris chip filter: "Semua" + setiap pilihan (enum berlabel).
// Chip terpilih ditekan lagi = kembali ke Semua (null).
@Composable
fun <T : PunyaLabel> BarisFilterChip(
    pilihan: List<T>,
    terpilih: T?,
    onPilih: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.padding_medium),
            vertical = dimensionResource(R.dimen.padding_small)
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = terpilih == null,
                onClick = { onPilih(null) },
                label = { Text(stringResource(R.string.btn_semua)) }
            )
        }
        items(pilihan) { item ->
            FilterChip(
                selected = terpilih == item,
                onClick = { onPilih(if (terpilih == item) null else item) },
                label = { Text(stringResource(item.label)) }
            )
        }
    }
}

// Pull to refresh butuh konten yang bisa di-scroll, jadi ErrorView/EmptyView
// dibungkus LazyColumn berisi satu item setinggi layar
@Composable
fun IsiPenuhBisaRefresh(modifier: Modifier = Modifier, isi: @Composable () -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { Column(modifier = Modifier.fillParentMaxSize()) { isi() } }
    }
}
