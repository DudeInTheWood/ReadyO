package app.readyo.feature.radio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.component.SectionHeader
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlaybackStatus
import app.readyo.feature.radio.presentation.RadioFilter

@Composable
fun RadioHomeScreen(
    last: AudioItem?,
    status: PlaybackStatus,
    onResume: (AudioItem) -> Unit,
    onExplore: (RadioFilter) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (last != null) {
            Surface(
                onClick = { onResume(last) },
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    IndexLabel("TUNED STATION", MaterialTheme.colorScheme.secondary)
                    Text(last.title, fontFamily = FontFamily.Serif, fontSize = 22.sp)
                    Text(
                        if (status == PlaybackStatus.Playing) "Pause station"
                        else "Listen to station",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        SectionHeader("Browse the airwaves")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                    listOf("Thailand" to RadioFilter.Thailand, "Worldwide" to RadioFilter.All),
                    listOf("Pop & Hits" to RadioFilter.Pop, "Late-night Jazz" to RadioFilter.Jazz),
                )
                .forEachIndexed { row, entries ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        entries.forEachIndexed { col, entry ->
                            StationCategoryCard(
                                entry.first,
                                "BAND / 0${row*2+col+1}",
                                Modifier.weight(1f),
                            ) {
                                onExplore(entry.second)
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun StationCategoryCard(
    title: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.padding(12.dp).heightIn(min = 42.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            IndexLabel(label, MaterialTheme.colorScheme.secondary)
            Text(
                title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
    }
}
