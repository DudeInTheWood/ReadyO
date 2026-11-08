package app.readyo.feature.music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.readyo.feature.music.domain.MusicDirectory
import app.readyo.feature.music.domain.MusicFolderCollection
import app.readyo.feature.music.domain.MusicLibraryRepository
import app.readyo.feature.music.domain.MusicLibrarySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class LibrarySelection(
    val scope: MusicLibraryScope = MusicLibraryScope.AllSongs,
    val folderId: String? = null,
    val genreId: String? = null,
)

private fun MusicLibrarySnapshot.toUiState(
    repository: MusicLibraryRepository,
    selection: LibrarySelection,
): MusicLibraryUiState {
    val nodes = collections.associateByTo(mutableMapOf()) { it.directory.id }
    for (folder in collections) {
        val ancestors = folder.directory.ancestors
        ancestors.forEachIndexed { index, crumb ->
            nodes.getOrPut(crumb.id) {
                val path = ancestors.take(index + 1).joinToString(" / ") { it.name }
                MusicFolderCollection(
                    MusicDirectory(crumb.id, crumb.name, path, ancestors.take(index)),
                    emptySet(),
                )
            }
        }
    }
    val selectedFolder = nodes[selection.folderId]
    val children =
        nodes.values
            .filter { it.directory.ancestors.lastOrNull()?.id == selectedFolder?.directory?.id }
            .sortedWith(
                compareBy<MusicFolderCollection> { it.directory.name.lowercase() }
                    .thenBy { it.directory.id }
            )
    val genres = app.readyo.feature.music.domain.buildMusicGenres(tracks)
    val selectedGenre = genres.find { it.id == selection.genreId }
    val visibleTracks =
        when (selection.scope) {
            MusicLibraryScope.AllSongs -> tracks
            MusicLibraryScope.Folders -> {
                val trackIds = selectedFolder?.trackIds.orEmpty()
                tracks.filter { it.id in trackIds }
            }
            MusicLibraryScope.Genres -> tracks.filter { it.id in selectedGenre?.trackIds.orEmpty() }
        }
    return MusicLibraryUiState(
        tracks = tracks,
        isLoading = isLoading,
        isImporting = isImporting,
        error = error,
        folders = folders,
        isScanning = isScanning,
        deviceScanEnabled = deviceScanEnabled,
        canScanDevice = repository.supportsDeviceScan,
        canLinkFolders = repository.supportsFolderLinking,
        scope = selection.scope,
        collections = children,
        selectedFolder = selectedFolder,
        visibleTracks = visibleTracks,
        genres = genres,
        selectedGenre = selectedGenre,
    )
}

class MusicLibraryViewModel(private val repository: MusicLibraryRepository) : ViewModel() {
    private val selection = MutableStateFlow(LibrarySelection())
    val uiState =
        combine(repository.library, selection) { library, selected ->
                library.toUiState(repository, selected)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                repository.library.value.toUiState(repository, selection.value),
            )

    init {
        viewModelScope.launch { repository.load() }
    }

    fun onAction(action: MusicLibraryAction) {
        when (action) {
            is MusicLibraryAction.OpenGenre ->
                selection.value = LibrarySelection(MusicLibraryScope.Genres, genreId = action.id)
            is MusicLibraryAction.ChangeScope -> selection.value = LibrarySelection(action.scope)
            is MusicLibraryAction.OpenFolder ->
                selection.value = LibrarySelection(MusicLibraryScope.Folders, action.id)
            MusicLibraryAction.BackToFolders ->
                selection.value =
                    LibrarySelection(
                        MusicLibraryScope.Folders,
                        uiState.value.selectedFolder?.directory?.ancestors?.lastOrNull()?.id,
                    )
            MusicLibraryAction.Refresh -> viewModelScope.launch { repository.refresh() }
            is MusicLibraryAction.ImportFiles ->
                viewModelScope.launch { repository.importAudio(action.sources) }
            is MusicLibraryAction.AddFolder ->
                viewModelScope.launch { repository.addFolder(action.source) }
            is MusicLibraryAction.RemoveFolder ->
                viewModelScope.launch { repository.removeFolder(action.id) }
            MusicLibraryAction.ScanDevice -> viewModelScope.launch { repository.scanDevice() }
        }
    }
}
