package app.readyo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import app.readyo.app.AndroidAppContainer
import app.readyo.app.ReadyOApp
import app.readyo.feature.music.data.DeviceMusicScanner

class MainActivity : ComponentActivity() {
    private val container: AndroidAppContainer by viewModels()
    private val audioPicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { sources ->
            container.completeImport(sources.map { it.toString() })
        }
    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { source ->
            container.completeFolderResult(source?.toString())
        }
    private val audioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            container.completeDeviceScanRequest()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            ReadyOApp(
                dependencies = container.dependencies,
                onImportRequested = { onResult ->
                    container.awaitImportResult(onResult)
                    audioPicker.launch(arrayOf("audio/*"))
                },
                onFolderRequested = { onResult ->
                    container.awaitFolderResult(onResult)
                    folderPicker.launch(null)
                },
                onDeviceScanRequested = { onResult ->
                    container.awaitDeviceScan(onResult)
                    if (DeviceMusicScanner(this).hasPermission)
                        container.completeDeviceScanRequest()
                    else audioPermission.launch(DeviceMusicScanner.audioPermission)
                },
                backHandler = { enabled, action -> BackHandler(enabled, action) },
            )
        }
    }
}
