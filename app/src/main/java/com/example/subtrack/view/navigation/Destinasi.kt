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
import com.example.subtrack.viewmodel.katalog.FormLayananViewModel

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
