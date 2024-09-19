package com.bpdevop.mediccontrol.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.createImageFile
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.core.utils.deleteImageFile
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.viewmodels.PatientsViewModel
import java.io.File
import java.util.Date

@Composable
fun PatientDetailScreen(
    patientId: String,
    viewModel: PatientsViewModel = hiltViewModel(),
    onPatientUpdated: () -> Unit,
    onPatientDeleted: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var bloodType by remember { mutableStateOf("") }
    var rhFactor by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var photoFile: File? = null
    var showImageOptions by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Estado del detalle del paciente
    val patientDetailState by viewModel.patientDetailState.collectAsState()

    // Cargar el detalle del paciente
    LaunchedEffect(patientId) {
        viewModel.loadPatientDetail(patientId)
    }

    val cameraPermissionDeniedMessage = stringResource(R.string.detail_patient_permission_denied)

    // Lanzador de cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri.let { photoUri = it }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoFile = context.createImageFile()
            cameraUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile!!)
            cameraLauncher.launch(cameraUri)
        } else {
            Toast.makeText(context, cameraPermissionDeniedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    when (patientDetailState) {
        is UiState.Success -> {
            val patient = (patientDetailState as UiState.Success<Patient>).data

            // Cargar los datos en los estados solo si no estamos en modo edición
            if (!isEditing) {
                name = patient.name
                phone = patient.phone ?: ""
                email = patient.email ?: ""
                address = patient.address ?: ""
                notes = patient.notes ?: ""
                birthDate = patient.birthDate
                bloodType = patient.bloodType ?: ""
                rhFactor = if (patient.rhFactor == true) "+" else "-"
                gender = patient.gender ?: ""
                doctorId = patient.doctorId
                photoUri = patient.photoUrl?.let { Uri.parse(it) }
            }
        }

        is UiState.Error -> {
            Toast.makeText(context, (patientDetailState as UiState.Error).message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }

    // UI del detalle del paciente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = photoUri ?: R.drawable.ic_person_placeholder
                ),
                contentDescription = stringResource(R.string.detail_patient_photo_desc),
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .clickable { if (isEditing) showImageOptions = true }
            )

            DropdownMenu(
                expanded = showImageOptions,
                onDismissRequest = { showImageOptions = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.detail_patient_take_photo)) },
                    onClick = {
                        val permissionCheckResult = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        )
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            photoFile = context.createImageFile()
                            cameraUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile!!)
                            cameraLauncher.launch(cameraUri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        showImageOptions = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.detail_patient_choose_photo)) },
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showImageOptions = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.detail_patient_remove_photo)) },
                    onClick = {
                        deleteImageFile(photoFile)
                        photoUri = null
                        showImageOptions = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.detail_patient_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate?.toString() ?: "",
            onValueChange = {},
            enabled = false,
            label = { Text(stringResource(R.string.detail_patient_birthdate)) },
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de género
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "M",
                    onClick = { if (isEditing) gender = "M" },
                    enabled = isEditing
                )
                Text(text = stringResource(R.string.detail_patient_male))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "F",
                    onClick = { if (isEditing) gender = "F" },
                    enabled = isEditing
                )
                Text(text = stringResource(R.string.detail_patient_female))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para teléfono
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.detail_patient_phone)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.detail_patient_email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para dirección
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.detail_patient_address)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para notas
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.detail_patient_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            enabled = isEditing,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de editar/guardar
        Button(
            onClick = {
                if (isEditing) {
                    viewModel.updatePatient(
                        Patient(
                            id = patientId,
                            name = name,
                            phone = phone.ifEmpty { null },
                            email = email.ifEmpty { null },
                            address = address.ifEmpty { null },
                            notes = notes.ifEmpty { null },
                            birthDate = birthDate,
                            bloodType = bloodType.ifEmpty { null },
                            rhFactor = rhFactor == "+",
                            gender = gender,
                            doctorId = doctorId
                        ),
                        photoUri
                    )
                    onPatientUpdated()
                }
                isEditing = !isEditing
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isEditing) stringResource(R.string.detail_patient_action_save) else stringResource(R.string.detail_patient_action_edit))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de eliminar
        Button(
            onClick = {
                viewModel.deletePatient(
                    Patient(
                        id = patientId,
                        name = name,
                        doctorId = doctorId
                    )
                )
                onPatientDeleted()
            },
            colors = ButtonDefaults.buttonColors(Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.detail_patient_action_delete))
        }
    }
}
