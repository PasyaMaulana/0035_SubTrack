package com.example.subtrack

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.subtrack.view.auth.LoginScreen
import com.example.subtrack.view.auth.RegisterScreen
import com.example.subtrack.view.theme.SubTrackTheme

class MainActivity : ComponentActivity() {

    // Hasil permintaan izin jaringan lokal; jika ditolak, beri tahu user
    private val mintaIzinJaringanLokal =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { diizinkan ->
            if (!diizinkan) {
                Toast.makeText(this, R.string.izin_jaringan_lokal, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        periksaIzinJaringanLokal()
        setContent {
            SubTrackTheme {
                // Sementara: pindah login <-> register tanpa NavGraph (navigasi lengkap di langkah 7)
                var tampilRegister by rememberSaveable { mutableStateOf(false) }
                if (tampilRegister) {
                    RegisterScreen(
                        onRegisterBerhasil = {},
                        onKeLogin = { tampilRegister = false }
                    )
                } else {
                    LoginScreen(
                        onLoginBerhasil = {},
                        onKeRegister = { tampilRegister = true }
                    )
                }
            }
        }
    }

    // Android 17 (API 37) mewajibkan izin runtime untuk mengakses server di jaringan lokal
    private fun periksaIzinJaringanLokal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) return
        val izin = Manifest.permission.ACCESS_LOCAL_NETWORK
        if (ContextCompat.checkSelfPermission(this, izin) != PackageManager.PERMISSION_GRANTED) {
            mintaIzinJaringanLokal.launch(izin)
        }
    }
}
