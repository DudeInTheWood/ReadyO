package app.readyo.app

import kotlin.test.*

class AppViewModelTest {
    @Test
    fun savesPrimaryTabAndRestoresItOnNextLaunch() {
        var saved = AppDestination.RadioHome
        val store =
            object : AppTabStore {
                override fun read() = saved

                override fun save(destination: AppDestination) {
                    saved = destination
                }
            }
        val first = AppViewModel(store)
        first.onAction(AppAction.Navigate(AppDestination.MusicLibrary))
        assertEquals(AppDestination.MusicLibrary, AppViewModel(store).destination.value)
        first.onAction(AppAction.Navigate(AppDestination.RadioSearch))
        assertEquals(AppDestination.RadioHome, AppViewModel(store).destination.value)
    }
}
