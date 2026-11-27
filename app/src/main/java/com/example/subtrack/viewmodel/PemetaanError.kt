package com.example.subtrack.viewmodel

import com.example.subtrack.repositori.HasilApi

// Hasil pemetaan errors[] dari backend ke form
data class ErrorTerpetakan(
    // nama field API -> pesan error pertama untuk field itu
    val perField: Map<String, String>,
    // pesan error pertama yang tidak cocok dengan field form mana pun
    val pesanUmum: String?
)

// Memisahkan errors[] menjadi error per field form dan pesan umum.
// fieldForm = nama field API yang punya input di form, contoh setOf("email", "password")
fun HasilApi.Gagal.petakanErrorField(fieldForm: Set<String>): ErrorTerpetakan {
    val (cocok, lain) = errorField.partition { it.field in fieldForm }
    return ErrorTerpetakan(
        perField = cocok.groupBy { it.field.orEmpty() }.mapValues { it.value.first().message },
        pesanUmum = lain.firstOrNull()?.message
    )
}
