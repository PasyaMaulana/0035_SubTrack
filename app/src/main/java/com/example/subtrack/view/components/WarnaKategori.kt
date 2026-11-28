package com.example.subtrack.view.components

import androidx.annotation.ColorRes
import com.example.subtrack.R
import com.example.subtrack.model.dto.Category

// Warna khas tiap kategori layanan dari colors.xml (versi gelap di values-night/colors.xml).
// Dipakai lewat colorResource(kategori.warnaRes()), contoh bar pengeluaran di dashboard.
@ColorRes
fun Category.warnaRes(): Int = when (this) {
    Category.STREAMING_VIDEO -> R.color.kategori_streaming_video
    Category.MUSIK -> R.color.kategori_musik
    Category.CLOUD_STORAGE -> R.color.kategori_cloud_storage
    Category.PRODUKTIVITAS -> R.color.kategori_produktivitas
    Category.GAMING -> R.color.kategori_gaming
    Category.LAINNYA -> R.color.kategori_lainnya
}
