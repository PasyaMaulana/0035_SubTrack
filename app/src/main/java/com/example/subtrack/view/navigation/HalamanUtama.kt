package com.example.subtrack.view.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.subtrack.model.dto.Role
import com.example.subtrack.view.dashboard.DashboardScreen
import com.example.subtrack.view.grup.GrupScreen
import com.example.subtrack.view.katalog.KatalogScreen
import com.example.subtrack.view.langganan.LanggananScreen
import com.example.subtrack.view.profil.ProfilScreen

// Halaman utama setelah login: Scaffold + NavigationBar bawah, menu sesuai role.
// Layar form (tambah/ubah) dibuka di NavGraph utama lewat callback onXxx supaya tampil tanpa NavigationBar.
// pesanHasil = pesan dari form yang baru ditutup, diteruskan ke tab yang sedang tampil.
// perluMuatUlang = data berubah dari layar lain (detail langganan/grup), daftar tab yang tampil dimuat ulang tanpa pesan.
// onTandaiMuatUlang = minta tab lain memuat ulang daftarnya (dipakai dashboard setelah menerima pesan hasil).
@Composable
fun HalamanUtama(
    role: Role,
    nama: String,
    onTambahLayanan: () -> Unit,
    onUbahLayanan: (Int) -> Unit,
    onTambahLangganan: () -> Unit,
    onDetailLangganan: (Int) -> Unit,
    onTambahGrup: () -> Unit,
    onDetailGrup: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    perluMuatUlang: Boolean,
    onMuatUlangDiterima: () -> Unit,
    onTandaiMuatUlang: () -> Unit,
    modifier: Modifier = Modifier
) {
    // NavController terpisah khusus perpindahan antar-tab
    val navControllerTab = rememberNavController()
    val entryAktif by navControllerTab.currentBackStackEntryAsState()
    val menu = MenuUtama.untukRole(role)

    // Pindah tab dengan pola standar bottom navigation: back stack tab tidak menumpuk,
    // state tiap tab disimpan dan dipulihkan
    fun pindahTab(tujuan: MenuUtama) {
        navControllerTab.navigate(tujuan.route) {
            popUpTo(navControllerTab.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                menu.forEach { item ->
                    val terpilih = entryAktif?.destination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = terpilih,
                        onClick = { pindahTab(item) },
                        icon = { Icon(item.ikon, contentDescription = null) },
                        label = { Text(stringResource(item.titleRes)) }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // Hanya padding bawah (tinggi NavigationBar). Padding atas tidak dipakai karena
        // setiap tab punya Scaffold + TopAppBar sendiri yang sudah mengurus status bar.
        val paddingBawah = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        NavHost(
            navController = navControllerTab,
            startDestination = MenuUtama.DASHBOARD.route,
            modifier = Modifier
                .padding(paddingBawah)
                .consumeWindowInsets(paddingBawah)
        ) {
            menu.forEach { item ->
                composable(item.route) {
                    when (item) {
                        MenuUtama.DASHBOARD -> DashboardScreen(
                            role = role,
                            nama = nama,
                            onDetailLangganan = onDetailLangganan,
                            onKePatungan = { pindahTab(MenuUtama.GRUP) },
                            pesanHasil = pesanHasil,
                            // Dashboard tidak memegang daftar; tab Langganan/Patungan diminta memuat ulang
                            onPesanHasilDiterima = {
                                onPesanHasilDiterima()
                                onTandaiMuatUlang()
                            }
                        )
                        MenuUtama.KATALOG -> KatalogScreen(
                            onTambah = onTambahLayanan,
                            onUbah = onUbahLayanan,
                            pesanHasil = pesanHasil,
                            onPesanHasilDiterima = onPesanHasilDiterima
                        )
                        MenuUtama.LANGGANAN -> LanggananScreen(
                            onTambah = onTambahLangganan,
                            onDetail = onDetailLangganan,
                            pesanHasil = pesanHasil,
                            onPesanHasilDiterima = onPesanHasilDiterima,
                            perluMuatUlang = perluMuatUlang,
                            onMuatUlangDiterima = onMuatUlangDiterima
                        )
                        MenuUtama.GRUP -> GrupScreen(
                            onTambah = onTambahGrup,
                            onDetail = onDetailGrup,
                            pesanHasil = pesanHasil,
                            onPesanHasilDiterima = onPesanHasilDiterima,
                            perluMuatUlang = perluMuatUlang,
                            onMuatUlangDiterima = onMuatUlangDiterima
                        )
                        MenuUtama.PROFIL -> ProfilScreen()
                    }
                }
            }
        }
    }
}
