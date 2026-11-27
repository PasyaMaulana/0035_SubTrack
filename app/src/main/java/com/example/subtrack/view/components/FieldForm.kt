package com.example.subtrack.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.subtrack.R
import com.example.subtrack.model.dto.PunyaLabel
import com.example.subtrack.util.millisKeTanggalApi
import com.example.subtrack.util.tanggalApiKeMillis
import com.example.subtrack.util.tanggalTampilan

// Teks error yang ditampilkan: pesan server didahulukan, lalu pesan lokal dari strings.xml
@Composable
private fun teksError(errorServer: String?, @StringRes errorRes: Int?): String? =
    errorServer ?: errorRes?.let { stringResource(it) }

// Teks di bawah field: error jika ada, jika tidak tampilkan petunjuk (supportingRes)
@Composable
private fun teksBawah(error: String?, @StringRes supportingRes: Int?): (@Composable () -> Unit)? = when {
    error != null -> { { Text(error) } }
    supportingRes != null -> { { Text(stringResource(supportingRes)) } }
    else -> null
}

// Field teks dengan label dan error di bawahnya.
// singleLine = false untuk teks panjang seperti catatan/deskripsi.
@Composable
fun FieldTeks(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    @StringRes supportingRes: Int? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val error = teksError(errorServer, errorRes)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        isError = error != null,
        supportingText = teksBawah(error, supportingRes),
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

// Field password dengan tombol tampilkan/sembunyikan
@Composable
fun FieldPassword(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var terlihat by rememberSaveable { mutableStateOf(false) }
    val error = teksError(errorServer, errorRes)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (terlihat) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { terlihat = !terlihat }) {
                Icon(
                    imageVector = if (terlihat) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (terlihat) R.string.cd_sembunyikan_password else R.string.cd_tampilkan_password
                    )
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

// Field pilihan dropdown (ExposedDropdownMenuBox) untuk daftar apa pun,
// contoh daftar layanan dari API. teksPilihan mengubah item menjadi teks tampilan.
// opsiKosongRes (opsional) menambah pilihan "kosong" di paling atas, contoh "Tanpa langganan".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FieldPilihan(
    pilihan: List<T>,
    terpilih: T?,
    onPilih: (T) -> Unit,
    @StringRes labelRes: Int,
    teksPilihan: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    @StringRes supportingRes: Int? = null,
    enabled: Boolean = true,
    @StringRes opsiKosongRes: Int? = null,
    onPilihKosong: () -> Unit = {}
) {
    var terbuka by remember { mutableStateOf(false) }
    val error = teksError(errorServer, errorRes)
    val teksTerpilih = when {
        terpilih != null -> teksPilihan(terpilih)
        opsiKosongRes != null -> stringResource(opsiKosongRes)
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = terbuka,
        onExpandedChange = { if (enabled) terbuka = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = teksTerpilih,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(labelRes)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = terbuka) },
            isError = error != null,
            supportingText = teksBawah(error, supportingRes),
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = terbuka, onDismissRequest = { terbuka = false }) {
            if (opsiKosongRes != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(opsiKosongRes)) },
                    onClick = {
                        onPilihKosong()
                        terbuka = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            pilihan.forEach { item ->
                DropdownMenuItem(
                    text = { Text(teksPilihan(item)) },
                    onClick = {
                        onPilih(item)
                        terbuka = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// Versi khusus enum (kategori, siklus, status): pilihan otomatis semua nilai enum,
// teks diambil dari label enum di strings.xml.
// Contoh: FieldPilihanEnum(terpilih = form.siklus, onPilih = viewModel::ubahSiklus, labelRes = R.string.label_siklus)
@Composable
inline fun <reified T> FieldPilihanEnum(
    terpilih: T?,
    noinline onPilih: (T) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    enabled: Boolean = true,
    @StringRes opsiKosongRes: Int? = null,
    noinline onPilihKosong: () -> Unit = {}
) where T : Enum<T>, T : PunyaLabel {
    FieldPilihan(
        pilihan = enumValues<T>().toList(),
        terpilih = terpilih,
        onPilih = onPilih,
        labelRes = labelRes,
        teksPilihan = { stringResource(it.label) },
        modifier = modifier,
        errorServer = errorServer,
        errorRes = errorRes,
        enabled = enabled,
        opsiKosongRes = opsiKosongRes,
        onPilihKosong = onPilihKosong
    )
}

// Field tanggal dengan DatePicker Material 3.
// value dan onValueChange memakai format API (YYYY-MM-DD), yang tampil format Indonesia (5 Des 2026).
// bolehKosong = true menampilkan tombol hapus, contoh untuk next_billing_date yang opsional.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldTanggal(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    errorServer: String? = null,
    @StringRes errorRes: Int? = null,
    @StringRes supportingRes: Int? = null,
    enabled: Boolean = true,
    bolehKosong: Boolean = false
) {
    var dialogTerbuka by rememberSaveable { mutableStateOf(false) }
    val error = teksError(errorServer, errorRes)

    // Field readOnly tidak punya onClick, jadi ketukan dideteksi dari interactionSource
    val interaksi = remember { MutableInteractionSource() }
    LaunchedEffect(interaksi, enabled) {
        interaksi.interactions.collect {
            if (it is PressInteraction.Release && enabled) dialogTerbuka = true
        }
    }

    OutlinedTextField(
        value = tanggalTampilan(value),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(labelRes)) },
        trailingIcon = {
            Row {
                if (bolehKosong && value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }, enabled = enabled) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.cd_hapus_tanggal))
                    }
                }
                IconButton(onClick = { dialogTerbuka = true }, enabled = enabled) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.cd_pilih_tanggal))
                }
            }
        },
        isError = error != null,
        supportingText = teksBawah(error, supportingRes),
        singleLine = true,
        enabled = enabled,
        interactionSource = interaksi,
        modifier = modifier.fillMaxWidth()
    )

    if (dialogTerbuka) {
        val state = rememberDatePickerState(initialSelectedDateMillis = tanggalApiKeMillis(value))
        DatePickerDialog(
            onDismissRequest = { dialogTerbuka = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { onValueChange(millisKeTanggalApi(it)) }
                        dialogTerbuka = false
                    },
                    enabled = state.selectedDateMillis != null
                ) {
                    Text(stringResource(R.string.btn_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogTerbuka = false }) {
                    Text(stringResource(R.string.btn_batal))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
