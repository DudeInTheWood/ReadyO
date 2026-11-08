package app.readyo.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.readyo.core.playback.AndroidPlayer
import app.readyo.feature.music.data.AndroidDailyMixRepository
import app.readyo.feature.music.data.AndroidMusicLibraryRepository
import app.readyo.feature.radio.data.RadioBrowserRepository
import app.readyo.feature.radio.data.RecentStationStore

/** Activity-retained platform resources; the service remains the owner of actual audio playback. */
class AndroidAppContainer(application: Application) : AndroidViewModel(application) {
    private val player = AndroidPlayer(application)
    private val radioRepository = RadioBrowserRepository()
    private val musicRepository = AndroidMusicLibraryRepository(application)
    private val dailyMix = AndroidDailyMixRepository(application, musicRepository)
    val dependencies =
        AppDependencies(
            player,
            radioRepository,
            musicRepository,
            RecentStationStore(application).stations,
            dailyMix,
            AndroidAppTabStore(application),
        )

    private var importResultHandler: ((List<String>) -> Unit)? = null
    private var folderResultHandler: ((String?) -> Unit)? = null
    private var deviceScanHandler: (() -> Unit)? = null

    fun awaitFolderResult(handler: (String?) -> Unit) {
        folderResultHandler = handler
    }

    fun completeFolderResult(source: String?) {
        folderResultHandler?.invoke(source)
        folderResultHandler = null
    }

    fun awaitDeviceScan(handler: () -> Unit) {
        deviceScanHandler = handler
    }

    fun completeDeviceScanRequest() {
        deviceScanHandler?.invoke()
        deviceScanHandler = null
    }

    fun awaitImportResult(handler: (List<String>) -> Unit) {
        importResultHandler = handler
    }

    fun completeImport(sources: List<String>) {
        importResultHandler?.invoke(sources)
        importResultHandler = null
    }

    override fun onCleared() {
        dailyMix.close()
        player.close()
        radioRepository.close()
        importResultHandler = null
        folderResultHandler = null
        deviceScanHandler = null
    }
}
