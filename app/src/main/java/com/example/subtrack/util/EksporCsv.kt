package com.example.subtrack.util

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.example.subtrack.R
import com.example.subtrack.model.dto.Payment
import com.example.subtrack.model.dto.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDate

// Membuat laporan CSV (daftar langganan + riwayat pembayaran) di cacheDir/laporan/
// lalu memberi Uri FileProvider supaya file bisa dibagikan lewat Intent.ACTION_SEND.
class EksporCsv(context: Context) {

    // applicationContext supaya tidak menahan Activity
    private val appContext = context.applicationContext

    // Tulis file laporan. pembayaran = riwayat per id langganan.
    // Mengembalikan Uri untuk dibagikan, atau null jika file gagal ditulis.
    suspend fun buatLaporan(
        daftarLangganan: List<Subscription>,
        pembayaran: Map<Int, List<Payment>>
    ): Uri? = withContext(Dispatchers.IO) {
        val hariIni = LocalDate.now().toString() // YYYY-MM-DD
        val folder = File(appContext.cacheDir, NAMA_FOLDER)
        val file = File(folder, "$AWALAN_NAMA_FILE$hariIni.csv")
        try {
            // Laporan lama dihapus supaya cache tidak menumpuk
            folder.mkdirs()
            folder.listFiles()?.forEach { it.delete() }

            val isi = susunCsv(daftarLangganan, pembayaran, hariIni)
            // BOM UTF-8 di awal file supaya Excel membaca huruf non-ASCII dengan benar
            file.outputStream().use { keluaran ->
                keluaran.write(BOM_UTF8)
                keluaran.write(isi.toByteArray(Charsets.UTF_8))
            }
            FileProvider.getUriForFile(appContext, "${appContext.packageName}$AKHIRAN_AUTHORITY", file)
        } catch (e: IOException) {
            file.delete()
            null
        } catch (e: IllegalArgumentException) {
            // Folder tidak terdaftar di file_paths.xml
            file.delete()
            null
        }
    }

    // Susun isi CSV: judul, tabel langganan, baris kosong, tabel pembayaran
    private fun susunCsv(
        daftarLangganan: List<Subscription>,
        pembayaran: Map<Int, List<Payment>>,
        hariIni: String
    ): String = buildString {
        baris(teks(R.string.csv_judul_laporan))
        baris(teks(R.string.csv_tanggal_ekspor), hariIni)
        baris()

        baris(teks(R.string.csv_judul_langganan))
        baris(
            teks(R.string.csv_kolom_layanan),
            teks(R.string.csv_kolom_kategori),
            teks(R.string.csv_kolom_paket),
            teks(R.string.csv_kolom_harga),
            teks(R.string.csv_kolom_siklus),
            teks(R.string.csv_kolom_status),
            teks(R.string.csv_kolom_tanggal_mulai),
            teks(R.string.csv_kolom_tagihan_berikutnya),
            teks(R.string.csv_kolom_metode)
        )
        daftarLangganan.forEach { langganan ->
            baris(
                langganan.service?.name.orEmpty(),
                langganan.service?.category?.let { teks(it.label) }.orEmpty(),
                langganan.planName,
                // Angka tanpa pemisah ribuan supaya bisa dijumlah di Excel
                langganan.price.toString(),
                teks(langganan.billingCycle.label),
                teks(langganan.status.label),
                langganan.startDate,
                langganan.nextBillingDate.orEmpty(),
                langganan.paymentMethod.orEmpty()
            )
        }
        baris()

        baris(teks(R.string.csv_judul_pembayaran))
        baris(
            teks(R.string.csv_kolom_layanan),
            teks(R.string.csv_kolom_tanggal_bayar),
            teks(R.string.csv_kolom_nominal),
            teks(R.string.csv_kolom_metode),
            teks(R.string.csv_kolom_catatan)
        )
        daftarLangganan.forEach { langganan ->
            pembayaran[langganan.id].orEmpty().forEach { bayar ->
                baris(
                    langganan.service?.name.orEmpty(),
                    bayar.paidAt,
                    bayar.amount.toString(),
                    bayar.method.orEmpty(),
                    bayar.note.orEmpty()
                )
            }
        }
    }

    private fun teks(@StringRes id: Int): String = appContext.getString(id)

    // Satu baris CSV; baris diakhiri CRLF sesuai format CSV (RFC 4180)
    private fun StringBuilder.baris(vararg kolom: String) {
        append(kolom.joinToString(PEMISAH.toString()) { escapeCsv(it) })
        append(AKHIR_BARIS)
    }

    companion object {
        private const val NAMA_FOLDER = "laporan"
        private const val AWALAN_NAMA_FILE = "laporan_subtrack_"
        // Harus sama dengan android:authorities di AndroidManifest
        private const val AKHIRAN_AUTHORITY = ".fileprovider"
        // Pemisah kolom titik koma: sesuai pengaturan wilayah Indonesia di Excel (koma dipakai untuk desimal)
        private const val PEMISAH = ';'
        private const val AKHIR_BARIS = "\r\n"
        private val BOM_UTF8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

        // Karakter awal yang dianggap rumus oleh Excel (=, +, -, @)
        private val AWALAN_RUMUS = charArrayOf('=', '+', '-', '@')

        // Escape satu nilai CSV:
        // - teks yang diawali karakter rumus diberi petik satu supaya Excel tidak menjalankannya sebagai rumus
        // - nilai yang mengandung pemisah kolom (titik koma), kutip, atau baris baru dibungkus kutip,
        //   kutip di dalamnya digandakan
        fun escapeCsv(nilai: String): String {
            val aman = if (nilai.isNotEmpty() && nilai[0] in AWALAN_RUMUS && nilai.toLongOrNull() == null) {
                "'$nilai"
            } else {
                nilai
            }
            val perluKutip = aman.any { it == PEMISAH || it == '"' || it == '\n' || it == '\r' }
            return if (perluKutip) "\"${aman.replace("\"", "\"\"")}\"" else aman
        }
    }
}
