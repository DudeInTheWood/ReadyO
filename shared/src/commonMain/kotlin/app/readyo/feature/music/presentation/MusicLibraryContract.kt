package app.readyo.feature.music.presentation

import app.readyo.core.model.AudioItem
import app.readyo.feature.music.domain.MusicFolder
import app.readyo.feature.music.domain.MusicFolderCollection

enum class MusicLibraryScope {
    AllSongs,
    Folders,
}

data class MusicLibraryUiState(
    val tracks: List<AudioItem> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val error: String? = null,
    val folders: List<MusicFolder> = emptyList(),
    val isScanning: Boolean = false,
    val deviceScanEnabled: Boolean = false,
    val canScanDevice: Boolean = false,
    val canLinkFolders: Boolean = false,
    val scope: MusicLibraryScope = MusicLibraryScope.AllSongs,
    val collections: List<MusicFolderCollection> = emptyList(),
    val selectedFolder: MusicFolderCollection? = null,
    val visibleTracks: List<AudioItem> = emptyList(),
)

sealed interface MusicLibraryAction {
    data class ChangeScope(val scope: MusicLibraryScope) : MusicLibraryAction

    data class OpenFolder(val id: String) : MusicLibraryAction

    data object BackToFolders : MusicLibraryAction

    data object Refresh : MusicLibraryAction

    data class ImportFiles(val sources: List<String>) : MusicLibraryAction

    data class AddFolder(val source: String) : MusicLibraryAction

    data class RemoveFolder(val id: String) : MusicLibraryAction

    data object ScanDevice : MusicLibraryAction
}
