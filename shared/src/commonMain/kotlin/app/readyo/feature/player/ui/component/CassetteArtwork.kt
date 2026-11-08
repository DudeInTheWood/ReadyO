package app.readyo.feature.player.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.ui.motion.DeckMotion
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun CassetteArtwork(
    state: PlayerState,
    motion: DeckMotion,
    angle: State<Float>,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showBack by remember(angle) { derivedStateOf { angle.value >= 90f } }
    DeckPanel(modifier) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().padding(12.dp).aspectRatio(510f / 362f).clickable(
                enabled = state.item != null,
                role = Role.Button,
                onClickLabel = if (showBack) "Turn to side A" else "Turn to side B",
            ) {
                onFlip()
            }
        ) {
            // A narrow chassis edge bridges the two faces when the tape is edge-on.
            Canvas(Modifier.fillMaxSize()) {
                val turn = abs(sin(angle.value * kotlin.math.PI.toFloat() / 180f))
                val thickness = 9.dp.toPx() * turn
                val top = size.height * 0.025f - 6.dp.toPx() * turn
                drawRoundRect(
                    brush =
                        Brush.horizontalGradient(
                            listOf(Color(0xFF171512), Color(0xFF8B7762), Color(0xFF25221E))
                        ),
                    topLeft = Offset((size.width - thickness) / 2f, top),
                    size = Size(thickness, size.height * 0.95f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                )
            }
            BoxWithConstraints(
                Modifier.fillMaxSize().graphicsLayer {
                    val lift = abs(sin(angle.value * kotlin.math.PI.toFloat() / 180f))
                    rotationY = angle.value
                    rotationX = lift * 6f
                    translationY = -6.dp.toPx() * lift
                    cameraDistance = 14f * density
                }
            ) {
                // Counter-rotate the back print so it faces outward at 180 degrees.
                BoxWithConstraints(
                    Modifier.fillMaxSize().graphicsLayer { rotationY = if (showBack) 180f else 0f }
                ) {
                    // Keep one retained Lottie on both sides, including the open reel window.
                    CassetteShell(motion)
                    if (showBack) {
                        CassetteSleeve(state.item, Modifier.fillMaxSize())
                    } else {
                        CassetteTitleLabel(
                            title = state.item?.title.orEmpty(),
                            modifier =
                                Modifier.offset(
                                        x = maxWidth * (145f / 510f),
                                        y = maxWidth * (59f / 510f),
                                    )
                                    .width(maxWidth * (240f / 510f))
                                    .height(maxWidth * (54f / 510f)),
                        )
                    }
                }
            }
        }
    }
}
