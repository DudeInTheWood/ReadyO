package app.readyo

import androidx.compose.ui.window.ComposeUIViewController
import app.readyo.app.AppDependencies
import app.readyo.app.ReadyOApp

/** The iOS host supplies native repositories and the Files picker bridge. */
fun MainViewController(
    dependencies: AppDependencies,
    onImportRequested: ((List<String>) -> Unit) -> Unit,
) = ComposeUIViewController {
    ReadyOApp(dependencies, onImportRequested)
}
