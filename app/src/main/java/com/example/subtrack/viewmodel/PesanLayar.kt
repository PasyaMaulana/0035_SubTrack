package com.example.subtrack.viewmodel

import androidx.annotation.StringRes
import com.example.subtrack.R
import com.example.subtrack.model.repository.HasilApi

// Pesan untuk layar (Snackbar atau ErrorView) tanpa ViewModel memegang teks.
// teksServer = message dari backend (ditampilkan apa adanya), diutamakan.
// teksRes    = pesan lokal dari strings.xml, dipakai jika server tidak memberi pesan.
data class PesanLayar(
    val teksServer: String? = null,
    @param:StringRes val teksRes: Int = R.string.error_umum
)

// Ubah hasil gagal dari repository menjadi pesan layar
fun HasilApi.Gagal.kePesanLayar(): PesanLayar = PesanLayar(teksServer = pesanServer, teksRes = pesanRes)
