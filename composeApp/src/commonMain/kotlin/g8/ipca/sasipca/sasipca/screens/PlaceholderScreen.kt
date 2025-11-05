package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import g8.ipca.sasipca.sasipca.ui.components.Header

@Composable
fun PlaceholderScreen() {
    Header("titulo")
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "ainda não implementado", color = MaterialTheme.colorScheme.onSurfaceVariant)
}
