package sasipca.ui.components.beneficiaries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryPostDTO
import sasipca.repositories.BeneficiaryRepository
import sasipca.viewmodels.BeneficiaryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBeneficiaryPopup(
    repository: BeneficiaryRepository,
    onDismiss: () -> Unit = {},
    onCreated: () -> Unit = {}
) {
    val viewModel = remember { BeneficiaryDetailViewModel(repository) } // ou outro VM específico se existir
    val scope = rememberCoroutineScope()

    // Campos do formulário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var nif by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var studentNum by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var curricularYear by remember { mutableStateOf("") }
    var globalObs by remember { mutableStateOf("") }
    var particularObs by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Criar Beneficiário", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

                // Campos do formulário
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nif,
                    onValueChange = { nif = it },
                    label = { Text("NIF") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Rua") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número da Porta") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Código-Postal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = studentNum,
                    onValueChange = { studentNum = it },
                    label = { Text("Número de Estudante") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Curso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = curricularYear,
                    onValueChange = { curricularYear = it },
                    label = { Text("Ano Curricular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = globalObs,
                    onValueChange = { globalObs = it },
                    label = { Text("Observações Globais") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = particularObs,
                    onValueChange = { particularObs = it },
                    label = { Text("Observações Particulares") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        val dto = BeneficiaryPostDTO(
                            name = name,
                            email = email,
                            contact = contact,
                            nif = nif.toIntOrNull(),
                            street = street,
                            number = number.toIntOrNull(),
                            postalCode = postalCode,
                            studentNum = studentNum.toIntOrNull(),
                            course = course,
                            curricularYear = curricularYear.toIntOrNull(),
                            globalObs = globalObs,
                            particularObs = particularObs
                        )

                        scope.launch {
                            viewModel.createBeneficiary(dto,
                                onSuccess = {
                                    onCreated()
                                    onDismiss()
                                }
                            )
                        }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Criar Beneficiário")
                }

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

