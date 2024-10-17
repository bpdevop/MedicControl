package com.bpdevop.mediccontrol.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.core.utils.deleteImageFile
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.ui.components.ImagePickerComponent
import com.bpdevop.mediccontrol.ui.viewmodels.DoctorProfileViewModel
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: DoctorProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var doctorId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var displayPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile: File? = null
    var isEditing by remember { mutableStateOf(false) }

    val doctorProfileState by viewModel.doctorProfileState.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getDoctorProfile()
    }

    LaunchedEffect(updateProfileState) {
        if (updateProfileState is UiState.Success) {
            photoFile?.let { deleteImageFile(it) }
            viewModel.resetUpdateProfileState()
            isEditing = false
            viewModel.getDoctorProfile()
            Toast.makeText(context, (updateProfileState as UiState.Success).data, Toast.LENGTH_SHORT).show()
        }
    }

    when (doctorProfileState) {
        is UiState.Success -> {
            val doctorProfile = (doctorProfileState as UiState.Success<DoctorProfile?>).data

            if (!isEditing) {
                doctorId = doctorProfile?.id ?: ""
                name = doctorProfile?.name ?: ""
                email = doctorProfile?.email ?: ""
                registrationNumber = doctorProfile?.registrationNumber ?: ""
                phoneNumber = doctorProfile?.phoneNumber ?: ""
                photoUrl = doctorProfile?.photoUrl
                if (photoUri == null) {
                    displayPhotoUri = photoUrl?.let { Uri.parse(it) }
                }
            }
        }

        is UiState.Error -> {
            Toast.makeText(context, (doctorProfileState as UiState.Error).message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ImagePickerComponent(
            imageUri = displayPhotoUri,
            onImagePicked = { newUri, file ->
                photoUri = newUri
                displayPhotoUri = newUri
                photoFile = file
            },
            onImageRemoved = { file ->
                file?.let { deleteImageFile(it) }
                displayPhotoUri = null
                photoUri = null
                photoFile = null
            },
            isEditing = isEditing
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.profile_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.profile_email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = registrationNumber,
            onValueChange = { registrationNumber = it },
            label = { Text(stringResource(R.string.profile_registration_number)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text(stringResource(R.string.profile_phone)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isEditing) {
                    viewModel.updateDoctorProfile(
                        DoctorProfile(
                            id = doctorId,
                            name = name,
                            email = email,
                            registrationNumber = registrationNumber,
                            phoneNumber = phoneNumber,
                            photoUrl = photoUrl
                        ),
                        photoUri
                    )
                }
                isEditing = !isEditing
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = updateProfileState !is UiState.Loading
        ) {
            if (updateProfileState is UiState.Loading) {
                CircularProgressIndicator()
            } else {
                Text(text = if (isEditing) stringResource(R.string.profile_action_save) else stringResource(R.string.profile_action_edit))
            }
        }
    }
}