package sasipca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CardTitle(title: String){
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
}