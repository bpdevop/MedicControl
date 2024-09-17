package com.bpdevop.mediccontrol.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.viewmodels.PatientsViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    viewModel: PatientsViewModel = hiltViewModel(),
    onPatientAdded: () -> Unit,
) {
    val context = LocalContext.current
    val cacheDir = context.cacheDir

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // State variables
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var bloodType by remember { mutableStateOf("") }
    var rhFactor by remember { mutableStateOf("+") }
    var gender by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var showBloodTypeMenu by remember { mutableStateOf(false) }
    var showRHMenu by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val addPatientState by viewModel.addPatientState.collectAsState()

    // Camera and gallery launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // La imagen seleccionada se almacena temporalmente en photoUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it // Almacenar temporalmente la imagen seleccionada de la galería
        }
    }

    // Toast para mostrar errores
    val showErrorToast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Manejar el estado de agregar paciente
    if (addPatientState is UiState.Success) {
        LaunchedEffect(Unit) {
            onPatientAdded()
            viewModel.resetAddPatientState()
        }
    }

    // Contenido principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Imagen del paciente (ya seleccionada o placeholder)
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = photoUri ?: R.drawable.ic_person_placeholder
                ),
                contentDescription = stringResource(R.string.add_patient_photo_desc),
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .clickable { showImageOptions = true }
            )

            // Menú contextual con opciones para cambiar o eliminar la imagen
            DropdownMenu(
                expanded = showImageOptions,
                onDismissRequest = { showImageOptions = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_patient_take_photo)) },
                    onClick = {
                        val photoFile = File(cacheDir, "patient_photo_${UUID.randomUUID()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                        showImageOptions = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_patient_choose_photo)) },
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showImageOptions = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_patient_remove_photo)) },
                    onClick = {
                        photoUri = null
                        showImageOptions = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input de nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.add_patient_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input de fecha de nacimiento
        OutlinedTextField(
            value = birthDate?.let { dateFormat.format(it) } ?: "",
            onValueChange = {},
            enabled = false,
            label = { Text(stringResource(R.string.add_patient_birthdate)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }  // Mostrar DatePicker
        )

        // Muestra el DatePickerModal
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let {
                        birthDate = Date(it)
                    }
                    showDatePicker = false
                },
                onDismiss = {
                    showDatePicker = false
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Género (RadioButton para "Masculino" y "Femenino")
        Text(text = stringResource(R.string.add_patient_gender), modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "M",
                    onClick = { gender = "M" }
                )
                Text(text = stringResource(R.string.add_patient_male))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "F",
                    onClick = { gender = "F" }
                )
                Text(text = stringResource(R.string.add_patient_female))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selección de tipo de sangre y RH
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = showBloodTypeMenu,
                onExpandedChange = { showBloodTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = bloodType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_patient_blood_type)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodTypeMenu)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .weight(1f)
                )

                ExposedDropdownMenu(
                    expanded = showBloodTypeMenu,
                    onDismissRequest = { showBloodTypeMenu = false }
                ) {
                    val bloodTypes = listOf("A", "B", "O", "AB")
                    bloodTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                bloodType = type
                                showBloodTypeMenu = false
                            }
                        )
                    }
                }
            }

            // Lista desplegable para RH
            ExposedDropdownMenuBox(
                expanded = showRHMenu,
                onExpandedChange = { showRHMenu = it }
            ) {
                OutlinedTextField(
                    value = rhFactor,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_patient_rh_factor)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRHMenu)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .weight(0.5f)
                )

                ExposedDropdownMenu(
                    expanded = showRHMenu,
                    onDismissRequest = { showRHMenu = false }
                ) {
                    listOf("+", "-").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                rhFactor = type
                                showRHMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input de teléfono
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.add_patient_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone // Para teclado numérico de teléfono
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input de dirección
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.add_patient_address)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input de email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.add_patient_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email // Para teclado de correo
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input de notas (Estilo textarea)
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.add_patient_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp), // Define la altura del textarea
            maxLines = 4 // Mostrar varias filas
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para agregar paciente
        Button(
            onClick = {
                loading = true

                val patient = Patient(
                    name = name,
                    phone = phone.ifEmpty { null },
                    email = email.ifEmpty { null },
                    address = address.ifEmpty { null },
                    notes = notes.ifEmpty { null },
                    birthDate = birthDate,
                    bloodType = bloodType.ifEmpty { null },
                    rhFactor = rhFactor == "+",
                    gender = gender
                )

                // Llamar al ViewModel con el objeto paciente y la Uri de la foto (si existe)
                viewModel.addPatient(patient, photoUri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.add_patient_action_add))
            }
        }

        // Manejo del estado de carga y errores
        when (addPatientState) {
            is UiState.Error -> LaunchedEffect(Unit) {
                showErrorToast((addPatientState as UiState.Error).message)
                loading = false
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}