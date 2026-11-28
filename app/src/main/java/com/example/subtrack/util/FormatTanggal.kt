package com.example.subtrack.util

import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException

// Format tanggal API (YYYY-MM-DD)
private val FORMAT_API: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// Format tampilan Indonesia, contoh 5 Des 2026 (nama bulan dari Locale Indonesia)
private val FORMAT_TAMPILAN: DateTimeFormatter = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("d MMM yyyy")
    .toFormatter(LOCALE_INDONESIA)

// Teks API -> LocalDate; null jika kosong atau formatnya salah
fun parseTanggalApi(tanggal: String?): LocalDate? {
    if (tanggal.isNullOrBlank()) return null
    return try {
        // take(10) agar tetap aman jika server mengirim tanggal-waktu ISO
        LocalDate.parse(tanggal.take(10), FORMAT_API)
    } catch (e: DateTimeParseException) {
        null
    }
}

// "2026-12-05" -> "5 Des 2026". Teks yang tidak bisa dibaca dikembalikan apa adanya.
fun tanggalTampilan(tanggalApi: String?): String =
    parseTanggalApi(tanggalApi)?.format(FORMAT_TAMPILAN) ?: tanggalApi.orEmpty()

// Tanggal-waktu ISO UTC dari server (contoh created_at "2026-11-28T00:30:00.000Z") -> "28 Nov 2026"
// menurut zona waktu perangkat, supaya tanggal tidak mundur sehari. Selain format itu, dibaca seperti tanggal biasa.
fun tanggalWaktuTampilan(tanggalWaktuIso: String?): String {
    if (tanggalWaktuIso.isNullOrBlank()) return ""
    return try {
        Instant.parse(tanggalWaktuIso).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMAT_TAMPILAN)
    } catch (e: DateTimeParseException) {
        tanggalTampilan(tanggalWaktuIso)
    }
}

// "5 Des 2026" -> "2026-12-05"; null jika formatnya salah
fun tanggalKeApi(tanggalTampilan: String): String? = try {
    LocalDate.parse(tanggalTampilan.trim(), FORMAT_TAMPILAN).format(FORMAT_API)
} catch (e: DateTimeParseException) {
    null
}

// Tanggal hari ini dalam format API, dipakai sebagai nilai awal form
fun hariIniApi(): String = LocalDate.now().format(FORMAT_API)

// DatePicker Material 3 memakai milidetik UTC tengah malam, jadi konversinya memakai UTC
fun millisKeTanggalApi(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(FORMAT_API)

fun tanggalApiKeMillis(tanggalApi: String?): Long? =
    parseTanggalApi(tanggalApi)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
