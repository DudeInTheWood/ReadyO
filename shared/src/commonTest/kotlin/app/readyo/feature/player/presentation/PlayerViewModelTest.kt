package app.readyo.feature.player.presentation

import androidx.lifecycle.ViewModelStore
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.*
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    private val store = ViewModelStore()

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun selectionStartsOneSourceAndOpeningClosingDoesNotChangePlayback() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = FakePlayerController()
        val viewModel = PlayerViewModel(player).also { store.put("player", it) }
        viewModel.onAction(PlayerAction.Select(listOf(testStation), 0))
        runCurrent()
        assertTrue(viewModel.uiState.value.isExpanded)
        assertEquals(testStation, viewModel.uiState.value.playback.item)
        viewModel.onAction(PlayerAction.Collapse)
        runCurrent()
        assertFalse(viewModel.uiState.value.isExpanded)
        assertEquals(PlaybackStatus.Playing, viewModel.uiState.value.playback.status)
        viewModel.onAction(PlayerAction.Expand)
        runCurrent()
        assertEquals(1, player.selections)
        assertTrue(viewModel.uiState.value.isExpanded)
    }

    @Test
    fun invalidSelectionAndEmptyPlayerCannotOpenOverlay() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = FakePlayerController()
        val viewModel = PlayerViewModel(player).also { store.put("player", it) }
        viewModel.onAction(PlayerAction.Select(emptyList(), 0))
        viewModel.onAction(PlayerAction.Expand)
        runCurrent()
        assertFalse(viewModel.uiState.value.isExpanded)
        assertEquals(0, player.selections)
    }

    @Test
    fun clearedNativeSessionDismissesPlayerAndNewSelectionStillWorks() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = FakePlayerController()
        val viewModel = PlayerViewModel(player).also { store.put("player", it) }
        viewModel.onAction(PlayerAction.Select(listOf(testStation), 0))
        runCurrent()
        assertTrue(viewModel.uiState.value.isExpanded)
        player.state.value = PlayerState()
        runCurrent()
        assertFalse(viewModel.uiState.value.isExpanded)
        assertEquals(null, viewModel.uiState.value.playback.item)
        viewModel.onAction(PlayerAction.Select(listOf(testStation), 0))
        runCurrent()
        assertTrue(viewModel.uiState.value.isExpanded)
        assertEquals(2, player.selections)
    }

    @Test
    fun transportActionsAreForwardedExactlyOnce() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val player = FakePlayerController()
        val viewModel = PlayerViewModel(player).also { store.put("player", it) }
        viewModel.onAction(PlayerAction.TogglePlayback)
        viewModel.onAction(PlayerAction.Seek(12_000))
        viewModel.onAction(PlayerAction.Next)
        viewModel.onAction(PlayerAction.Previous)
        assertEquals(listOf("toggle", "seek:12000", "next", "previous"), player.commands)
    }
}

private val testStation = AudioItem("station", "Station", "", "https://example.com/live", true)

private class FakePlayerController : PlayerController {
    override val state = MutableStateFlow(PlayerState())
    var selections = 0
    val commands = mutableListOf<String>()

    override fun select(items: List<AudioItem>, index: Int) {
        selections++
        state.value = PlayerState(items[index], PlaybackStatus.Playing)
    }

    override fun toggle() {
        commands += "toggle"
    }

    override fun seek(position: Long) {
        commands += "seek:$position"
    }

    override fun next() {
        commands += "next"
    }

    override fun previous() {
        commands += "previous"
    }
}
