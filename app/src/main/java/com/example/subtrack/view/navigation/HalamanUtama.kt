package com.example.subtrack.view.navigation

import androidx.compose.foundation.layout.Box
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

// Halaman utama setelah login: Scaffold + NavigationBar bawah, menu sesuai role
@Composable
fun HalamanUtama(role: Role, modifier: Modifier = Modifier) {
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
        NavHost(
            navController = navControllerTab,
            startDestination = MenuUtama.DASHBOARD.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            menu.forEach { item ->
                composable(item.route) {
                    // Sementara: isi tab berupa placeholder, diganti layar fitur di langkah berikutnya
                    PlaceholderTab(judul = stringResource(item.titleRes))
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
