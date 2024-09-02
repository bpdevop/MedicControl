package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.ui.components.EmailField
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.viewmodels.AuthViewModel


@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onPasswordReset: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val passwordResetState by authViewModel.passwordResetState.collectAsState()

    fun onResetClick() {
        if (isEmailValid) {
            authViewModel.resetPassword(email)
        } else {
            dialogMessage = "Por favor, ingresa un email válido."
            showDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Recuperar Contraseña",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailField(
            value = email,
            onValueChange = {
                email = it
            },
            onValidationChange = {
                isEmailValid = it
            },
            imeAction = ImeAction.Done,
            onImeAction = {
                onResetClick()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onResetClick() },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEmailValid
        ) {
            Text("Enviar Correo de Recuperación")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBackToLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al Login")
        }
    }

    // Mostrar el diálogo si es necesario
    if (showDialog) {
        MessageDialog(
            title = "Información",
            message = dialogMessage,
            onDismiss = {
                showDialog = false
                authViewModel.resetUiState()
            }
        )
    }

    // Manejar el estado de la UI para la recuperación de contraseña
    when (passwordResetState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            dialogMessage = (passwordResetState as UiState.Success).data
            showDialog = true
            onPasswordReset() // Puedes redirigir al Login o a una pantalla de confirmación
        }

        is UiState.Error -> {
            dialogMessage = (passwordResetState as UiState.Error).message
            showDialog = true
        }

        is UiState.Idle -> {
            // No hacer nada
        }
    }
}