package com.pavi2410.appupdater.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavi2410.appupdater.AppUpdater
import com.pavi2410.appupdater.github
import com.pavi2410.appupdater.ui.UpdateCard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val updater = remember {
                AppUpdater.github(
                    context = applicationContext,
                    owner = "pavi2410",
                    repo = "kmp-app-updater",
                )
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "App Updater Sample",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        UpdateCard(updater = updater)
                    }
                }
            }
        }
    }
}
