package com.example.subtrack.view.auth

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subtrack.R
import com.example.subtrack.view.components.FieldPassword
import com.example.subtrack.view.components.FieldTeks
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.auth.RegisterViewModel

// Layar daftar akun: nama, email, password, dan konfirmasi password
@Composable
fun RegisterScreen(
    onRegisterBerhasil: () -> Unit,
    onKeLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current

    // Register sukses -> sesi sudah tersimpan, lanjut ke layar berikutnya (diatur navigasi)
    LaunchedEffect(uiState.berhasil) {
        if (uiState.berhasil) onRegisterBerhasil()
    }

    KerangkaAuth(
        judulRes = R.string.register_title,
        subjudulRes = R.string.register_subtitle,
        pesanError = uiState.pesanErrorServer ?: uiState.pesanErrorRes?.let { stringResource(it) },
        onPesanErrorTampil = viewModel::hapusPesanError,
        modifier = modifier
    ) {
        FieldTeks(
            value = uiState.nama,
            onValueChange = viewModel::onNamaChange,
            labelRes = R.string.label_nama,
            errorServer = uiState.errorNamaServer,
            errorRes = uiState.errorNama,
            enabled = !uiState.isLoading
        )
        FieldTeks(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            labelRes = R.string.label_email,
            errorServer = uiState.errorEmailServer,
            errorRes = uiState.errorEmail,
            enabled = !uiState.isLoading,
            keyboardType = KeyboardType.Email
        )
        FieldPassword(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            labelRes = R.string.label_password,
            errorServer = uiState.errorPasswordServer,
            errorRes = uiState.errorPassword,
            enabled = !uiState.isLoading
        )
        // Konfirmasi hanya divalidasi lokal, server tidak mengirim error untuk field ini
        FieldPassword(
            value = uiState.konfirmasiPassword,
            onValueChange = viewModel::onKonfirmasiChange,
            labelRes = R.string.label_konfirmasi_password,
            errorRes = uiState.errorKonfirmasi,
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.register()
            })
        )
        TombolUtama(
            teksRes = R.string.register_button,
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.register()
            }
        )
        TautanAuth(
            teksRes = R.string.register_sudah_punya_akun,
            enabled = !uiState.isLoading,
            onClick = onKeLogin
        )
    }
}
