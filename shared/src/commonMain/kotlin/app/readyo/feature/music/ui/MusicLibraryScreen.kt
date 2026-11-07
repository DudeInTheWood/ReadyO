package app.readyo.feature.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.readyo.core.designsystem.component.ReadyOEmptyState
import app.readyo.core.designsystem.component.ReadyOErrorState
import app.readyo.core.designsystem.component.ScreenHeader
import app.readyo.core.designsystem.component.SectionHeader
import app.readyo.core.playback.PlaybackStatus
import app.readyo.core.playback.PlayerState
import app.readyo.feature.music.presentation.MusicLibraryAction
import app.readyo.feature.music.presentation.MusicLibraryScope
import app.readyo.feature.music.presentation.MusicLibraryUiState

@Composable
fun MusicLibraryScreen(
    state: MusicLibraryUiState,
    modifier: Modifier = Modifier,
    playback: PlayerState,
    onToggle: () -> Unit,
    onShuffle: () -> Unit,
    onImport: () -> Unit,
    onAddFolder: () -> Unit,
    onScanDevice: () -> Unit,
    onRefresh: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onBrowseAction: (MusicLibraryAction) -> Unit,
    onPlay: (Int) -> Unit,
) {
    var showSources by remember { mutableStateOf(false) }
    val showingFolders = state.scope == MusicLibraryScope.Folders && state.selectedFolder == null
    Box(modifier, contentAlignment = Alignment.TopCenter) {
        key(state.scope, state.selectedFolder?.directory?.id) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item("header") {
                    ScreenHeader(
                        "The collection.",
                        "ARCHIVE EDITION / SIDE B",
                        actionLabel =
                            if (state.tracks.isEmpty()) null
                            else if (showSources) "Done" else "Sources",
                        onAction = { showSources = !showSources },
                    )
                }
                if (!showingFolders)
                    item("catalog-sleeve") { CatalogSleeve(state.visibleTracks.size) }
                item("scopes") {
                    MusicScopeSelector(state.scope) {
                        onBrowseAction(MusicLibraryAction.ChangeScope(it))
                    }
                }
                item("sources") {
                    AnimatedVisibility(visible = showSources || state.tracks.isEmpty()) {
                        MusicSourcesPanel(
                            state,
                            onImport,
                            onAddFolder,
                            onScanDevice,
                            onRefresh,
                            onRemoveFolder,
                        )
                    }
                }
                if (state.isLoading || state.isImporting || state.isScanning) {
                    item("progress") { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                }
                state.error?.let { message ->
                    item("error") { ReadyOErrorState(message, onRefresh) }
                }
                state.selectedFolder?.let { folder ->
                    item("folder-heading") {
                        MusicFolderHeading(folder) {
                            onBrowseAction(MusicLibraryAction.BackToFolders)
                        }
                    }
                }
                item("collection-heading") {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SectionHeader(
                            if (showingFolders) "${state.collections.size} folders"
                            else songCount(state.visibleTracks.size)
                        )
                        if (!showingFolders && state.visibleTracks.isNotEmpty()) {
                            Row {
                                TextButton(onClick = { onPlay(0) }) { Text("Play all") }
                                TextButton(onClick = onShuffle) { Text("Shuffle") }
                            }
                        }
                    }
                }
                if (showingFolders) {
                    if (!state.isLoading && !state.isScanning && state.collections.isEmpty()) {
                        item("empty-folders") {
                            ReadyOEmptyState(
                                "A place for every song",
                                "Scan device music or link a folder from Sources. Your music folders will appear here.",
                            )
                        }
                    }
                    items(state.collections, key = { "folder:${it.directory.id}" }) { folder ->
                        MusicFolderRow(folder) {
                            onBrowseAction(MusicLibraryAction.OpenFolder(folder.directory.id))
                        }
                    }
                } else {
                    if (!state.isLoading && !state.isScanning && state.visibleTracks.isEmpty()) {
                        item("empty-songs") {
                            ReadyOEmptyState(
                                if (state.selectedFolder != null) "No songs in this folder"
                                else "A little room for your music",
                                "Add music from Sources, then rescan to refresh your library.",
                            )
                        }
                    }
                    itemsIndexed(state.visibleTracks, key = { _, track -> "track:${track.id}" }) {
                        index,
                        track ->
                        val current = playback.takeIf { it.item?.id == track.id }
                        TrackRow(track, index, current) {
                            if (
                                current?.status in
                                    listOf(PlaybackStatus.Playing, PlaybackStatus.Paused)
                            )
                                onToggle()
                            else onPlay(index)
                        }
                    }
                }
            }
        }
    }
}
