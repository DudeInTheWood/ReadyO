package app.readyo.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReadyOEmptyState(title: String, detail: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontFamily = FontFamily.Serif, fontSize = 24.sp, lineHeight = 30.sp)
        Text(detail)
    }
}

@Composable
fun ReadyOErrorState(message: String, retry: () -> Unit) {
    Column {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(retry) { Text("Retry") }
    }
}
