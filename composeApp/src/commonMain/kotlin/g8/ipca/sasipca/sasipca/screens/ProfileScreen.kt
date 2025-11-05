package g8.ipca.sasipca.sasipca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import g8.ipca.sasipca.sasipca.ui.components.Header
import g8.ipca.sasipca.sasipca.utils.getFormattedDatePt

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
