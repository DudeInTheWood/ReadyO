package app.readyo.app

enum class AppDestination(val route: String) {
    RadioHome("radio/home"),
    RadioSearch("radio/search"),
    MusicLibrary("music/library"),
}

sealed interface AppAction {
    data class Navigate(val destination: AppDestination) : AppAction

    data object Back : AppAction
}
