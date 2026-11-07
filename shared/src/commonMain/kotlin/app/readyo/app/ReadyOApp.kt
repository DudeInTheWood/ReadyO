package app.readyo.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.readyo.core.designsystem.theme.ReadyOTheme
import app.readyo.core.playback.PlaybackStatus
import app.readyo.feature.music.presentation.MusicLibraryAction
import app.readyo.feature.music.presentation.MusicLibraryViewModel
import app.readyo.feature.music.ui.MusicLibraryScreen
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.presentation.PlayerViewModel
import app.readyo.feature.player.ui.MiniPlayer
import app.readyo.feature.player.ui.NowPlayingScreen
import app.readyo.feature.player.ui.motion.PlayerDragSurface
import app.readyo.feature.player.ui.motion.rememberDeckMotion
import app.readyo.feature.radio.presentation.RadioAction
import app.readyo.feature.radio.presentation.RadioViewModel
import app.readyo.feature.radio.ui.RadioScreen

/** Route binds lifecycle-owned ViewModels. All screen composables below are stateless. */
@Composable
fun ReadyOApp(
    dependencies: AppDependencies,
    onImportRequested: ((List<String>) -> Unit) -> Unit,
    onFolderRequested: ((String?) -> Unit) -> Unit = { it(null) },
    onDeviceScanRequested: (() -> Unit) -> Unit = { it() },
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
) {
    val appViewModel = viewModel { AppViewModel() }
    val radioViewModel = viewModel { RadioViewModel(dependencies.radioRepository) }
    val musicViewModel = viewModel { MusicLibraryViewModel(dependencies.musicRepository) }
    val playerViewModel = viewModel { PlayerViewModel(dependencies.player) }

    val destination by appViewModel.destination.collectAsStateWithLifecycle()
    val radioState by radioViewModel.uiState.collectAsStateWithLifecycle()
    val musicState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val playback = playerState.playback
    val expanded = playerState.isExpanded

    val browsingMusicFolder =
        destination == AppDestination.MusicLibrary && musicState.selectedFolder != null
    backHandler(expanded || destination == AppDestination.RadioSearch || browsingMusicFolder) {
        if (expanded) playerViewModel.onAction(PlayerAction.Collapse)
        else if (browsingMusicFolder) musicViewModel.onAction(MusicLibraryAction.BackToFolders)
        else appViewModel.onAction(AppAction.Back)
    }

    ReadyOTheme {
        val motion = rememberDeckMotion(playback.status == PlaybackStatus.Playing, enabled = true)
        val backgroundScale by
            animateFloatAsState(
                targetValue = if (expanded) 0.965f else 1f,
                animationSpec = spring(stiffness = 230f),
                label = "Library depth",
            )

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Scaffold(
                modifier =
                    Modifier.graphicsLayer {
                            scaleX = backgroundScale
                            scaleY = backgroundScale
                        }
                        .semantics { if (expanded) hideFromAccessibility() },
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    Column {
                        AnimatedVisibility(
                            visible = playback.item != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            MiniPlayer(playback, motion, playerViewModel::onAction)
                        }
                        ReadyOBottomNavigation(
                            radio = destination != AppDestination.MusicLibrary,
                            onSelect = { radio ->
                                val target =
                                    if (radio) AppDestination.RadioHome
                                    else AppDestination.MusicLibrary
                                appViewModel.onAction(AppAction.Navigate(target))
                            },
                        )
                    }
                },
            ) { contentPadding ->
                val contentModifier = Modifier.fillMaxSize().padding(contentPadding)
                when (destination) {
                    AppDestination.RadioHome,
                    AppDestination.RadioSearch ->
                        RadioScreen(
                            state = radioState,
                            playbackStatus = playback.status,
                            onToggle = { playerViewModel.onAction(PlayerAction.TogglePlayback) },
                            isSearch = destination == AppDestination.RadioSearch,
                            lastStation = playback.item?.takeIf { it.radio },
                            modifier = contentModifier,
                            onAction = radioViewModel::onAction,
                            onSearch = { filter ->
                                radioViewModel.onAction(RadioAction.FilterChanged(filter))
                                appViewModel.onAction(
                                    AppAction.Navigate(AppDestination.RadioSearch)
                                )
                            },
                            onBack = { appViewModel.onAction(AppAction.Back) },
                            onPlay = { items, index ->
                                val selected = items.getOrNull(index)
                                if (selected?.radio == true && items.size == 1) {
                                    val context = (items + radioState.stations).distinctBy { it.id }
                                    playerViewModel.onAction(PlayerAction.Select(context, 0))
                                } else {
                                    playerViewModel.onAction(PlayerAction.Select(items, index))
                                }
                            },
                        )
                    AppDestination.MusicLibrary ->
                        MusicLibraryScreen(
                            state = musicState,
                            playback = playback,
                            onToggle = { playerViewModel.onAction(PlayerAction.TogglePlayback) },
                            onShuffle = {
                                playerViewModel.onAction(
                                    PlayerAction.Select(musicState.visibleTracks.shuffled(), 0)
                                )
                            },
                            modifier = contentModifier,
                            onImport = {
                                onImportRequested { sources ->
                                    musicViewModel.onAction(MusicLibraryAction.ImportFiles(sources))
                                }
                            },
                            onAddFolder = {
                                onFolderRequested { source ->
                                    source?.let {
                                        musicViewModel.onAction(MusicLibraryAction.AddFolder(it))
                                    }
                                }
                            },
                            onScanDevice = {
                                onDeviceScanRequested {
                                    musicViewModel.onAction(MusicLibraryAction.ScanDevice)
                                }
                            },
                            onRefresh = { musicViewModel.onAction(MusicLibraryAction.Refresh) },
                            onRemoveFolder = {
                                musicViewModel.onAction(MusicLibraryAction.RemoveFolder(it))
                            },
                            onBrowseAction = musicViewModel::onAction,
                            onPlay = { index ->
                                playerViewModel.onAction(
                                    PlayerAction.Select(musicState.visibleTracks, index)
                                )
                            },
                        )
                }
            }

            AnimatedVisibility(
                visible = expanded && playback.item != null,
                enter =
                    slideInVertically(spring(dampingRatio = 0.86f, stiffness = 230f)) { it } +
                        fadeIn(tween(200)),
                exit =
                    slideOutVertically(tween(330, easing = FastOutSlowInEasing)) { it } +
                        fadeOut(tween(260)),
            ) {
                PlayerDragSurface(onDismiss = { playerViewModel.onAction(PlayerAction.Collapse) }) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        NowPlayingScreen(playback, motion, playerViewModel::onAction)
                    }
                }
            }
        }
    }
}
