package app.readyo.feature.music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.readyo.feature.music.domain.DailyMixRepository
import app.readyo.feature.music.domain.DailyMixState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DailyMixViewModel(private val repository: DailyMixRepository?) : ViewModel() {
    val state = repository?.state ?: MutableStateFlow(DailyMixState())

    fun refresh() {
        viewModelScope.launch { repository?.refresh() }
    }
}
