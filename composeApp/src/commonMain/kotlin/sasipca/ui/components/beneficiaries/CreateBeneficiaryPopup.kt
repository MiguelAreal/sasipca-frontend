package sasipca.ui.components.beneficiaries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryPost
import sasipca.repositories.BeneficiaryRepository
import sasipca.viewmodels.BeneficiaryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBeneficiaryPopup(
    repository: BeneficiaryRepository,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    // Inicializar ViewModel
    val viewModel = remember { BeneficiaryDetailViewModel(repository) }
    val scope = rememberCoroutineScope()

    // --- ESTADOS DO FORMULÁRIO ---
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Ocupa largura adequada
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f) // 95% da largura do ecrã
                .fillMaxHeight(0.9f) // Máximo 90% da altura para caber o teclado
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- CABEÇALHO ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Novo Beneficiário",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Preencha os dados abaixo para registar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // --- CONTEÚDO COM SCROLL ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. DADOS PESSOAIS
                    SectionTitle("Dados Pessoais")

                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = contact, onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) contact = it },
                            label = { Text("Telemóvel") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = nif, onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) nif = it },
                            label = { Text("NIF") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    // 2. MORADA
                    SectionTitle("Morada")

                    OutlinedTextField(
                        value = street, onValueChange = { street = it },
                        label = { Text("Rua") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = number, onValueChange = { number = it },
                            label = { Text("Nº Porta") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = postalCode, onValueChange = { postalCode = it },
                            label = { Text("Cód. Postal") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // 3. DADOS ACADÉMICOS
                    SectionTitle("Dados Académicos")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = studentNum, onValueChange = { if (it.all { c -> c.isDigit() }) studentNum = it },
                            label = { Text("Nº Estudante") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = curricularYear, onValueChange = { if (it.length <= 1 && it.all { c -> c.isDigit() }) curricularYear = it },
                            label = { Text("Ano") },
                            modifier = Modifier.weight(0.5f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    OutlinedTextField(
                        value = course, onValueChange = { course = it },
                        label = { Text("Curso") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
                    )

                    // 4. OBSERVAÇÕES
                    SectionTitle("Observações")

                    OutlinedTextField(
                        value = globalObs, onValueChange = { globalObs = it },
                        label = { Text("Observações Globais") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = particularObs, onValueChange = { particularObs = it },
                        label = { Text("Observações Particulares") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // --- RODAPÉ COM AÇÕES ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "O nome é obrigatório."
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            val dto = BeneficiaryPost(
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
                                        isLoading = false
                                        onCreated()
                                        onDismiss()
                                    }
                                )
                                // Adiciona tratamento de erro aqui se o ViewModel suportar,
                                // ou o isLoading fica true para sempre se falhar
                                // isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Criar Beneficiário")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}