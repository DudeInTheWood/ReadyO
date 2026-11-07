package app.readyo.feature.player.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.theme.ReadyOCassettePalette
import app.readyo.resources.Res
import app.readyo.resources.cassette_still
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.imageResource

/** Decorative duplicate; the full, scalable title below the cassette remains accessible. */
@Composable
internal fun CassetteTitleLabel(title: String, modifier: Modifier = Modifier) {
    val paper = imageResource(Res.drawable.cassette_still)
    val durationScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]
    val canAnimate =
        LocalWindowInfo.current.isWindowFocused && (durationScale?.scaleFactor ?: 1f) > 0f

    BoxWithConstraints(
        modifier.clipToBounds().clearAndSetSemantics {},
        contentAlignment = Alignment.CenterStart,
    ) {
        val fontScale = LocalDensity.current.fontScale
        val printSize = (maxHeight.value * 0.58f / fontScale).sp
        // Cover the original baked title with blank paper sampled from the same cassette asset.
        // The Lottie JSON and its rotating hubs remain unchanged.
        Canvas(
            Modifier.fillMaxSize().graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
        ) {
            drawImage(
                image = paper,
                srcOffset = IntOffset(114, 59),
                srcSize = IntSize(30, 54),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                filterQuality = FilterQuality.High,
            )
            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.025f to Color.White,
                    0.975f to Color.White,
                    1f to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
            drawRect(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.08f to Color.White,
                    0.92f to Color.White,
                    1f to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            key(title) {
                Text(
                    text = title,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .basicMarquee(
                                iterations = if (canAnimate) Int.MAX_VALUE else 0,
                                initialDelayMillis = 1400,
                                repeatDelayMillis = 1800,
                                spacing = MarqueeSpacing(28.dp),
                                velocity = 24.dp,
                            ),
                    color = ReadyOCassettePalette.labelInk,
                    fontFamily = FontFamily.Cursive,
                    fontSize = printSize,
                    lineHeight = printSize * 1.2f,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    style =
                        androidx.compose.ui.text.TextStyle(
                            textDirection = TextDirection.ContentOrLtr
                        ),
                )
            }
        }
    }
}
