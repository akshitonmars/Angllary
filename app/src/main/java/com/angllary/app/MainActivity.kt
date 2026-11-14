package com.angllary.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.angllary.app.data.GalleryStore
import com.angllary.app.data.MediaStoreRepository
import com.angllary.app.ui.AngllaryApp
import com.angllary.app.ui.theme.AngllaryTheme
import com.angllary.app.util.AppIconManager

class MainActivity : FragmentActivity() {
    private val incomingIntentState = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val galleryStore = GalleryStore(applicationContext)
        val initialThemeMode = galleryStore.themeMode()
        val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isLight = when (initialThemeMode) {
            "light" -> true
            "dark", "amoled" -> false
            else -> !isSystemDark
        }
        setTheme(if (isLight) R.style.Theme_Angllary_Light else R.style.Theme_Angllary_Dark)
        super.onCreate(savedInstanceState)
        incomingIntentState.value = intent
        enableEdgeToEdge()
        AppIconManager.syncIconWithTheme(applicationContext, initialThemeMode, isSystemDark)

        setContent {
            var themeMode by remember { mutableStateOf(initialThemeMode) }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark", "amoled" -> true
                else -> systemDark
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            AngllaryTheme(darkTheme = darkTheme, pureBlack = themeMode == "amoled") {
                val repository = remember { MediaStoreRepository(contentResolver) }
                AngllaryApp(
                    repository = repository,
                    galleryStore = galleryStore,
                    onThemeModeChange = {
                        themeMode = it
                        AppIconManager.syncIconWithTheme(applicationContext, it, systemDark)
                    },
                    incomingIntent = incomingIntentState.value,
                    onPickerResult = { uri ->
                        val result = Intent().apply {
                            setData(uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingIntentState.value = intent
    }
}