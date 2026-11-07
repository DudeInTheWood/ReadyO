package app.readyo.feature.music.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlaybackStatus
import app.readyo.core.playback.PlayerState

@Composable
internal fun CatalogSleeve(count: Int) {
    val paper = Color(0xffddc8ab)
    val ink = Color(0xff30251c)
    Row(
        Modifier.fillMaxWidth().background(paper, RoundedCornerShape(7.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "READYO / INDEX CARD",
                color = ink,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
            )
            Text(
                "Songs, side B.",
                color = ink,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 25.sp,
            )
            Text(
                songCount(count).uppercase(),
                color = ink,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
            )
        }
        Box(
            Modifier.sizeIn(minWidth = 52.dp, minHeight = 52.dp)
                .border(1.dp, ink.copy(alpha = .6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("B / 90", color = ink, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun TrackRow(item: AudioItem, index: Int, playback: PlayerState?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val active = playback != null
    val playing = playback?.status == PlaybackStatus.Playing
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth()
                .background(if (active) colors.surfaceContainerHigh else Color.Transparent)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Spacer(
                Modifier.width(3.dp)
                    .fillMaxHeight()
                    .background(if (active) colors.primary else Color.Transparent)
            )
            Text(
                (index + 1).toString().padStart(2, '0'),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = if (active) colors.primary else colors.onSurfaceVariant,
            )
            RecordSleeve(item.id)
            Column(Modifier.weight(1f).padding(vertical = 12.dp, horizontal = 2.dp)) {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (playing) "● NOW PLAYING"
                    else playback?.status?.name?.uppercase() ?: "LOCAL",
                    color = colors.secondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                )
                if (playback != null && playback.duration > 0) {
                    LinearProgressIndicator(
                        progress = {
                            (playback.position.toFloat() / playback.duration).coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun RecordSleeve(id: String) {
    val papers = listOf(Color(0xff816c54), Color(0xff515f62), Color(0xff695567), Color(0xff58634d))
    Canvas(
        Modifier.size(49.dp, 55.dp)
            .clipToBounds()
            .background(papers[id.hashCode().mod(papers.size)])
    ) {
        val ink = Color(0xffddc4a1)
        val radius = size.width * .5f
        drawCircle(
            ink.copy(alpha = .65f),
            radius,
            Offset(0f, size.height * .4f),
            style = Stroke(7.dp.toPx()),
        )
        val center = Offset(size.width * .84f, size.height * .27f)
        drawCircle(Color(0xff252625), radius, center)
        for (ring in 2..6) drawCircle(
            ink.copy(alpha = .2f),
            radius * ring / 7,
            center,
            style = Stroke(.6.dp.toPx()),
        )
        drawCircle(ink, radius * .18f, center)
    }
}
