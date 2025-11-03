package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import g8.ipca.sasipca.sasipca.ui.components.HeaderSection

@Composable
fun PlaceholderScreen() {
    HeaderSection("titulo")
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "ainda não implementado", color = MaterialTheme.colorScheme.onSurfaceVariant)
}
