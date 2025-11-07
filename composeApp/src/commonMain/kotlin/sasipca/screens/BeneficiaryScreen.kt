package sasipca.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class para o beneficiário completo
data class BeneficiaryDetails(
    val id: Int,
    val name: String,
    val email: String,
    val contact: String,
    val nif: Int?,
    val course: String,
    val curricularYear: Int?,
    val studentNum: Int?,
    val globalObs: String?,
    val particularObs: String?,
    val street: String?,
    val number: Int?,
    val postalCode: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryScreen(
    beneficiaryId: Int,
    onBack: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Mock data - substituir por chamada à API
    var beneficiary by remember {
        mutableStateOf(
            BeneficiaryDetails(
                id = beneficiaryId,
                name = "Ana Sofia Costa",
                email = "ana.costa@ipca.pt",
                contact = "912345678",
                nif = 123456789,
                course = "Engenharia Informática",
                curricularYear = 2,
                studentNum = 25123,
                globalObs = "Beneficiária regular do programa de apoio alimentar.",
                particularObs = "Preferência por produtos sem lactose.",
                street = "Rua das Flores",
                number = 123,
                postalCode = "4750-123"
            )
        )
    }

    // Estados editáveis
    var editName by remember { mutableStateOf(beneficiary.name) }
    var editEmail by remember { mutableStateOf(beneficiary.email) }
    var editContact by remember { mutableStateOf(beneficiary.contact) }
    var editCourse by remember { mutableStateOf(beneficiary.course) }
    var editCurricularYear by remember { mutableStateOf(beneficiary.curricularYear?.toString() ?: "") }
    var editGlobalObs by remember { mutableStateOf(beneficiary.globalObs ?: "") }
    var editParticularObs by remember { mutableStateOf(beneficiary.particularObs ?: "") }
    var editStreet by remember { mutableStateOf(beneficiary.street ?: "") }
    var editNumber by remember { mutableStateOf(beneficiary.number?.toString() ?: "") }
    var editPostalCode by remember { mutableStateOf(beneficiary.postalCode ?: "") }


    // Dialog de confirmação para apagar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Beneficiário") },
            text = { Text("Tem a certeza que deseja eliminar este beneficiário? Esta ação não pode ser revertida.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Chamar API para eliminar
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isEditMode) {
                            // Cancelar edição
                            isEditMode = false
                            // Resetar valores
                            editName = beneficiary.name
                            editEmail = beneficiary.email
                            editContact = beneficiary.contact
                            editCourse = beneficiary.course
                            editCurricularYear = beneficiary.curricularYear?.toString() ?: ""
                            editGlobalObs = beneficiary.globalObs ?: ""
                            editParticularObs = beneficiary.particularObs ?: ""
                            editStreet = beneficiary.street ?: ""
                            editNumber = beneficiary.number?.toString() ?: ""
                            editPostalCode = beneficiary.postalCode ?: ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isEditMode) "Cancelar" else "Voltar"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Editar Beneficiário" else "Beneficiário",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Informações Pessoais
                item {
                    SectionCard(title = "Informações Pessoais") {
                        FormField(
                            label = "Nome",
                            value = editName,
                            onValueChange = { editName = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Person
                        )
                        FormField(
                            label = "Email",
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email
                        )
                        FormField(
                            label = "Contacto",
                            value = editContact,
                            onValueChange = { editContact = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Phone,
                            keyboardType = KeyboardType.Phone
                        )
                        if (beneficiary.nif != null) {
                            InfoField(
                                label = "NIF",
                                value = beneficiary.nif.toString(),
                                icon = Icons.Outlined.Badge
                            )
                        }
                        if (beneficiary.studentNum != null) {
                            InfoField(
                                label = "Número de Estudante",
                                value = beneficiary.studentNum.toString(),
                                icon = Icons.Outlined.School
                            )
                        }
                    }
                }

                // Informações Académicas
                item {
                    SectionCard(title = "Informações Académicas") {
                        FormField(
                            label = "Curso",
                            value = editCourse,
                            onValueChange = { editCourse = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.School
                        )
                        FormField(
                            label = "Ano Curricular",
                            value = editCurricularYear,
                            onValueChange = { editCurricularYear = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.CalendarToday,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                // Morada
                item {
                    SectionCard(title = "Morada") {
                        FormField(
                            label = "Rua",
                            value = editStreet,
                            onValueChange = { editStreet = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Home
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Número",
                                    value = editNumber,
                                    onValueChange = { editNumber = it },
                                    enabled = isEditMode,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Código Postal",
                                    value = editPostalCode,
                                    onValueChange = { editPostalCode = it },
                                    enabled = isEditMode,
                                    placeholder = "0000-000"
                                )
                            }
                        }
                    }
                }

                // Observações
                item {
                    SectionCard(title = "Observações") {
                        FormFieldMultiline(
                            label = "Observações Globais",
                            value = editGlobalObs,
                            onValueChange = { editGlobalObs = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Info
                        )
                        FormFieldMultiline(
                            label = "Observações Particulares",
                            value = editParticularObs,
                            onValueChange = { editParticularObs = it },
                            enabled = isEditMode,
                            icon = Icons.Outlined.Lock
                        )
                    }
                }

                // Botão de guardar (só aparece em modo edição)
                if (isEditMode) {
                    item {
                        Button(
                            onClick = {
                                // TODO: Chamar API PUT /api/beneficiaries/{id}
                                isEditMode = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Alterações",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            content()
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = !enabled,
            placeholder = { Text(placeholder.ifEmpty { label }) },
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun FormFieldMultiline(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector? = null
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            enabled = enabled,
            readOnly = !enabled,
            placeholder = { Text(label) },
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = value,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}