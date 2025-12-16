package sasipca.ui.components.beneficiaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sasipca.models.BeneficiaryGet
import sasipca.models.BeneficiaryPost
import sasipca.ui.components.LoadingWidget
import sasipca.ui.components.ValidatedTextField
import sasipca.ui.components.formatPostalCode
import sasipca.ui.theme.CardTitle

@Composable
fun BeneficiaryEditForm(
    beneficiary: BeneficiaryGet?,
    isLoading: Boolean,
    errors: Map<String, String>,
    onSave: (BeneficiaryPost) -> Unit,
    isReadOnly: Boolean = false
) {
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

    LaunchedEffect(beneficiary) {
        beneficiary?.let {
            editName = it.name
            editEmail = it.email
            editContact = it.contact
            editNif = it.nif?.toString() ?: ""
            editStreet = it.street ?: ""
            editNumber = it.number?.toString() ?: ""
            editPostalcode = it.postalCode ?: ""
            editStudentnum = it.studentNum?.toString() ?: ""
            editCourse = it.course ?: ""
            editCurricularYear = it.curricularYear?.toString() ?: ""
            editGlobalObs = it.globalObs ?: ""
            editParticularObs = it.particularObs ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informações Pessoais
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        CardTitle("Informações Pessoais")

                        ValidatedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = "Nome",
                            error = errors["name"],
                            maxLength = 50,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = "E-Mail",
                            error = errors["email"],
                            maxLength = 50,
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isReadOnly
                        )
                        ValidatedTextField(
                            value = editContact,
                            onValueChange = { editContact = it },
                            label = "Contacto",
                            error = errors["contact"],
                            maxLength = 13,
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = editNif,
                            onValueChange = { editNif = it },
                            label = "NIF",
                            error = errors["nif"],
                            maxLength = 9,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Morada
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardTitle("Morada")

                        ValidatedTextField(
                            value = editStreet,
                            onValueChange = { editStreet = it },
                            label = "Rua",
                            error = errors["street"],
                            maxLength = 255,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = editNumber,
                            onValueChange = { editNumber = it },
                            label = "Número da Porta",
                            error = errors["number"],
                            maxLength = 11,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ValidatedTextField(
                            value = editPostalcode,
                            onValueChange = { editPostalcode = formatPostalCode(it) },
                            label = "Código Postal",
                            error = errors["postalCode"],
                            maxLength = 8,
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Informações Académicas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardTitle("Informações Académicas")

                        ValidatedTextField(
                            value = editStudentnum,
                            onValueChange = { editStudentnum = it },
                            label = "Número de estudante",
                            error = errors["studentNum"],
                            maxLength = 10,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = editCourse,
                            onValueChange = { editCourse = it },
                            label = "Curso",
                            error = errors["course"],
                            maxLength = 50,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ValidatedTextField(
                            value = editCurricularYear,
                            onValueChange = { editCurricularYear = it },
                            label = "Ano Curricular",
                            error = errors["curricularYear"],
                            maxLength = 2,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Observações - SÓ APARECE SE NÃO FOR READ-ONLY (ADMIN)
            if (!isReadOnly) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CardTitle("Observações")

                            ValidatedTextField(
                                value = editGlobalObs,
                                onValueChange = { editGlobalObs = it },
                                label = "Globais",
                                error = errors["obsGlobal"],
                                maxLength = 255,
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp)
                            )

                            ValidatedTextField(
                                value = editParticularObs,
                                onValueChange = { editParticularObs = it },
                                label = "Particulares",
                                error = errors["obsParticular"],
                                maxLength = 255,
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp)
                            )
                        }
                    }
                }
            }
                // Botão Guardar - SÓ APARECE SE NÃO FOR READ-ONLY
                item {
                    Button(
                        onClick = {
                            val body = BeneficiaryPost(
                                name = editName,
                                email = editEmail,
                                contact = editContact,
                                nif = editNif.toIntOrNull(),
                                street = editStreet,
                                number = editNumber.toIntOrNull(),
                                postalCode = editPostalcode,
                                studentNum = editStudentnum.toIntOrNull(),
                                course = editCourse,
                                curricularYear = editCurricularYear.toIntOrNull(),
                                globalObs = editGlobalObs,
                                particularObs = editParticularObs
                            )
                            onSave(body)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Guardar Alterações", fontSize = 16.sp)
                    }
                }

        }

        if (isLoading) {
            LoadingWidget()
        }
    }
}