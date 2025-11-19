    package sasipca.ui.components.beneficiaries

    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.outlined.Check
    import androidx.compose.material3.Button
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import sasipca.models.BeneficiaryGet
    import sasipca.models.BeneficiaryPost
    import sasipca.ui.components.LoadingWidget
    import sasipca.ui.theme.CardTitle


    @Composable
    fun BeneficiaryEditForm(
        beneficiary: BeneficiaryGet?,
        isLoading: Boolean,
        onSave: (BeneficiaryPost) -> Unit
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

                // Observações
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
                            CardTitle("Observações")

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

                // Botão Guardar
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