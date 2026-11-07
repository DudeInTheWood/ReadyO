package app.readyo.feature.player.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import app.readyo.feature.player.ui.motion.DeckMotion
import app.readyo.resources.Res
import app.readyo.resources.cassette_still
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.painterResource

private const val CassetteLoopSeconds = 8f
private const val CassetteAspectRatio = 510f / 362f

/** The supplied Lottie is bundled offline, including its embedded image assets. */
@Composable
fun CassetteReelAnimation(motion: DeckMotion, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/cassette_transport.json").decodeToString()
        )
    }
    val animatedPainter =
        rememberLottiePainter(
            composition = composition,
            progress = { (motion.seconds % CassetteLoopSeconds) / CassetteLoopSeconds },
        )
    Image(
        painter =
            if (composition == null) painterResource(Res.drawable.cassette_still)
            else animatedPainter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxWidth().aspectRatio(CassetteAspectRatio),
    )
}
