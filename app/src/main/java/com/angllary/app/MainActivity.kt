package com.angllary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.angllary.app.data.MediaStoreRepository
import com.angllary.app.ui.AngllaryApp
import com.angllary.app.ui.theme.AngllaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AngllaryTheme {
                val repository = remember { MediaStoreRepository(contentResolver) }
                AngllaryApp(repository = repository)
            }
        }
    }
}
