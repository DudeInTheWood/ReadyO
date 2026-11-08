package app.readyo.feature.music.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.component.MicroLabel
import app.readyo.feature.music.domain.DailyMixState

@Composable
internal fun DailyMixCard(state: DailyMixState, onPlay: () -> Unit, onRefresh: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .55f)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MicroLabel("MADE FROM YOUR MUSIC")
            Text("Daily Mix", fontFamily = FontFamily.Serif, fontSize = 25.sp)
            Text(
                state.tracks.take(3).joinToString(" · ") { it.title },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MicroLabel(songCount(state.tracks.size).uppercase())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlay, enabled = !state.isRefreshing) { Text("Play mix") }
                TextButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                    Text("Refresh mix")
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
