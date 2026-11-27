package com.example.subtrack.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.subtrack.viewmodel.PesanLayar

// Ubah PesanLayar dari ViewModel menjadi teks: pesan server didahulukan, lalu strings.xml
@Composable
fun PesanLayar.teks(): String = teksServer ?: stringResource(teksRes)
