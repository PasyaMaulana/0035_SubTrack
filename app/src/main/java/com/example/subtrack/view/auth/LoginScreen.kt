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
import com.example.subtrack.view.components.TombolUtama
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.auth.LoginViewModel

// Layar masuk: email dan password
@Composable
fun LoginScreen(
    onLoginBerhasil: () -> Unit,
    onKeRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current

    // Login sukses -> lanjut ke layar berikutnya (diatur navigasi)
    LaunchedEffect(uiState.berhasil) {
        if (uiState.berhasil) onLoginBerhasil()
    }

    KerangkaAuth(
        judulRes = R.string.login_title,
        subjudulRes = R.string.login_subtitle,
        pesanError = uiState.pesanErrorServer ?: uiState.pesanErrorRes?.let { stringResource(it) },
        onPesanErrorTampil = viewModel::hapusPesanError,
        modifier = modifier
    ) {
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
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.login()
            })
        )
        TombolUtama(
            teksRes = R.string.login_button,
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.login()
            }
        )
        TautanAuth(
            teksRes = R.string.login_belum_punya_akun,
            enabled = !uiState.isLoading,
            onClick = onKeRegister
        )
    }
}
