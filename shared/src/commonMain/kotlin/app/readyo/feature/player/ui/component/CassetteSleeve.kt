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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.theme.ReadyOCassettePalette
import app.readyo.core.model.AudioItem
import app.readyo.resources.Res
import app.readyo.resources.cassette_still
import app.readyo.resources.music_note
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource

/** The reverse label spans the paper and the translucent reel window. */
@Composable
internal fun CassetteSleeve(item: AudioItem?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val paper = imageResource(Res.drawable.cassette_still)
    BoxWithConstraints(modifier) {
        val unit = maxWidth / 510f
        val printScale = unit.value / LocalDensity.current.fontScale
        // Follow the bevel at the paper's top corners and the curve of the reel window.
        val sleeveShape = remember {
            GenericShape { size, _ ->
                val bevel = size.width * 0.04f
                val radius = size.width * 0.025f
                moveTo(bevel, 0f)
                lineTo(size.width - bevel, 0f)
                lineTo(size.width, bevel)
                lineTo(size.width, size.height - radius)
                quadraticTo(size.width, size.height, size.width - radius, size.height)
                lineTo(radius, size.height)
                quadraticTo(0f, size.height, 0f, size.height - radius)
                lineTo(0f, bevel)
                close()
            }
        }
        AnimatedContent(
            targetState = item,
            contentKey = { it?.uri },
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
            label = "Track label",
            modifier =
                Modifier.offset(x = unit * 37f, y = unit * 55f)
                    .width(unit * 436f)
                    .height(unit * 199f)
                    .clip(sleeveShape)
                    .border(0.5.dp, colors.outline.copy(alpha = 0.45f), sleeveShape),
        ) { track ->
            val cover = rememberTrackCover(track)
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier.weight(1f).fillMaxSize().background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (cover != null) {
                        Image(cover, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(
                            painterResource(Res.drawable.music_note),
                            null,
                            Modifier.size(unit * 56f),
                            tint = colors.primary,
                        )
                    }
                }
                Column(Modifier.weight(1f).fillMaxSize()) {
                    Row(
                        Modifier.width(unit * 218f)
                            .height(unit * 74f)
                            .background(androidx.compose.ui.graphics.Color(0xFFE2C29F))
                            .drawBehind {
                                drawImage(
                                    paper,
                                    IntOffset(114, 59),
                                    IntSize(30, 36),
                                    dstSize =
                                        IntSize(size.width.roundToInt(), size.height.roundToInt()),
                                    filterQuality = FilterQuality.High,
                                )
                            }
                            .padding(horizontal = unit * 12f),
                        horizontalArrangement = Arrangement.spacedBy(unit * 10f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "B",
                            color = ReadyOCassettePalette.labelInk,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = (21f * printScale).sp,
                        )
                        Text(
                            track?.title.orEmpty(),
                            color = ReadyOCassettePalette.labelInk,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = (16f * printScale).sp,
                            lineHeight = (19f * printScale).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        Modifier.fillMaxSize()
                            .background(colors.surface.copy(alpha = 0.8f))
                            .padding(horizontal = unit * 14f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            track?.subtitle.orEmpty(),
                            color = colors.onSurface,
                            fontSize = (13f * printScale).sp,
                            lineHeight = (16f * printScale).sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable internal expect fun rememberTrackCover(item: AudioItem?): ImageBitmap?
