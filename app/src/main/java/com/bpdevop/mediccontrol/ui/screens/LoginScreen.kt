package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.ui.components.EmailField
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.PasswordField
import com.bpdevop.mediccontrol.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    fun onLoginClick() {
        if (isEmailValid && isPasswordValid) {
            authViewModel.signIn(email, password)
        } else {
            dialogMessage = "Por favor, verifica tu email y contraseña."
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

        Image(
            painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
        )

        EmailField(
            value = email,
            onValueChange = {
                email = it
            },
            onValidationChange = {
                isEmailValid = it
            },
            imeAction = ImeAction.Next,
            onImeAction = {
                passwordFocusRequester.requestFocus()
            },
            modifier = Modifier.focusRequester(emailFocusRequester)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = password,
            onValueChange = {
                password = it
            },
            onValidationChange = {
                isPasswordValid = it
            },
            imeAction = ImeAction.Done,
            onImeAction = {
                onLoginClick()
            },
            modifier = Modifier.focusRequester(passwordFocusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLoginClick() },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEmailValid && isPasswordValid
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onForgotPasswordClick, // Añadir la opción para recuperación de contraseña
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No tienes cuenta? Regístrate aquí")
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

    // Manejar el estado de la UI
    when (uiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            onLoginSuccess()  // Redirige al `MainActivity` después de un inicio de sesión exitoso
            authViewModel.resetUiState()
        }

        is UiState.Error -> {
            dialogMessage = (uiState as UiState.Error).message
            showDialog = true
        }

        is UiState.Idle -> {
            // No hacer nada
        }
    }
}