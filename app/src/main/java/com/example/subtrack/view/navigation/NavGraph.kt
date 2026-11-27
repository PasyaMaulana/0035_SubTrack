package com.example.subtrack.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subtrack.model.dto.Role
import com.example.subtrack.view.auth.LoginScreen
import com.example.subtrack.view.auth.RegisterScreen
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.viewmodel.AppViewModelProvider
import com.example.subtrack.viewmodel.auth.SesiViewModel
import com.example.subtrack.viewmodel.auth.StatusSesi

// Grafik navigasi utama aplikasi
@Composable
fun SubTrackNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sesiViewModel: SesiViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val status by sesiViewModel.status.collectAsStateWithLifecycle()

    // Tunggu sesi selesai dibaca dari DataStore sebelum menentukan layar awal
    if (status is StatusSesi.Memuat) {
        LoadingView(modifier = modifier)
        return
    }

    // Layar awal ditentukan sekali saat aplikasi dibuka: ada sesi -> halaman utama, tidak ada -> login
    val ruteAwal = rememberSaveable {
        if (status is StatusSesi.SudahLogin) DestinasiUtama.route else DestinasiLogin.route
    }

    // Sesi terhapus (logout atau refresh token gagal) -> kembali ke login, back stack dikosongkan
    LaunchedEffect(status) {
        if (status !is StatusSesi.BelumLogin) return@LaunchedEffect
        val ruteSekarang = navController.currentDestination?.route
        if (ruteSekarang != DestinasiLogin.route && ruteSekarang != DestinasiRegister.route) {
            navController.navigate(DestinasiLogin.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ruteAwal,
        modifier = modifier
    ) {
        composable(DestinasiLogin.route) {
            LoginScreen(
                onLoginBerhasil = { navController.keHalamanUtama() },
                onKeRegister = { navController.navigate(DestinasiRegister.route) }
            )
        }
        composable(DestinasiRegister.route) {
            RegisterScreen(
                onRegisterBerhasil = { navController.keHalamanUtama() },
                // Register selalu dibuka dari login, jadi cukup kembali ke layar sebelumnya
                onKeLogin = { navController.popBackStack() }
            )
        }
        composable(DestinasiUtama.route) {
            val role = (status as? StatusSesi.SudahLogin)?.role ?: Role.USER
            HalamanUtama(role = role)
        }
    }
}

// Pindah ke halaman utama dan hapus login (serta register) dari back stack,
// supaya tombol kembali tidak membuka layar login lagi
private fun NavHostController.keHalamanUtama() {
    navigate(DestinasiUtama.route) {
        popUpTo(DestinasiLogin.route) { inclusive = true }
    }
}
