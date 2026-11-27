package com.example.subtrack

import android.app.Application
import com.example.subtrack.di.AppContainer
import com.example.subtrack.di.DefaultAppContainer

// Class Application: dibuat sekali saat aplikasi dijalankan, menyimpan AppContainer
class SubTrackApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
