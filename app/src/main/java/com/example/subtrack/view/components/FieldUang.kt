package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.subtrack.R

// Field input rupiah. value berisi angka murni tanpa titik (contoh "186000"),
// yang tampil diberi pemisah ribuan (186.000) dengan awalan "Rp".
// Karakter selain angka dibuang sebelum dikirim ke onValueChange.
@Composable
fun FieldUang(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    @StringRes supportingRes: Int? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val error = errorServer ?: errorRes?.let { stringResource(it) }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(stringResource(labelRes)) },
        prefix = { Text(stringResource(R.string.prefix_rupiah)) },
        isError = error != null,
        supportingText = when {
            error != null -> { { Text(error) } }
            supportingRes != null -> { { Text(stringResource(supportingRes)) } }
            else -> null
        },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PemisahRibuanTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

// Menyisipkan titik setiap 3 digit dari kanan hanya untuk tampilan (186000 -> 186.000).
// OffsetMapping menjaga posisi kursor tetap benar walau ada titik tambahan.
private object PemisahRibuanTransformation : VisualTransformation {
    private const val PEMISAH = '.'
    private const val KELOMPOK = 3

    override fun filter(text: AnnotatedString): TransformedText {
        val asli = text.text
        val hasil = buildString {
            asli.forEachIndexed { i, c ->
                // Titik disisipkan sebelum digit ke-i jika sisa digit di kanannya kelipatan 3
                if (i > 0 && (asli.length - i) % KELOMPOK == 0) append(PEMISAH)
                append(c)
            }
        }

        val mapping = object : OffsetMapping {
            // Posisi di teks asli -> posisi di teks tampilan
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var digit = 0
                hasil.forEachIndexed { i, c ->
                    if (c != PEMISAH) {
                        digit++
                        if (digit == offset) return i + 1
                    }
                }
                return hasil.length
            }

            // Posisi di teks tampilan -> posisi di teks asli (titik tidak dihitung)
            override fun transformedToOriginal(offset: Int): Int =
                hasil.take(offset.coerceIn(0, hasil.length)).count { it != PEMISAH }
        }
        return TransformedText(AnnotatedString(hasil), mapping)
    }
}
