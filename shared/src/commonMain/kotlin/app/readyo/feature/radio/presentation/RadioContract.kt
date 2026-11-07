package app.readyo.feature.radio.presentation

import app.readyo.core.model.AudioItem

enum class RadioFilter(val label: String) {
    All("All"),
    Thailand("Thailand"),
    Pop("Pop"),
    Jazz("Jazz"),
}

data class RadioUiState(
    val query: String = "",
    val filter: RadioFilter = RadioFilter.All,
    val stations: List<AudioItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface RadioAction {
    data class SearchChanged(val query: String) : RadioAction

    data class FilterChanged(val filter: RadioFilter) : RadioAction

    data object Retry : RadioAction
}
