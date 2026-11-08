package app.readyo.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(private val tabStore: AppTabStore? = null) : ViewModel() {
    private val mutableDestination = MutableStateFlow(tabStore?.read() ?: AppDestination.RadioHome)
    val destination = mutableDestination.asStateFlow()

    fun onAction(action: AppAction) {
        mutableDestination.value =
            when (action) {
                is AppAction.Navigate -> action.destination
                AppAction.Back -> AppDestination.RadioHome
            }
        tabStore?.save(
            if (mutableDestination.value == AppDestination.MusicLibrary) AppDestination.MusicLibrary
            else AppDestination.RadioHome
        )
    }
}
