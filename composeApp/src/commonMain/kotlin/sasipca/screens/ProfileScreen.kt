package sasipca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import sasipca.ui.components.Header
import sasipca.utils.getFormattedDatePt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Header("Perfil", getFormattedDatePt())

    }
}
