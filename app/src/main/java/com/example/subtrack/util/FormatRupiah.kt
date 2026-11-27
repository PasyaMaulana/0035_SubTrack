package com.example.subtrack.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.subtrack.R
import java.text.NumberFormat
import java.util.Locale

// Locale Indonesia: pemisah ribuan berupa titik (186000 -> 186.000)
val LOCALE_INDONESIA: Locale = Locale.forLanguageTag("id-ID")

// Angka dengan pemisah ribuan tanpa awalan Rp, contoh 186.000
fun formatRibuan(nilai: Long): String =
    NumberFormat.getIntegerInstance(LOCALE_INDONESIA).format(nilai)

// Rupiah untuk layar Composable, contoh Rp186.000 (awalan dari string format_rupiah)
@Composable
fun teksRupiah(nilai: Long): String =
    stringResource(R.string.format_rupiah, formatRibuan(nilai))

// Rupiah di luar Composable (misalnya ekspor CSV)
fun Context.formatRupiah(nilai: Long): String =
    getString(R.string.format_rupiah, formatRibuan(nilai))

// Ubah input pengguna ("186.000" atau "186000") menjadi angka; null jika kosong/tidak valid
fun angkaDariTeks(teks: String): Long? =
    teks.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toLongOrNull()
