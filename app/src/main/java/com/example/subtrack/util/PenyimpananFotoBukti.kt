package com.example.subtrack.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

// Penyimpanan foto bukti pembayaran di penyimpanan internal aplikasi (filesDir/bukti/).
// Server hanya menyimpan nama filenya (proof_file_name), fotonya tetap di perangkat.
class PenyimpananFotoBukti(context: Context) {

    // applicationContext supaya tidak menahan Activity
    private val appContext = context.applicationContext

    private val folder: File
        get() = File(appContext.filesDir, NAMA_FOLDER).apply { mkdirs() }

    // Salin gambar dari Photo Picker ke folder bukti dengan nama unik.
    // Mengembalikan nama file, atau null jika gagal dibaca/disalin.
    suspend fun simpan(uri: Uri): String? = withContext(Dispatchers.IO) {
        val tipe = appContext.contentResolver.getType(uri)
        val ekstensi = tipe?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: EKSTENSI_DEFAULT
        val nama = "$AWALAN${UUID.randomUUID()}.$ekstensi"
        val tujuan = File(folder, nama)
        try {
            val input = appContext.contentResolver.openInputStream(uri) ?: return@withContext null
            input.use { sumber -> tujuan.outputStream().use { sumber.copyTo(it) } }
            nama
        } catch (e: IOException) {
            tujuan.delete()
            null
        } catch (e: SecurityException) {
            // Izin baca uri sudah habis
            tujuan.delete()
            null
        }
    }

    // File dari nama yang tersimpan; null jika kosong atau filenya tidak ada di perangkat ini
    // (contoh: dicatat dari perangkat lain atau aplikasi pernah di-install ulang)
    fun ambilFile(nama: String?): File? {
        if (nama.isNullOrBlank()) return null
        return fileDariNama(nama).takeIf { it.isFile }
    }

    // Hapus file bukti. Operasi file kecil, jadi aman dipanggil juga dari onCleared ViewModel.
    fun hapus(nama: String?) {
        if (nama.isNullOrBlank()) return
        fileDariNama(nama).delete()
    }

    // File(nama).name membuang bagian folder, supaya nama dari server tidak bisa keluar dari folder bukti
    private fun fileDariNama(nama: String): File = File(folder, File(nama).name)

    private companion object {
        const val NAMA_FOLDER = "bukti"
        const val AWALAN = "bukti_"
        const val EKSTENSI_DEFAULT = "jpg"
    }
}
