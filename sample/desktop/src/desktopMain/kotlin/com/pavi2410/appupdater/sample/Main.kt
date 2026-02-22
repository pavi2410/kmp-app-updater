package com.pavi2410.appupdater.sample

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pavi2410.appupdater.GitHubUpdater
import com.pavi2410.appupdater.PlatformContext
import com.pavi2410.appupdater.ui.UpdateCard

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "App Updater Sample") {
        val updater = remember {
            GitHubUpdater(
                owner = "pavi2410",
                repo = "kmp-app-updater",
                currentVersion = "0.1.0",
                platformContext = PlatformContext(),
            )
        }

        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "App Updater Sample (Desktop)",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    UpdateCard(updater = updater)
                }
            }
        }
    }
}
