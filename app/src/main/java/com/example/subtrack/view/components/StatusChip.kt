package com.example.subtrack.view.components

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.subtrack.R
import com.example.subtrack.model.dto.DuesStatus
import com.example.subtrack.model.dto.SubscriptionStatus

// Chip kecil (badge) untuk status. Warna teks dan latar dari colors.xml,
// otomatis berganti versi gelap dari values-night/colors.xml.
@Composable
fun StatusChip(
    @StringRes labelRes: Int,
    @ColorRes warnaTeksRes: Int,
    @ColorRes warnaLatarRes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small)),
        color = colorResource(warnaLatarRes),
        contentColor = colorResource(warnaTeksRes),
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.padding_small),
                vertical = dimensionResource(R.dimen.padding_extra_small)
            )
        )
    }
}

// Chip status langganan: Aktif (hijau), Dijeda (kuning), Dibatalkan (abu-abu)
@Composable
fun StatusLanggananChip(status: SubscriptionStatus, modifier: Modifier = Modifier) {
    val (teks, latar) = when (status) {
        SubscriptionStatus.ACTIVE -> R.color.status_aktif to R.color.status_aktif_container
        SubscriptionStatus.PAUSED -> R.color.status_dijeda to R.color.status_dijeda_container
        SubscriptionStatus.CANCELLED -> R.color.status_dibatalkan to R.color.status_dibatalkan_container
    }
    StatusChip(labelRes = status.label, warnaTeksRes = teks, warnaLatarRes = latar, modifier = modifier)
}

// Chip status iuran anggota: Lunas (hijau), Belum Lunas (merah)
@Composable
fun StatusIuranChip(status: DuesStatus, modifier: Modifier = Modifier) {
    val (teks, latar) = when (status) {
        DuesStatus.LUNAS -> R.color.status_lunas to R.color.status_lunas_container
        DuesStatus.BELUM_LUNAS -> R.color.status_belum_lunas to R.color.status_belum_lunas_container
    }
    StatusChip(labelRes = status.label, warnaTeksRes = teks, warnaLatarRes = latar, modifier = modifier)
}

// Chip status layanan katalog: Aktif (hijau), Nonaktif (abu-abu)
@Composable
fun StatusLayananChip(aktif: Boolean, modifier: Modifier = Modifier) {
    if (aktif) {
        StatusChip(R.string.label_aktif, R.color.status_aktif, R.color.status_aktif_container, modifier)
    } else {
        StatusChip(R.string.label_nonaktif, R.color.status_dibatalkan, R.color.status_dibatalkan_container, modifier)
    }
}
