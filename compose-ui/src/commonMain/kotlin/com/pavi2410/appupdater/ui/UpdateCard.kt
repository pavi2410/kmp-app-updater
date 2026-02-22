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
 * A full-featured update card that shows the current state of the updater
 * and provides actions to check, download, and install updates.
 */
@Composable
fun UpdateCard(
    updater: AppUpdater,
    modifier: Modifier = Modifier,
) {
    val state by updater.state.collectAsState()
    val scope = rememberCoroutineScope()
    val currentVersion = updater.currentVersion

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Current version: v$currentVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when (val s = state) {
                is UpdateState.Idle -> {
                    Text("Tap below to check for updates", style = MaterialTheme.typography.bodyMedium)
                }

                is UpdateState.Checking -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(modifier = Modifier.weight(1f))
                        Text("Checking...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                is UpdateState.UpdateAvailable -> {
                    Text(
                        text = "v$currentVersion → v${s.release.version}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (s.release.publishedAt.isNotEmpty()) {
                        Text(
                            text = "Released: ${s.release.publishedAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Changelog:", style = MaterialTheme.typography.labelMedium)
                    Text(s.release.changelog, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { scope.launch { updater.downloadUpdate() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Download Update (${formatSize(s.asset.size)})")
                    }
                }

                is UpdateState.UpToDate -> {
                    Text(
                        text = "You're up to date!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is UpdateState.Downloading -> {
                    Text("Downloading update...", style = MaterialTheme.typography.bodyMedium)
                    DownloadProgressIndicator(
                        progress = s.progress,
                        bytesDownloaded = s.bytesDownloaded,
                        totalBytes = s.totalBytes,
                    )
                }

                is UpdateState.ReadyToInstall -> {
                    Text(
                        text = "Download complete! Ready to install.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { updater.installUpdate() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Install Update")
                    }
                }

                is UpdateState.Error -> {
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (state is UpdateState.Idle || state is UpdateState.UpToDate || state is UpdateState.Error) {
                OutlinedButton(
                    onClick = { scope.launch { updater.checkForUpdate() } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Check for Updates")
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}
