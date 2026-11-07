package app.readyo.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Shared semantic colors. Cassette hardware materials live in their own palette. */
@Immutable
data class ReadyOColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHover: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val danger: Color,
    val border: Color,
    val onAccent: Color,
    val labelSurface: Color,
    val labelText: Color,
) {
    companion object {
        val ArchiveDark =
            ReadyOColors(
                background = Color(0xFF0E0F10),
                surface = Color(0xFF1A1C1D),
                surfaceRaised = Color(0xFF252729),
                surfaceHover = Color(0xFF303335),
                textPrimary = Color(0xFFF0E7D7),
                textSecondary = Color(0xFFB8AA96),
                textMuted = Color(0xFF8E867A),
                accentPrimary = Color(0xFFD57B49),
                accentSecondary = Color(0xFF9CAA88),
                danger = Color(0xFFDE816C),
                border = Color(0xFF434541),
                onAccent = Color(0xFF1C1510),
                labelSurface = Color(0xFFDDC8AB),
                labelText = Color(0xFF30251C),
            )

        val PaperLight =
            ReadyOColors(
                background = Color(0xFFEFE7D6),
                surface = Color(0xFFF8F1E3),
                surfaceRaised = Color(0xFFE5D9C3),
                surfaceHover = Color(0xFFE9DEC9),
                textPrimary = Color(0xFF332E27),
                textSecondary = Color(0xFF706653),
                textMuted = Color(0xFF938671),
                accentPrimary = Color(0xFFB75035),
                accentSecondary = Color(0xFF6C785D),
                danger = Color(0xFFBB523C),
                border = Color(0xFFC5B79E),
                onAccent = Color(0xFFFFF6E9),
                labelSurface = Color(0xFFFDF8E9),
                labelText = Color(0xFF352D25),
            )
    }
}

val LocalReadyOColors = staticCompositionLocalOf { ReadyOColors.ArchiveDark }
