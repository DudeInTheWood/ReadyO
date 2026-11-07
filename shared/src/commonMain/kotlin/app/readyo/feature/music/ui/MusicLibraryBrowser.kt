package app.readyo.feature.music.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.feature.music.domain.MusicFolderCollection
import app.readyo.feature.music.presentation.MusicLibraryScope

@Composable
internal fun MusicScopeSelector(scope: MusicLibraryScope, onSelect: (MusicLibraryScope) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MusicLibraryScope.entries.forEach { option ->
            val active = scope == option
            Surface(
                onClick = { onSelect(option) },
                modifier =
                    Modifier.weight(1f).semantics {
                        selected = active
                        role = Role.Tab
                    },
                shape = RoundedCornerShape(8.dp),
                color = if (active) colors.primaryContainer else colors.surface,
                contentColor = if (active) colors.primary else colors.onSurfaceVariant,
                border = BorderStroke(1.dp, if (active) colors.primary else colors.outlineVariant),
            ) {
                Text(
                    if (option == MusicLibraryScope.AllSongs) "All songs" else "Folders",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
internal fun MusicFolderRow(folder: MusicFolderCollection, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Canvas(Modifier.size(30.dp)) {
                val folderPath =
                    Path().apply {
                        moveTo(size.width * 0.08f, size.height * 0.26f)
                        lineTo(size.width * 0.38f, size.height * 0.26f)
                        lineTo(size.width * 0.5f, size.height * 0.38f)
                        lineTo(size.width * 0.92f, size.height * 0.38f)
                        lineTo(size.width * 0.92f, size.height * 0.82f)
                        lineTo(size.width * 0.08f, size.height * 0.82f)
                        close()
                    }
                drawPath(folderPath, colors.primary, style = Stroke(1.5.dp.toPx()))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    folder.directory.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    folder.directory.path,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    songCount(folder.trackIds.size),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                )
            }
            Text("›", fontSize = 24.sp, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
internal fun MusicFolderHeading(folder: MusicFolderCollection, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TextButton(onClick = onBack) { Text("‹ All folders") }
        Text(
            folder.directory.name,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(folder.directory.path, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun songCount(count: Int): String = "$count ${if (count == 1) "song" else "songs"}"
