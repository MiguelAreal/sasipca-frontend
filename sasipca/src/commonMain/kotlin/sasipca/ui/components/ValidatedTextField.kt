package sasipca.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import sasipca.ui.theme.UnderlineError

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None, // Adicionado
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // Se houver limite, corta o input extra
                val newValue = if (maxLength != null) input.take(maxLength) else input
                onValueChange(newValue)
            },
            label = { Text(label) },
            isError = error != null,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                cursorColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        )
        if (error != null) {
            UnderlineError(error)
        }
    }
}

// --- FUNÇÃO AUXILIAR DE FORMATAÇÃO ---
fun formatPostalCode(input: String): String {
    // 1. Remove tudo o que não é dígito
    val digits = input.filter { it.isDigit() }

    // 2. Limita a 7 caracteres (4+3)
    val truncated = digits.take(7)

    // 3. Aplica a máscara xxxx-xxx
    return if (truncated.length > 4) {
        "${truncated.substring(0, 4)}-${truncated.substring(4)}"
    } else {
        truncated
    }
}