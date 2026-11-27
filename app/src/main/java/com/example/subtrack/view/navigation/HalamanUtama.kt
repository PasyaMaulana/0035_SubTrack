package com.example.subtrack.view.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.subtrack.R
import com.example.subtrack.model.dto.Role
import com.example.subtrack.view.katalog.KatalogScreen
import com.example.subtrack.view.langganan.LanggananScreen

// Halaman utama setelah login: Scaffold + NavigationBar bawah, menu sesuai role.
// Layar form (tambah/ubah) dibuka di NavGraph utama lewat callback onXxx supaya tampil tanpa NavigationBar.
// pesanHasil = pesan dari form yang baru ditutup, diteruskan ke tab yang sedang tampil.
@Composable
fun HalamanUtama(
    role: Role,
    onTambahLayanan: () -> Unit,
    onUbahLayanan: (Int) -> Unit,
    onTambahLangganan: () -> Unit,
    onDetailLangganan: (Int) -> Unit,
    @StringRes pesanHasil: Int?,
    onPesanHasilDiterima: () -> Unit,
    modifier: Modifier = Modifier
) {
    // NavController terpisah khusus perpindahan antar-tab
    val navControllerTab = rememberNavController()
    val entryAktif by navControllerTab.currentBackStackEntryAsState()
    val menu = MenuUtama.untukRole(role)

    Scaffold(
        bottomBar = {
            NavigationBar {
                menu.forEach { item ->
                    val terpilih = entryAktif?.destination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = terpilih,
                        onClick = {
                            navControllerTab.navigate(item.route) {
                                // Pola standar bottom navigation: back stack tab tidak menumpuk,
                                // state tiap tab disimpan dan dipulihkan
                                popUpTo(navControllerTab.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
                            onPesanHasilDiterima = onPesanHasilDiterima
                        )
                        // Sementara: tab lain masih placeholder, diganti layar fitur di langkah berikutnya
                        else -> PlaceholderTab(judul = stringResource(item.titleRes))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderTab(judul: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_large))
    ) {
        Text(
            text = stringResource(R.string.msg_segera_hadir, judul),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
