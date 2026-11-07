package app.readyo.feature.music.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.readyo.core.designsystem.component.SectionHeader
import app.readyo.feature.music.presentation.MusicLibraryUiState

@Composable
internal fun MusicSourcesPanel(
    state: MusicLibraryUiState,
    onImport: () -> Unit,
    onAddFolder: () -> Unit,
    onScanDevice: () -> Unit,
    onRefresh: () -> Unit,
    onRemoveFolder: (String) -> Unit,
) {
    val busy = state.isScanning || state.isImporting
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeader("Music sources")
            Text(
                if (state.canLinkFolders)
                    "Linked music stays where it is. Individual imports are copied into ReadyO."
                else "Imported files are saved in ReadyO for offline listening.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = if (state.canLinkFolders) onAddFolder else onImport,
                enabled = !busy,
                shape = RoundedCornerShape(7.dp),
            ) {
                Text(if (state.canLinkFolders) "Choose music folder" else "Import audio files")
            }
            if (state.canScanDevice) {
                OutlinedButton(
                    onClick = onScanDevice,
                    enabled = !busy,
                    shape = RoundedCornerShape(7.dp),
                ) {
                    Text("Scan device music")
                }
            }
            if (state.canLinkFolders) {
                TextButton(onClick = onImport, enabled = !busy) { Text("Import individual files") }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (state.deviceScanEnabled) "Device scan enabled"
                    else songCount(state.tracks.size),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRefresh, enabled = !busy) { Text("Rescan") }
            }
            state.folders.forEach { folder ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(folder.name)
                        Text(
                            songCount(folder.trackCount),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onRemoveFolder(folder.id) }, enabled = !busy) {
                        Text("Unlink")
                    }
                }
            }
        }
    }
}
