package com.example.subtrack.model.dto

import androidx.annotation.StringRes
import com.example.subtrack.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Pilihan tetap aplikasi. Nilai JSON lewat @SerialName, teks tampilan lewat label (strings.xml).
// Nama konstanta sama dengan nilai API supaya bisa langsung dipakai sebagai query parameter.

// Kontrak enum yang punya teks tampilan, dipakai FieldPilihanEnum dan chip status
interface PunyaLabel {
    @get:StringRes val label: Int
}

@Serializable
enum class Role(@param:StringRes override val label: Int) : PunyaLabel {
    @SerialName("ADMIN") ADMIN(R.string.role_admin),
    @SerialName("USER") USER(R.string.role_user)
}

@Serializable
enum class Category(@param:StringRes override val label: Int) : PunyaLabel {
    @SerialName("STREAMING_VIDEO") STREAMING_VIDEO(R.string.kategori_streaming_video),
    @SerialName("MUSIK") MUSIK(R.string.kategori_musik),
    @SerialName("CLOUD_STORAGE") CLOUD_STORAGE(R.string.kategori_cloud_storage),
    @SerialName("PRODUKTIVITAS") PRODUKTIVITAS(R.string.kategori_produktivitas),
    @SerialName("GAMING") GAMING(R.string.kategori_gaming),
    @SerialName("LAINNYA") LAINNYA(R.string.kategori_lainnya)
}

@Serializable
enum class BillingCycle(@param:StringRes override val label: Int) : PunyaLabel {
    @SerialName("BULANAN") BULANAN(R.string.siklus_bulanan),
    @SerialName("TRIWULAN") TRIWULAN(R.string.siklus_triwulan),
    @SerialName("TAHUNAN") TAHUNAN(R.string.siklus_tahunan)
}

@Serializable
enum class SubscriptionStatus(@param:StringRes override val label: Int) : PunyaLabel {
    @SerialName("ACTIVE") ACTIVE(R.string.status_active),
    @SerialName("PAUSED") PAUSED(R.string.status_paused),
    @SerialName("CANCELLED") CANCELLED(R.string.status_cancelled)
}

@Serializable
enum class DuesStatus(@param:StringRes override val label: Int) : PunyaLabel {
    @SerialName("LUNAS") LUNAS(R.string.iuran_lunas),
    @SerialName("BELUM_LUNAS") BELUM_LUNAS(R.string.iuran_belum_lunas)
}
