package app.readyo.feature.radio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlaybackStatus

internal fun presetNumber(index: Int) = (index + 1).toString().padStart(2, '0')

@Composable
internal fun StationRow(item: AudioItem, index: Int, status: PlaybackStatus?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val live = status == PlaybackStatus.Playing
    val accent = if (status != null) colors.primary else colors.secondary
    val plateColors =
        listOf(Color(0xff404a40), Color(0xff765443), Color(0xff46595c), Color(0xff645453))
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (status != null) colors.surfaceContainerHigh else colors.surface,
        border = BorderStroke(1.dp, if (status != null) accent else colors.outlineVariant),
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Spacer(Modifier.fillMaxHeight().width(3.dp).background(accent))
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IndexLabel("PRESET / " + presetNumber(index))
                    IndexLabel(if (live) "● LIVE" else "INDEXED", accent)
                }
                Row(
                    Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(52.dp)
                            .background(
                                plateColors[item.id.hashCode().mod(plateColors.size)],
                                RoundedCornerShape(3.dp),
                            )
                            .padding(5.dp)
                            .border(1.dp, Color.White.copy(alpha = .28f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            item.title.trim().take(1).uppercase(),
                            color = Color(0xfff0e7d7),
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 26.sp,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 19.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.subtitle,
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (live) colors.primary else colors.surfaceContainerHighest,
                    ) {
                        Text(
                            if (live) "Ⅱ" else "▶",
                            Modifier.padding(9.dp),
                            color = if (live) colors.onPrimary else colors.onSurface,
                        )
                    }
                }
                HorizontalDivider(color = colors.outlineVariant)
                Box(
                    Modifier.fillMaxWidth()
                        .background(colors.background)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    IndexLabel(
                        if (live) "● TRANSMITTING"
                        else status?.name?.uppercase() ?: "INTERNET RADIO"
                    )
                }
            }
        }
    }
}

@Composable
internal fun IndexLabel(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = .7.sp,
    )
}
