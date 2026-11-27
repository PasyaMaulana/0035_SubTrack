package com.example.subtrack.repositori

import android.app.Application

// Class Application: dibuat sekali saat aplikasi dijalankan, menyimpan ContainerApp
class AplikasiSubTrack : Application() {

    lateinit var container: ContainerApp

    override fun onCreate() {
        super.onCreate()
        container = ContainerDataApp(this)
    }
}
