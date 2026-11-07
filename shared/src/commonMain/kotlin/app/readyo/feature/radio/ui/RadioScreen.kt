package app.readyo.feature.radio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.readyo.core.designsystem.component.ReadyOEmptyState
import app.readyo.core.designsystem.component.ReadyOErrorState
import app.readyo.core.designsystem.component.ScreenHeader
import app.readyo.core.designsystem.component.SectionHeader
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlaybackStatus
import app.readyo.feature.radio.presentation.RadioAction
import app.readyo.feature.radio.presentation.RadioFilter
import app.readyo.feature.radio.presentation.RadioUiState

@Composable
fun RadioScreen(
    state: RadioUiState,
    playbackStatus: PlaybackStatus,
    onToggle: () -> Unit,
    isSearch: Boolean,
    lastStation: AudioItem?,
    modifier: Modifier = Modifier,
    onAction: (RadioAction) -> Unit,
    onSearch: (RadioFilter) -> Unit,
    onBack: () -> Unit,
    onPlay: (List<AudioItem>, Int) -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    title = if (isSearch) "On the dial." else "ReadyO",
                    subtitle = if (isSearch) "THE WORLD ON TAPE" else "ARCHIVE EDITION / SIDE A",
                    actionLabel = if (isSearch) "Back" else "Search",
                    onAction =
                        if (isSearch) onBack
                        else {
                            { onSearch(RadioFilter.All) }
                        },
                )
            }
            item {
                PresetDial(
                    state.stations.size,
                    state.stations.indexOfFirst { it.id == lastStation?.id },
                    playbackStatus,
                )
            }
            if (isSearch) {
                item {
                    RadioSearchScreen(
                        state.query,
                        { onAction(RadioAction.SearchChanged(it)) },
                        state.filter,
                        { onAction(RadioAction.FilterChanged(it)) },
                    )
                }
                item { SectionHeader("Station index · ${state.stations.size}") }
            } else {
                item {
                    RadioHomeScreen(
                        lastStation,
                        playbackStatus,
                        {
                            if (
                                playbackStatus == PlaybackStatus.Playing ||
                                    playbackStatus == PlaybackStatus.Paused
                            )
                                onToggle()
                            else onPlay(listOf(it), 0)
                        },
                        onSearch,
                    )
                }
                item { SectionHeader("Popular stations") }
            }
            if (state.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            state.error?.let { message ->
                item { ReadyOErrorState(message) { onAction(RadioAction.Retry) } }
            }
            if (!state.isLoading && state.error == null && state.stations.isEmpty()) {
                item {
                    ReadyOEmptyState("No matching stations", "Try another station name or preset.")
                }
            }
            itemsIndexed(state.stations, key = { _, station -> station.id }) { index, station ->
                val current = station.id == lastStation?.id
                StationRow(station, index, playbackStatus.takeIf { current }) {
                    if (
                        current &&
                            playbackStatus in listOf(PlaybackStatus.Playing, PlaybackStatus.Paused)
                    )
                        onToggle()
                    else onPlay(state.stations, index)
                }
            }
        }
    }
}
