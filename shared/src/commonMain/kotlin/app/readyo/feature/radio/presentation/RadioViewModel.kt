package app.readyo.feature.radio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.readyo.core.model.AudioItem
import app.readyo.feature.radio.domain.RadioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RadioViewModel(
    private val repository: RadioRepository,
    history: Flow<List<AudioItem>> = flowOf(emptyList()),
) : ViewModel() {
    private val mutableState = MutableStateFlow(RadioUiState())
    val uiState = mutableState.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            history.collect { recent -> mutableState.update { it.copy(recentStations = recent) } }
        }
    }

    fun onAction(action: RadioAction) {
        when (action) {
            is RadioAction.OpenDirectory -> {
                mutableState.update { it.copy(query = "", filter = action.filter) }
                loadStations()
            }
            is RadioAction.SearchChanged -> {
                mutableState.update { it.copy(query = action.query) }
                loadStations(debounce = true)
            }
            is RadioAction.FilterChanged -> {
                mutableState.update { it.copy(filter = action.filter) }
                loadStations()
            }
            RadioAction.Retry -> loadStations()
        }
    }

    private fun loadStations(debounce: Boolean = false) {
        searchJob?.cancel()
        val request = mutableState.value
        mutableState.update { it.copy(isLoading = true, error = null, stations = emptyList()) }
        searchJob = viewModelScope.launch {
            if (debounce) delay(350)
            try {
                val stations =
                    repository.stations(
                        query = request.query,
                        tag =
                            when (request.filter) {
                                RadioFilter.Pop -> "pop"
                                RadioFilter.Jazz -> "jazz"
                                else -> ""
                            },
                        country = if (request.filter == RadioFilter.Thailand) "Thailand" else "",
                    )
                mutableState.update { it.copy(stations = stations, isLoading = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        error = "The station directory is unavailable. Please try again.",
                    )
                }
            }
        }
    }
}
