package app.readyo.feature.music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

private fun MusicLibrarySnapshot.toUiState(
    repository: MusicLibraryRepository,
    selection: LibrarySelection,
): MusicLibraryUiState {
    val selectedFolder = collections.find { it.directory.id == selection.folderId }
    val visibleTracks =
        when (selection.scope) {
            MusicLibraryScope.AllSongs -> tracks
            MusicLibraryScope.Folders -> {
                val trackIds = selectedFolder?.trackIds.orEmpty()
                tracks.filter { it.id in trackIds }
            }
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
        collections = collections,
        selectedFolder = selectedFolder,
        visibleTracks = visibleTracks,
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
        onAction(MusicLibraryAction.Refresh)
    }

    fun onAction(action: MusicLibraryAction) {
        when (action) {
            is MusicLibraryAction.ChangeScope -> selection.value = LibrarySelection(action.scope)
            is MusicLibraryAction.OpenFolder ->
                selection.value = LibrarySelection(MusicLibraryScope.Folders, action.id)
            MusicLibraryAction.BackToFolders ->
                selection.value = LibrarySelection(MusicLibraryScope.Folders)
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
