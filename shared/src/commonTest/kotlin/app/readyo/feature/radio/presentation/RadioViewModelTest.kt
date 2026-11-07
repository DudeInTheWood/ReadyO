package app.readyo.feature.radio.presentation

import androidx.lifecycle.ViewModelStore
import app.readyo.core.model.AudioItem
import app.readyo.feature.radio.domain.RadioRepository
import kotlin.test.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RadioViewModelTest {
    private val store = ViewModelStore()

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun typingDebouncesAndOnlyLatestQueryIsRequested() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeRadioRepository()
        val viewModel = RadioViewModel(repository).also { store.put("radio", it) }
        runCurrent()

        viewModel.onAction(RadioAction.SearchChanged("ja"))
        advanceTimeBy(200)
        viewModel.onAction(RadioAction.SearchChanged("jazz"))
        advanceTimeBy(349)
        runCurrent()
        assertEquals(listOf(""), repository.queries)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("", "jazz"), repository.queries)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun obsoleteRequestCannotClearTheNewLoadingState() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeRadioRepository().apply { pending = CompletableDeferred() }
        val viewModel = RadioViewModel(repository).also { store.put("radio", it) }
        runCurrent()
        viewModel.onAction(RadioAction.SearchChanged("new"))
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        repository.pending?.complete(emptyList())
        advanceUntilIdle()
        assertEquals("new", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun directoryFailureIsRecoverableAndFilterReachesRepository() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeRadioRepository().apply { fail = true }
        val viewModel = RadioViewModel(repository).also { store.put("radio", it) }
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        repository.fail = false
        viewModel.onAction(RadioAction.FilterChanged(RadioFilter.Thailand))
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)
        assertEquals("Thailand", repository.lastCountry)
    }
}

private class FakeRadioRepository : RadioRepository {
    val queries = mutableListOf<String>()
    var lastCountry = ""
    var fail = false
    var pending: CompletableDeferred<List<AudioItem>>? = null

    override suspend fun stations(
        query: String,
        tag: String,
        country: String,
        language: String,
    ): List<AudioItem> {
        queries += query
        lastCountry = country
        if (fail) error("Directory offline")
        return pending?.await() ?: emptyList()
    }
}
