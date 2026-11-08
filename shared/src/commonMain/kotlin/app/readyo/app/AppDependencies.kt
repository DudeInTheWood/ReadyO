package app.readyo.app

import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlayerController
import app.readyo.feature.music.domain.MusicLibraryRepository
import app.readyo.feature.radio.domain.RadioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Platform hosts own these long-lived resources; screens receive only state and actions. */
data class AppDependencies(
    val player: PlayerController,
    val radioRepository: RadioRepository,
    val musicRepository: MusicLibraryRepository,
    val recentStations: Flow<List<AudioItem>> = flowOf(emptyList()),
    val dailyMix: app.readyo.feature.music.domain.DailyMixRepository? = null,
    val tabStore: AppTabStore? = null,
)
