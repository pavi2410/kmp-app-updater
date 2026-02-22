package com.pavi2410.appupdater.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavi2410.appupdater.AppUpdater
import com.pavi2410.appupdater.UpdateState
import kotlinx.coroutines.launch

/**
 * A minimal inline banner that shows update status.
 * Useful for embedding at the top of a screen or in a settings list.
 */
@Composable
fun UpdateBanner(
    updater: AppUpdater,
    modifier: Modifier = Modifier,
) {
    val state by updater.state.collectAsState()
    val scope = rememberCoroutineScope()

    when (val s = state) {
        is UpdateState.UpdateAvailable -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Update available: v${s.release.version}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { scope.launch { updater.downloadUpdate() } }) {
                        Text("Update")
                    }
                }
            }
        }

        is UpdateState.Downloading -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Downloading update...", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { s.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        is UpdateState.ReadyToInstall -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ready to install",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { updater.installUpdate() }) {
                        Text("Install")
                    }
                }
            }
        }

        else -> {
            // Don't show banner for Idle, Checking, UpToDate, Error
        }
    }
}
