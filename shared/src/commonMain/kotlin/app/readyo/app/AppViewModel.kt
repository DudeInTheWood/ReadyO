package app.readyo.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val mutableDestination = MutableStateFlow(AppDestination.RadioHome)
    val destination = mutableDestination.asStateFlow()

    fun onAction(action: AppAction) {
        mutableDestination.value =
            when (action) {
                is AppAction.Navigate -> action.destination
                AppAction.Back -> AppDestination.RadioHome
            }
    }
}
