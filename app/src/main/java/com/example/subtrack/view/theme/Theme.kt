package com.example.subtrack.view.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Skema warna terang dari palet di Color.kt
private val SkemaTerang = lightColorScheme(
    primary = BiruPrimer,
    onPrimary = PadaPrimer,
    primaryContainer = BiruPrimerKontainer,
    onPrimaryContainer = PadaPrimerKontainer,
    secondary = TealSekunder,
    onSecondary = PadaSekunder,
    secondaryContainer = TealSekunderKontainer,
    onSecondaryContainer = PadaSekunderKontainer,
    tertiary = OranyeTersier,
    onTertiary = PadaTersier,
    tertiaryContainer = OranyeTersierKontainer,
    onTertiaryContainer = PadaTersierKontainer,
    error = MerahError,
    onError = PadaError,
    errorContainer = MerahErrorKontainer,
    onErrorContainer = PadaErrorKontainer,
    background = LatarTerang,
    onBackground = PadaLatarTerang,
    surface = LatarTerang,
    onSurface = PadaLatarTerang,
    surfaceVariant = PermukaanVarianTerang,
    onSurfaceVariant = PadaPermukaanVarianTerang,
    outline = GarisTerang,
    outlineVariant = GarisVarianTerang
)

// Skema warna gelap dari palet di Color.kt
private val SkemaGelap = darkColorScheme(
    primary = BiruPrimerGelap,
    onPrimary = PadaPrimerGelap,
    primaryContainer = BiruPrimerKontainerGelap,
    onPrimaryContainer = PadaPrimerKontainerGelap,
    secondary = TealSekunderGelap,
    onSecondary = PadaSekunderGelap,
    secondaryContainer = TealSekunderKontainerGelap,
    onSecondaryContainer = PadaSekunderKontainerGelap,
    tertiary = OranyeTersierGelap,
    onTertiary = PadaTersierGelap,
    tertiaryContainer = OranyeTersierKontainerGelap,
    onTertiaryContainer = PadaTersierKontainerGelap,
    error = MerahErrorGelap,
    onError = PadaErrorGelap,
    errorContainer = MerahErrorKontainerGelap,
    onErrorContainer = PadaErrorKontainerGelap,
    background = LatarGelap,
    onBackground = PadaLatarGelap,
    surface = LatarGelap,
    onSurface = PadaLatarGelap,
    surfaceVariant = PermukaanVarianGelap,
    onSurfaceVariant = PadaPermukaanVarianGelap,
    outline = GarisGelap,
    outlineVariant = GarisVarianGelap
)

// Tema aplikasi. Default memakai palet SubTrack (dynamicColor = false)
// supaya tampilan konsisten di semua perangkat.
@Composable
fun SubTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Warna dinamis Material You hanya tersedia di Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SkemaGelap
        else -> SkemaTerang
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
