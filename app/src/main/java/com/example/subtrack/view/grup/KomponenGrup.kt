package com.example.subtrack.view.grup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.example.subtrack.R
import com.example.subtrack.model.dto.Group
import com.example.subtrack.util.teksRupiah

// Progres iuran grup: bar (terkumpul dibanding total biaya), nominal terkumpul, dan jumlah anggota lunas.
// Dipakai di kartu daftar grup dan di detail grup.
@Composable
fun ProgresIuran(grup: Group, modifier: Modifier = Modifier) {
    val ringkasan = grup.summary
    val teksTerkumpul = stringResource(
        R.string.format_terkumpul_dari,
        teksRupiah(ringkasan.collected),
        teksRupiah(grup.totalCost)
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small)),
        modifier = modifier.fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = { rasioTerkumpul(ringkasan.collected, grup.totalCost) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.progress_bar_height))
                .semantics { contentDescription = teksTerkumpul }
        )
        Row {
            Text(
                text = teksTerkumpul,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.format_jumlah_lunas, ringkasan.paidCount, ringkasan.memberCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }
}

// Nilai 0..1 untuk LinearProgressIndicator; total 0 dianggap belum ada progres
private fun rasioTerkumpul(terkumpul: Long, total: Long): Float =
    if (total <= 0) 0f else (terkumpul.toFloat() / total).coerceIn(0f, 1f)
