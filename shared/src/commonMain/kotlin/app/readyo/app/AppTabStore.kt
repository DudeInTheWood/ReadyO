package app.readyo.app

interface AppTabStore {
    fun read(): AppDestination

    fun save(destination: AppDestination)
}
