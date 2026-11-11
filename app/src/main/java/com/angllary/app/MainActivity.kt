package com.angllary.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.core.view.WindowCompat
import com.angllary.app.data.GalleryStore
import com.angllary.app.data.MediaStoreRepository
import com.angllary.app.ui.AngllaryApp
import com.angllary.app.ui.theme.AngllaryTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val galleryStore = remember { GalleryStore(applicationContext) }
            var themeMode by remember { mutableStateOf(galleryStore.themeMode()) }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            AngllaryTheme(darkTheme = darkTheme) {
                val repository = remember { MediaStoreRepository(contentResolver) }
                AngllaryApp(
                    repository = repository,
                    galleryStore = galleryStore,
                    onThemeModeChange = { themeMode = it }
                )
            }
        }
    }
}
