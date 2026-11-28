package com.example.subtrack.view.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.subtrack.R
import com.example.subtrack.model.dto.Role
import com.example.subtrack.viewmodel.grup.DetailGrupViewModel
import com.example.subtrack.viewmodel.grup.FormGrupViewModel
import com.example.subtrack.viewmodel.katalog.FormLayananViewModel
import com.example.subtrack.viewmodel.langganan.DetailLanggananViewModel
import com.example.subtrack.viewmodel.langganan.FormLanggananViewModel
import com.example.subtrack.viewmodel.pembayaran.FormPembayaranViewModel

// Kontrak setiap destinasi: route (alamat navigasi) dan judul layar
interface DestinasiNavigasi {
    val route: String
    @get:StringRes val titleRes: Int
}

object DestinasiLogin : DestinasiNavigasi {
    override val route = "login"
    override val titleRes = R.string.login_title
}

object DestinasiRegister : DestinasiNavigasi {
    override val route = "register"
    override val titleRes = R.string.register_title
}

// Halaman utama: wadah NavigationBar bawah beserta tab-tabnya
object DestinasiUtama : DestinasiNavigasi {
    override val route = "utama"
    override val titleRes = R.string.app_name
}

// Form tambah/ubah layanan katalog (admin). id opsional: tanpa id = tambah, dengan id = ubah
object DestinasiFormLayanan : DestinasiNavigasi {
    private const val BASE = "form_layanan"
    const val ARG_ID = FormLayananViewModel.ARG_ID
    override val route = "$BASE?$ARG_ID={$ARG_ID}"
    override val titleRes = R.string.title_tambah_layanan

    // Rute tujuan navigasi, contoh "form_layanan" atau "form_layanan?id=5"
    fun buatRute(id: Int? = null): String = if (id == null) BASE else "$BASE?$ARG_ID=$id"
}

// Detail langganan (user). id wajib, contoh "detail_langganan/5"
object DestinasiDetailLangganan : DestinasiNavigasi {
    private const val BASE = "detail_langganan"
    const val ARG_ID = DetailLanggananViewModel.ARG_ID
    override val route = "$BASE/{$ARG_ID}"
    override val titleRes = R.string.title_detail_langganan

    fun buatRute(id: Int): String = "$BASE/$id"
}

// Form tambah/ubah langganan (user). id opsional: tanpa id = tambah, dengan id = ubah
object DestinasiFormLangganan : DestinasiNavigasi {
    private const val BASE = "form_langganan"
    const val ARG_ID = FormLanggananViewModel.ARG_ID
    override val route = "$BASE?$ARG_ID={$ARG_ID}"
    override val titleRes = R.string.title_tambah_langganan

    // Rute tujuan navigasi, contoh "form_langganan" atau "form_langganan?id=5"
    fun buatRute(id: Int? = null): String = if (id == null) BASE else "$BASE?$ARG_ID=$id"
}

// Form tambah/ubah pembayaran satu langganan. idLangganan wajib, idPembayaran opsional (ada = ubah)
object DestinasiFormPembayaran : DestinasiNavigasi {
    private const val BASE = "form_pembayaran"
    const val ARG_ID_LANGGANAN = FormPembayaranViewModel.ARG_ID_LANGGANAN
    const val ARG_ID_PEMBAYARAN = FormPembayaranViewModel.ARG_ID_PEMBAYARAN
    override val route = "$BASE/{$ARG_ID_LANGGANAN}?$ARG_ID_PEMBAYARAN={$ARG_ID_PEMBAYARAN}"
    override val titleRes = R.string.title_tambah_pembayaran

    // Contoh "form_pembayaran/5" (tambah) atau "form_pembayaran/5?idPembayaran=9" (ubah)
    fun buatRute(idLangganan: Int, idPembayaran: Int? = null): String =
        if (idPembayaran == null) "$BASE/$idLangganan" else "$BASE/$idLangganan?$ARG_ID_PEMBAYARAN=$idPembayaran"
}

// Detail grup patungan (user). id wajib, contoh "detail_grup/3"
object DestinasiDetailGrup : DestinasiNavigasi {
    private const val BASE = "detail_grup"
    const val ARG_ID = DetailGrupViewModel.ARG_ID
    override val route = "$BASE/{$ARG_ID}"
    override val titleRes = R.string.title_detail_grup

    fun buatRute(id: Int): String = "$BASE/$id"
}

// Form tambah/ubah grup patungan (user). id opsional: tanpa id = tambah, dengan id = ubah
object DestinasiFormGrup : DestinasiNavigasi {
    private const val BASE = "form_grup"
    const val ARG_ID = FormGrupViewModel.ARG_ID
    override val route = "$BASE?$ARG_ID={$ARG_ID}"
    override val titleRes = R.string.title_tambah_grup

    // Rute tujuan navigasi, contoh "form_grup" atau "form_grup?id=3"
    fun buatRute(id: Int? = null): String = if (id == null) BASE else "$BASE?$ARG_ID=$id"
}

// Tab di NavigationBar halaman utama
enum class MenuUtama(
    override val route: String,
    @param:StringRes override val titleRes: Int,
    val ikon: ImageVector
) : DestinasiNavigasi {
    DASHBOARD("tab_dashboard", R.string.menu_dashboard, Icons.Filled.Dashboard),
    KATALOG("tab_katalog", R.string.menu_katalog, Icons.Filled.Storefront),
    LANGGANAN("tab_langganan", R.string.menu_langganan, Icons.Filled.Subscriptions),
    GRUP("tab_grup", R.string.menu_grup, Icons.Filled.Groups),
    PROFIL("tab_profil", R.string.menu_profil, Icons.Filled.Person);

    companion object {
        // Menu berbeda per role: admin mengelola katalog, user mencatat langganan dan patungan
        fun untukRole(role: Role): List<MenuUtama> = when (role) {
            Role.ADMIN -> listOf(DASHBOARD, KATALOG, PROFIL)
            Role.USER -> listOf(DASHBOARD, LANGGANAN, GRUP, PROFIL)
        }
    }
}
