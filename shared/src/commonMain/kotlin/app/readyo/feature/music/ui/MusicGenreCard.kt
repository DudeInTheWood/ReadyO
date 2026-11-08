package app.readyo.feature.music.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.feature.music.domain.MusicGenre
import app.readyo.feature.music.presentation.MusicLibraryScope

@Composable
internal fun LibraryScopeIcon(scope: MusicLibraryScope, modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier.size(22.dp)) {
        val stroke = Stroke(1.4.dp.toPx())
        when (scope) {
            MusicLibraryScope.AllSongs -> {
                for (row in 0..2) {
                    val y = size.height * (.25f + row * .25f)
                    drawCircle(color, 1.2.dp.toPx(), Offset(size.width * .15f, y))
                    drawLine(
                        color,
                        Offset(size.width * .35f, y),
                        Offset(size.width * .9f, y),
                        1.4.dp.toPx(),
                    )
                }
            }
            MusicLibraryScope.Folders -> {
                val path =
                    Path().apply {
                        moveTo(size.width * .08f, size.height * .24f)
                        lineTo(size.width * .4f, size.height * .24f)
                        lineTo(size.width * .5f, size.height * .36f)
                        lineTo(size.width * .92f, size.height * .36f)
                        lineTo(size.width * .92f, size.height * .82f)
                        lineTo(size.width * .08f, size.height * .82f)
                        close()
                    }
                drawPath(path, color, style = stroke)
            }
            MusicLibraryScope.Genres -> {
                drawCircle(color, size.minDimension * .4f, style = stroke)
                drawCircle(color, size.minDimension * .24f, style = stroke)
                drawCircle(color, size.minDimension * .065f)
            }
        }
    }
}

@Composable
internal fun MusicGenreCard(genre: MusicGenre, modifier: Modifier = Modifier, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        modifier = modifier,
        color = colors.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.secondaryContainer,
                contentColor = colors.secondary,
            ) {
                LibraryScopeIcon(MusicLibraryScope.Genres, Modifier.padding(10.dp))
            }
            Text(
                genre.name,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(songCount(genre.trackIds.size), fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
    }
}
