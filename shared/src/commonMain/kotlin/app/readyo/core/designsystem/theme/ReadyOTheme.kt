package app.readyo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/** Archive Dark is the app default, independent of the device appearance. */
@Composable
fun ReadyOTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colors = if (darkTheme) ReadyOColors.ArchiveDark else ReadyOColors.PaperLight
    val scheme =
        remember(colors, darkTheme) {
            val base = if (darkTheme) darkColorScheme() else lightColorScheme()
            base.copy(
                primary = colors.accentPrimary,
                onPrimary = colors.onAccent,
                primaryContainer =
                    colors.accentPrimary.copy(alpha = 0.15f).compositeOver(colors.surface),
                onPrimaryContainer = colors.accentPrimary,
                secondary = colors.accentSecondary,
                onSecondary = colors.background,
                secondaryContainer =
                    colors.accentSecondary.copy(alpha = 0.12f).compositeOver(colors.surface),
                onSecondaryContainer = colors.accentSecondary,
                tertiary = colors.labelSurface,
                onTertiary = colors.labelText,
                background = colors.background,
                onBackground = colors.textPrimary,
                surface = colors.surface,
                onSurface = colors.textPrimary,
                surfaceVariant = colors.surfaceRaised,
                onSurfaceVariant = colors.textSecondary,
                surfaceDim = colors.background,
                surfaceBright = colors.surfaceHover,
                surfaceContainerLowest = colors.background,
                surfaceContainerLow = colors.surface,
                surfaceContainer = colors.surface,
                surfaceContainerHigh = colors.surfaceRaised,
                surfaceContainerHighest = colors.surfaceHover,
                surfaceTint = Color.Transparent,
                outline = colors.border,
                outlineVariant = colors.border.copy(alpha = 0.5f),
                error = colors.danger,
                onError = colors.background,
                errorContainer = colors.danger.copy(alpha = 0.12f).compositeOver(colors.surface),
                onErrorContainer = colors.danger,
                inverseSurface = colors.textPrimary,
                inverseOnSurface = colors.background,
                inversePrimary = colors.accentPrimary,
            )
        }
    CompositionLocalProvider(LocalReadyOColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
