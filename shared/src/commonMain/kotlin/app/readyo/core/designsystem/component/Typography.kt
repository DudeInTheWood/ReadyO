package app.readyo.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.theme.LocalReadyOColors

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        fontFamily = FontFamily.Serif,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
fun MicroLabel(label: String) {
    Text(
        label,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.sp,
        color = LocalReadyOColors.current.textMuted,
    )
}
