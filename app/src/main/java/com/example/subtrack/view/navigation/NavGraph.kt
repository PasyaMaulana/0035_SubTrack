package com.example.subtrack.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subtrack.model.dto.Role
import com.example.subtrack.view.anggota.FormAnggotaScreen
import com.example.subtrack.view.auth.LoginScreen
import com.example.subtrack.view.auth.RegisterScreen
import com.example.subtrack.view.components.LoadingView
import com.example.subtrack.view.grup.DetailGrupScreen
import com.example.subtrack.view.grup.FormGrupScreen
import com.example.subtrack.view.katalog.FormLayananScreen
import com.example.subtrack.view.langganan.DetailLanggananScreen
import com.example.subtrack.view.langganan.FormLanggananScreen
import com.example.subtrack.view.pembayaran.FormPembayaranScreen
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
        composable(DestinasiUtama.route) { entry ->
            val role = (status as? StatusSesi.SudahLogin)?.role ?: Role.USER
            // Pesan hasil dari form (disimpan form di savedStateHandle entry ini sebelum ditutup)
            val pesanHasil by entry.savedStateHandle
                .getStateFlow<Int?>(KUNCI_PESAN_HASIL, null)
                .collectAsStateWithLifecycle()
            // Penanda muat ulang daftar langganan (diset saat langganan diubah dari layar detail)
            val perluMuatUlang by entry.savedStateHandle
                .getStateFlow(KUNCI_MUAT_ULANG, false)
                .collectAsStateWithLifecycle()
            HalamanUtama(
                role = role,
                onTambahLayanan = { navController.navigate(DestinasiFormLayanan.buatRute()) },
                onUbahLayanan = { id -> navController.navigate(DestinasiFormLayanan.buatRute(id)) },
                onTambahLangganan = { navController.navigate(DestinasiFormLangganan.buatRute()) },
                onDetailLangganan = { id -> navController.navigate(DestinasiDetailLangganan.buatRute(id)) },
                onTambahGrup = { navController.navigate(DestinasiFormGrup.buatRute()) },
                onDetailGrup = { id -> navController.navigate(DestinasiDetailGrup.buatRute(id)) },
                pesanHasil = pesanHasil,
                onPesanHasilDiterima = { entry.savedStateHandle[KUNCI_PESAN_HASIL] = null },
                perluMuatUlang = perluMuatUlang,
                onMuatUlangDiterima = { entry.savedStateHandle[KUNCI_MUAT_ULANG] = false }
            )
        }
        composable(
            route = DestinasiFormLayanan.route,
            // id opsional: default -1 berarti mode tambah
            arguments = listOf(navArgument(DestinasiFormLayanan.ARG_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            FormLayananScreen(
                onKembali = { navController.navigateUp() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) }
            )
        }
        composable(
            route = DestinasiDetailLangganan.route,
            arguments = listOf(navArgument(DestinasiDetailLangganan.ARG_ID) { type = NavType.IntType })
        ) { entry ->
            // Pesan hasil dari form ubah yang dibuka dari detail ini
            val pesanHasil by entry.savedStateHandle
                .getStateFlow<Int?>(KUNCI_PESAN_HASIL, null)
                .collectAsStateWithLifecycle()
            DetailLanggananScreen(
                onKembali = { navController.navigateUp() },
                onUbah = { id -> navController.navigate(DestinasiFormLangganan.buatRute(id)) },
                onTambahPembayaran = { idLangganan ->
                    navController.navigate(DestinasiFormPembayaran.buatRute(idLangganan))
                },
                onUbahPembayaran = { idLangganan, idPembayaran ->
                    navController.navigate(DestinasiFormPembayaran.buatRute(idLangganan, idPembayaran))
                },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) },
                pesanHasil = pesanHasil,
                onPesanHasilDiterima = { entry.savedStateHandle[KUNCI_PESAN_HASIL] = null }
            )
        }
        composable(
            route = DestinasiFormLangganan.route,
            // id opsional: default -1 berarti mode tambah
            arguments = listOf(navArgument(DestinasiFormLangganan.ARG_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            FormLanggananScreen(
                onKembali = { navController.navigateUp() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) }
            )
        }
        composable(
            route = DestinasiFormPembayaran.route,
            // idPembayaran opsional: default -1 berarti mode tambah
            arguments = listOf(
                navArgument(DestinasiFormPembayaran.ARG_ID_LANGGANAN) { type = NavType.IntType },
                navArgument(DestinasiFormPembayaran.ARG_ID_PEMBAYARAN) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            // Dibuka dari detail langganan; pesan hasil membuat detail memuat ulang riwayat pembayaran
            FormPembayaranScreen(
                onKembali = { navController.navigateUp() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) }
            )
        }
        composable(
            route = DestinasiDetailGrup.route,
            arguments = listOf(navArgument(DestinasiDetailGrup.ARG_ID) { type = NavType.IntType })
        ) { entry ->
            // Pesan hasil dari form ubah yang dibuka dari detail ini
            val pesanHasil by entry.savedStateHandle
                .getStateFlow<Int?>(KUNCI_PESAN_HASIL, null)
                .collectAsStateWithLifecycle()
            DetailGrupScreen(
                onKembali = { navController.navigateUp() },
                onUbah = { id -> navController.navigate(DestinasiFormGrup.buatRute(id)) },
                onTambahAnggota = { idGrup -> navController.navigate(DestinasiFormAnggota.buatRute(idGrup)) },
                onUbahAnggota = { idGrup, idAnggota ->
                    navController.navigate(DestinasiFormAnggota.buatRute(idGrup, idAnggota))
                },
                // Status iuran diubah atau anggota dihapus langsung di detail -> daftar grup ikut dimuat ulang
                onAnggotaBerubah = { navController.tandaiMuatUlangUtama() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) },
                pesanHasil = pesanHasil,
                onPesanHasilDiterima = { entry.savedStateHandle[KUNCI_PESAN_HASIL] = null }
            )
        }
        composable(
            route = DestinasiFormAnggota.route,
            // idAnggota opsional: default -1 berarti mode tambah
            arguments = listOf(
                navArgument(DestinasiFormAnggota.ARG_ID_GRUP) { type = NavType.IntType },
                navArgument(DestinasiFormAnggota.ARG_ID_ANGGOTA) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            // Dibuka dari detail grup; pesan hasil membuat detail memuat ulang anggota dan ringkasan iuran
            FormAnggotaScreen(
                onKembali = { navController.navigateUp() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) }
            )
        }
        composable(
            route = DestinasiFormGrup.route,
            // id opsional: default -1 berarti mode tambah
            arguments = listOf(navArgument(DestinasiFormGrup.ARG_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            FormGrupScreen(
                onKembali = { navController.navigateUp() },
                onSelesai = { pesanRes -> navController.kembaliDenganPesan(pesanRes) }
            )
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

// Tutup layar dan titipkan pesan hasil ke layar sebelumnya (halaman utama atau detail).
// Jika layar sebelumnya bukan halaman utama (contoh form ubah dibuka dari detail),
// halaman utama juga diberi penanda agar daftarnya dimuat ulang saat dibuka kembali.
private fun NavHostController.kembaliDenganPesan(pesanRes: Int) {
    val sebelumnya = previousBackStackEntry
    sebelumnya?.savedStateHandle?.set(KUNCI_PESAN_HASIL, pesanRes)
    if (sebelumnya?.destination?.route != DestinasiUtama.route) tandaiMuatUlangUtama()
    popBackStack()
}

// Beri penanda agar daftar di halaman utama dimuat ulang saat dibuka kembali
private fun NavHostController.tandaiMuatUlangUtama() {
    val entryUtama = try {
        getBackStackEntry(DestinasiUtama.route)
    } catch (e: IllegalArgumentException) {
        null
    }
    entryUtama?.savedStateHandle?.set(KUNCI_MUAT_ULANG, true)
}

// Kunci savedStateHandle untuk pesan hasil form dan penanda muat ulang daftar
private const val KUNCI_PESAN_HASIL = "pesan_hasil"
private const val KUNCI_MUAT_ULANG = "muat_ulang"
