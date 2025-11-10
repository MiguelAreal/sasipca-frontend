package sasipca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sasipca.models.BeneficiaryPostDTO
import sasipca.navigation.NavigationService
import sasipca.repositories.BeneficiaryRepository
import sasipca.ui.components.Header
import sasipca.viewmodels.BeneficiaryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryScreen(
    beneficiaryId: Int,
    repository: BeneficiaryRepository
) {
    val viewModel = remember { BeneficiaryDetailViewModel(repository) }
    val scope = rememberCoroutineScope()

    val beneficiary by remember { viewModel::beneficiary }
    val isLoading by remember { viewModel::isLoading }

    // Campos de edição
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editContact by remember { mutableStateOf("") }
    var editNif by remember { mutableStateOf("") }
    var editStreet by remember { mutableStateOf("") }
    var editNumber by remember { mutableStateOf("") }
    var editPostalcode by remember { mutableStateOf("") }
    var editStudentnum by remember { mutableStateOf("") }
    var editCourse by remember { mutableStateOf("") }
    var editCurricularYear by remember { mutableStateOf("") }
    var editGlobalObs by remember { mutableStateOf("") }
    var editParticularObs by remember { mutableStateOf("") }

    // Carrega dados iniciais
    LaunchedEffect(beneficiaryId) {
        viewModel.loadBeneficiary(beneficiaryId)
    }

    // Preenche os campos quando o beneficiário é carregado
    LaunchedEffect(beneficiary) {
        beneficiary?.let {
            editName = it.name
            editEmail = it.email
            editContact = it.contact
            editNif = it.nif?.toString() ?: ""
            editStreet = it.street?: ""
            editNumber = it.number?.toString() ?: ""
            editPostalcode = it.postalCode?: ""
            editStudentnum = it.studentNum?.toString() ?: ""
            editCourse = it.course?: ""
            editCurricularYear = it.curricularYear?.toString() ?: ""
            editGlobalObs = it.globalObs?: ""
            editParticularObs = it.particularObs?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Editar Beneficiário",subTitle = editName)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {

                /** Informações Pessoais **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Informações Pessoais",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nome") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editContact,
                                onValueChange = { editContact = it },
                                label = { Text("Contacto") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editNif,
                                onValueChange = { editNif = it },
                                label = { Text("NIF") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                /** Informações de Morada **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Morada",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = editStreet,
                                onValueChange = { editStreet = it },
                                label = { Text("Rua") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editNumber,
                                onValueChange = { editNumber = it },
                                label = { Text("Número da Porta") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editPostalcode,
                                onValueChange = { editPostalcode = it },
                                label = { Text("Código-Postal") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                        }
                    }
                }

                /** Informações Académicas **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Informações Académicas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = editStudentnum,
                                onValueChange = { editStudentnum = it },
                                label = { Text("Número de estudante") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = editCourse,
                                onValueChange = { editCourse = it },
                                label = { Text("Curso") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = editCurricularYear,
                                onValueChange = { editCurricularYear = it },
                                label = { Text("Ano Curricular") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                /** Observações **/
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Observações",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = editGlobalObs,
                                onValueChange = { editGlobalObs = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                label = { Text("Globais") },
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 5
                            )

                            OutlinedTextField(
                                value = editParticularObs,
                                onValueChange = { editParticularObs = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                label = { Text("Particulares") },
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 5
                            )
                        }
                    }
                }

                /** Botão Guardar **/
                item {
                    Button(
                        onClick = {
                            val dto = BeneficiaryPostDTO(
                                name = editName,
                                email = editEmail,
                                contact = editContact,
                                nif = editNif.toIntOrNull(),
                                street =  editStreet,
                                number = editNumber.toIntOrNull(),
                                postalCode = editPostalcode,
                                studentNum = editStudentnum.toIntOrNull(),
                                course = editCourse,
                                curricularYear = editCurricularYear.toIntOrNull(),
                                globalObs = editGlobalObs,
                                particularObs = editParticularObs
                            )
                            scope.launch {
                                viewModel.updateBeneficiary(beneficiaryId, dto) {
                                    NavigationService.goBack();
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
