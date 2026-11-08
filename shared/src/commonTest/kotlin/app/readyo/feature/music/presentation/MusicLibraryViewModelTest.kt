package app.readyo.feature.music.presentation

import androidx.lifecycle.ViewModelStore
import app.readyo.core.model.AudioItem
import app.readyo.feature.music.domain.MusicDirectory
import app.readyo.feature.music.domain.MusicFolderCollection
import app.readyo.feature.music.domain.MusicLibraryRepository
import app.readyo.feature.music.domain.MusicLibrarySnapshot
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MusicLibraryViewModelTest {
    private val store = ViewModelStore()

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun folderWithOnlyDescendantsAppearsAndBackRestoresItsChildren() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val parent = app.readyo.feature.music.domain.FolderCrumb("parent", "Music")
        val albums = MusicDirectory("child", "Albums", "Music/Albums", listOf(parent))
        val child = MusicFolderCollection(albums, setOf(storedTrack.id))
        val repository =
            FakeMusicRepository(
                MusicLibrarySnapshot(tracks = listOf(storedTrack), collections = listOf(child))
            )
        val vm = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        vm.onAction(MusicLibraryAction.ChangeScope(MusicLibraryScope.Folders))
        runCurrent()
        assertEquals(listOf(parent.id), vm.uiState.value.collections.map { it.directory.id })
        vm.onAction(MusicLibraryAction.OpenFolder(parent.id))
        runCurrent()
        assertEquals(listOf(child), vm.uiState.value.collections)
        assertTrue(vm.uiState.value.visibleTracks.isEmpty())
        vm.onAction(MusicLibraryAction.OpenFolder(albums.id))
        runCurrent()
        assertEquals(listOf(storedTrack), vm.uiState.value.visibleTracks)
        vm.onAction(MusicLibraryAction.BackToFolders)
        runCurrent()
        assertEquals(parent.id, vm.uiState.value.selectedFolder?.directory?.id)
    }

    @Test
    fun refreshPublishesStoredTracksWithoutAutoPlaying() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeMusicRepository()
        val viewModel = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        runCurrent()
        assertEquals(listOf(storedTrack), viewModel.uiState.value.tracks)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun pendingImportKeepsExistingTracksVisibleAndPublishesRecoveryError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeMusicRepository()
        val viewModel = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        runCurrent()
        viewModel.onAction(MusicLibraryAction.ImportFiles(listOf("content://audio/one")))
        runCurrent()
        assertTrue(viewModel.uiState.value.isImporting)
        assertEquals(listOf(storedTrack), viewModel.uiState.value.tracks)
        repository.importResult.complete(Unit)
        runCurrent()
        assertFalse(viewModel.uiState.value.isImporting)
        assertEquals("Unsupported file", viewModel.uiState.value.error)
        assertEquals(listOf("content://audio/one"), repository.importedSources)
    }

    @Test
    fun folderScopeRestrictsThePlayableListAndAllSongsRestoresIt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val other = storedTrack.copy(id = "other", title = "Other track")
        val folder =
            MusicFolderCollection(MusicDirectory("folder", "Music", "Music"), setOf(storedTrack.id))
        val repository =
            FakeMusicRepository(
                MusicLibrarySnapshot(
                    tracks = listOf(storedTrack, other),
                    collections = listOf(folder),
                )
            )
        val viewModel = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        runCurrent()

        viewModel.onAction(MusicLibraryAction.ChangeScope(MusicLibraryScope.Folders))
        runCurrent()
        assertEquals(emptyList(), viewModel.uiState.value.visibleTracks)
        viewModel.onAction(MusicLibraryAction.OpenFolder("folder"))
        runCurrent()
        assertEquals(listOf(storedTrack), viewModel.uiState.value.visibleTracks)
        assertEquals(folder, viewModel.uiState.value.selectedFolder)
        assertEquals(listOf(storedTrack, other), viewModel.uiState.value.tracks)

        viewModel.onAction(MusicLibraryAction.ChangeScope(MusicLibraryScope.AllSongs))
        runCurrent()
        assertEquals(listOf(storedTrack, other), viewModel.uiState.value.visibleTracks)
        assertEquals(null, viewModel.uiState.value.selectedFolder)
    }

    @Test
    fun folderContentsFollowRescansAndRemovedFoldersReturnToTheFolderList() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val directory = MusicDirectory("folder", "Music", "Music")
        val folder = MusicFolderCollection(directory, setOf(storedTrack.id))
        val repository =
            FakeMusicRepository(
                MusicLibrarySnapshot(tracks = listOf(storedTrack), collections = listOf(folder))
            )
        val viewModel = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        runCurrent()
        viewModel.onAction(MusicLibraryAction.OpenFolder(directory.id))
        runCurrent()
        val replacement = storedTrack.copy(id = "new", title = "New song")
        repository.library.value =
            repository.library.value.copy(
                tracks = listOf(replacement),
                collections = listOf(folder.copy(trackIds = setOf(replacement.id))),
            )
        runCurrent()
        assertEquals(listOf(replacement), viewModel.uiState.value.visibleTracks)
        repository.library.value =
            repository.library.value.copy(tracks = emptyList(), collections = emptyList())
        runCurrent()
        assertEquals(MusicLibraryScope.Folders, viewModel.uiState.value.scope)
        assertEquals(null, viewModel.uiState.value.selectedFolder)
        assertEquals(emptyList(), viewModel.uiState.value.visibleTracks)
    }

    @Test
    fun backFromFolderReturnsToFoldersWithoutChangingTheLibrary() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val folder =
            MusicFolderCollection(MusicDirectory("folder", "Music", "Music"), setOf(storedTrack.id))
        val repository =
            FakeMusicRepository(
                MusicLibrarySnapshot(tracks = listOf(storedTrack), collections = listOf(folder))
            )
        val viewModel = MusicLibraryViewModel(repository).also { store.put("music", it) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        runCurrent()
        viewModel.onAction(MusicLibraryAction.OpenFolder("folder"))
        runCurrent()
        viewModel.onAction(MusicLibraryAction.BackToFolders)
        runCurrent()
        assertEquals(MusicLibraryScope.Folders, viewModel.uiState.value.scope)
        assertEquals(null, viewModel.uiState.value.selectedFolder)
        assertEquals(listOf(folder), viewModel.uiState.value.collections)
        assertEquals(listOf(storedTrack), viewModel.uiState.value.tracks)
    }
}

private val storedTrack =
    AudioItem("local", "Stored track", "Artist", "file:///audio/track.mp3", false)

private class FakeMusicRepository(
    initial: MusicLibrarySnapshot = MusicLibrarySnapshot(tracks = listOf(storedTrack))
) : MusicLibraryRepository {
    override val library = MutableStateFlow(initial)
    val importResult = CompletableDeferred<Unit>()
    var importedSources = emptyList<String>()

    override suspend fun addFolder(source: String) {}

    override suspend fun removeFolder(id: String) {}

    override suspend fun scanDevice() {}

    override suspend fun refresh() {
        library.value = library.value.copy(isLoading = false)
    }

    override suspend fun importAudio(sources: List<String>) {
        importedSources = sources
        library.value = library.value.copy(isImporting = true)
        importResult.await()
        library.value = library.value.copy(isImporting = false, error = "Unsupported file")
    }
}
