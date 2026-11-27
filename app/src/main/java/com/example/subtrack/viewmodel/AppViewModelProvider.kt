package com.example.subtrack.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.subtrack.SubTrackApplication
import com.example.subtrack.viewmodel.auth.LoginViewModel
import com.example.subtrack.viewmodel.auth.RegisterViewModel
import com.example.subtrack.viewmodel.auth.SesiViewModel

// Factory semua ViewModel. Dipakai di layar: viewModel(factory = AppViewModelProvider.Factory)
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(subTrackApplication().container.authRepository) }
        initializer { RegisterViewModel(subTrackApplication().container.authRepository) }
        initializer { SesiViewModel(subTrackApplication().container.authRepository) }
    }
}

// Mengambil instance SubTrackApplication dari CreationExtras untuk mengakses AppContainer
fun CreationExtras.subTrackApplication(): SubTrackApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SubTrackApplication
