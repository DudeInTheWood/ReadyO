package app.readyo.feature.player.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.model.AudioItem

/** Artwork and print share the smoked window, with the live reels visible underneath. */
@Composable
internal fun CassetteSleeve(item: AudioItem?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(modifier) {
        val unit = maxWidth / 510f
        val printScale = unit.value / LocalDensity.current.fontScale
        CassetteTitleLabel(
            title = "",
            modifier =
                Modifier.offset(x = unit * 145f, y = unit * 59f)
                    .width(unit * 240f)
                    .height(unit * 54f),
        )
        Box(
            Modifier.offset(x = unit * 69f, y = unit * 80f)
                .width(unit * 43f)
                .height(unit * 36f)
                .background(colors.surface, RoundedCornerShape(unit * 3f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "B",
                color = colors.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = (29f * printScale).sp,
            )
        }
        val windowShape = RoundedCornerShape(unit * 9f)
        AnimatedContent(
            targetState = item,
            contentKey = { it?.uri },
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
            label = "Track label",
            modifier =
                Modifier.offset(x = unit * 40f, y = unit * 129f)
                    .width(unit * 430f)
                    .height(unit * 124f)
                    .clip(windowShape)
                    .border(0.5.dp, colors.outline.copy(alpha = 0.45f), windowShape),
        ) { track ->
            val cover = rememberTrackCover(track)
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier.weight(1f)
                        .fillMaxSize()
                        .background(
                            colors.surface.copy(alpha = if (cover == null) 0.45f else 0.12f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (cover != null) {
                        Image(
                            cover,
                            null,
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.72f,
                        )
                    } else {
                        Text(
                            track?.title.orEmpty().trim().take(1).uppercase(),
                            fontFamily = FontFamily.Serif,
                            color = colors.primary.copy(alpha = 0.9f),
                            fontSize = (48f * printScale).sp,
                        )
                    }
                }
                Column(
                    Modifier.weight(1f)
                        .fillMaxSize()
                        .background(colors.surface.copy(alpha = 0.82f))
                        .padding(horizontal = unit * 14f, vertical = unit * 10f),
                    verticalArrangement =
                        Arrangement.spacedBy(unit * 7f, Alignment.CenterVertically),
                ) {
                    Text(
                        track?.title.orEmpty(),
                        color = colors.onSurface,
                        fontSize = (16f * printScale).sp,
                        lineHeight = (19f * printScale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!track?.subtitle.isNullOrBlank()) {
                        Text(
                            track.subtitle,
                            color = colors.onSurfaceVariant,
                            fontSize = (11f * printScale).sp,
                            lineHeight = (14f * printScale).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable internal expect fun rememberTrackCover(item: AudioItem?): ImageBitmap?
