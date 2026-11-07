package app.readyo.app

import app.readyo.core.playback.PlayerController
import app.readyo.feature.music.domain.MusicLibraryRepository
import app.readyo.feature.radio.domain.RadioRepository

/** Platform hosts own these long-lived resources; screens receive only state and actions. */
data class AppDependencies(
    val player: PlayerController,
    val radioRepository: RadioRepository,
    val musicRepository: MusicLibraryRepository,
)
