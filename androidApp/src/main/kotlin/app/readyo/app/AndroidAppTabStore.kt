package app.readyo.app

import android.content.Context

internal class AndroidAppTabStore(context: Context) : AppTabStore {
    private val preferences = context.getSharedPreferences("navigation", Context.MODE_PRIVATE)

    override fun read() =
        if (preferences.getBoolean("music", false)) AppDestination.MusicLibrary
        else AppDestination.RadioHome

    override fun save(destination: AppDestination) {
        preferences.edit().putBoolean("music", destination == AppDestination.MusicLibrary).apply()
    }
}
