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
import sasipca.ui.components.ValidatedTextField
import sasipca.ui.components.formatPostalCode
import sasipca.viewmodels.BeneficiaryDetailViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBeneficiaryPopup(
    repository: BeneficiaryRepository,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val viewModel = remember { BeneficiaryDetailViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // --- ESTADOS ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("+351") }
    var nif by remember { mutableStateOf("") }

    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var studentNum by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var curricularYear by remember { mutableStateOf("") }

    var globalObs by remember { mutableStateOf("") }
    var particularObs by remember { mutableStateOf("") }

    // Reagir ao Sucesso
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onCreated()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // CABEÇALHO
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

                // CONTEÚDO COM SCROLL
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. DADOS PESSOAIS
                    SectionTitle("Dados Pessoais")

                    ValidatedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome Completo",
                        error = uiState.errors["name"],
                        maxLength = 50
                    )

                    ValidatedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        error = uiState.errors["email"],
                        maxLength = 50,
                        keyboardType = KeyboardType.Email
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedTextField(
                            value = contact,
                            onValueChange = { contact = it },
                            label = "Contacto",
                            error = uiState.errors["contact"],
                            maxLength = 13,
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = nif,
                            // Garante apenas números no NIF
                            onValueChange = { input -> if (input.all { it.isDigit() }) nif = input },
                            label = "NIF",
                            error = uiState.errors["nif"],
                            maxLength = 9,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 2. MORADA
                    SectionTitle("Morada")

                    ValidatedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = "Rua",
                        error = uiState.errors["street"],
                        maxLength = 255
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedTextField(
                            value = number,
                            // Garante apenas números
                            onValueChange = { input -> if (input.all { it.isDigit() }) number = input },
                            label = "Nº Porta",
                            error = uiState.errors["number"],
                            maxLength = 6,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(0.4f)
                        )

                        // --- CAMPO CÓDIGO POSTAL ---
                        ValidatedTextField(
                            value = postalCode,
                            // Aplica a formatação automática aqui
                            onValueChange = { postalCode = formatPostalCode(it) },
                            label = "Código Postal",
                            error = uiState.errors["postalCode"],
                            maxLength = 8,
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    // 3. DADOS ACADÉMICOS
                    SectionTitle("Dados Académicos")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedTextField(
                            value = studentNum,
                            onValueChange = { input -> if (input.all { it.isDigit() }) studentNum = input },
                            label = "Nº Mec.",
                            error = uiState.errors["studentNum"],
                            maxLength = 10,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedTextField(
                            value = curricularYear,
                            onValueChange = { input -> if (input.all { it.isDigit() }) curricularYear = input },
                            label = "Ano",
                            error = uiState.errors["curricularYear"],
                            maxLength = 2,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    ValidatedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = "Curso",
                        error = uiState.errors["course"],
                        maxLength = 50
                    )

                    // 4. OBSERVAÇÕES
                    SectionTitle("Observações")

                    ValidatedTextField(
                        value = globalObs,
                        onValueChange = { globalObs = it },
                        label = "Globais",
                        singleLine = false,
                        modifier = Modifier.height(80.dp)
                    )

                    ValidatedTextField(
                        value = particularObs,
                        onValueChange = { particularObs = it },
                        label = "Particulares",
                        singleLine = false,
                        modifier = Modifier.height(80.dp)
                    )

                    if (uiState.lastErrorMessage != null) {
                        Text(
                            text = uiState.lastErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // --- RODAPÉ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !uiState.isLoading) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.submitCreateBeneficiary(
                                name, email, contact, nif, street, number, postalCode,
                                studentNum, course, curricularYear, globalObs, particularObs
                            )
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
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
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}