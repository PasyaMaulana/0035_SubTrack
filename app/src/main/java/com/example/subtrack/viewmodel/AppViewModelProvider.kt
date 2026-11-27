package com.example.subtrack.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.subtrack.SubTrackApplication
import com.example.subtrack.viewmodel.auth.LoginViewModel
import com.example.subtrack.viewmodel.auth.RegisterViewModel
import com.example.subtrack.viewmodel.auth.SesiViewModel
import com.example.subtrack.viewmodel.katalog.FormLayananViewModel
import com.example.subtrack.viewmodel.katalog.KatalogViewModel
import com.example.subtrack.viewmodel.langganan.DetailLanggananViewModel
import com.example.subtrack.viewmodel.langganan.FormLanggananViewModel
import com.example.subtrack.viewmodel.langganan.LanggananViewModel
import com.example.subtrack.viewmodel.pembayaran.FormPembayaranViewModel
import com.example.subtrack.viewmodel.pembayaran.RiwayatPembayaranViewModel

// Factory semua ViewModel. Dipakai di layar: viewModel(factory = AppViewModelProvider.Factory)
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(subTrackApplication().container.authRepository) }
        initializer { RegisterViewModel(subTrackApplication().container.authRepository) }
        initializer { SesiViewModel(subTrackApplication().container.authRepository) }

        // Katalog layanan (admin)
        initializer { KatalogViewModel(subTrackApplication().container.serviceRepository) }
        initializer {
            // SavedStateHandle berisi argumen navigasi (id layanan saat mode ubah)
            FormLayananViewModel(createSavedStateHandle(), subTrackApplication().container.serviceRepository)
        }

        // Langganan (user)
        initializer { LanggananViewModel(subTrackApplication().container.subscriptionRepository) }
        initializer {
            // SavedStateHandle berisi argumen navigasi (id langganan).
            // PaymentRepository dan PenyimpananFotoBukti dipakai untuk menghapus foto bukti saat langganan dihapus.
            val container = subTrackApplication().container
            DetailLanggananViewModel(
                createSavedStateHandle(),
                container.subscriptionRepository,
                container.paymentRepository,
                container.penyimpananFotoBukti
            )
        }
        initializer {
            // SavedStateHandle berisi id langganan saat mode ubah; daftar layanan dari ServiceRepository
            val container = subTrackApplication().container
            FormLanggananViewModel(createSavedStateHandle(), container.subscriptionRepository, container.serviceRepository)
        }

        // Pembayaran (user)
        initializer {
            // SavedStateHandle berisi id langganan dari rute detail langganan
            val container = subTrackApplication().container
            RiwayatPembayaranViewModel(createSavedStateHandle(), container.paymentRepository, container.penyimpananFotoBukti)
        }
        initializer {
            // SavedStateHandle berisi id langganan dan id pembayaran (mode ubah)
            val container = subTrackApplication().container
            FormPembayaranViewModel(
                createSavedStateHandle(),
                container.paymentRepository,
                container.subscriptionRepository,
                container.penyimpananFotoBukti
            )
        }
    }
}

// Mengambil instance SubTrackApplication dari CreationExtras untuk mengakses AppContainer
fun CreationExtras.subTrackApplication(): SubTrackApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SubTrackApplication
