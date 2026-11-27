package com.example.subtrack.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.subtrack.repositori.AplikasiSubTrack

// Factory semua ViewModel. Dipakai di layar: viewModel(factory = PenyediaViewModel.Factory)
object PenyediaViewModel {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(aplikasiSubTrack().container.authRepository) }
        initializer { RegisterViewModel(aplikasiSubTrack().container.authRepository) }
    }
}

// Mengambil instance AplikasiSubTrack dari CreationExtras untuk mengakses ContainerApp
fun CreationExtras.aplikasiSubTrack(): AplikasiSubTrack =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiSubTrack
