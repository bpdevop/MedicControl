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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.ui.components.EmailField
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.PasswordField
import com.bpdevop.mediccontrol.ui.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onBackToLoginClick: () -> Unit,
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(false) }
    var isConfirmPasswordValid by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    // Manejador del botón de registro
    fun onSignUpClick() {
        if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            if (password == confirmPassword) {
                authViewModel.signUp(email, password)
            } else {
                dialogMessage = "Las contraseñas no coinciden."
                showDialog = true
            }
        } else {
            dialogMessage = "Por favor, completa todos los campos correctamente."
            showDialog = true
        }
    }

    // Layout principal de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

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
            imeAction = ImeAction.Next,
            onImeAction = {
                confirmPasswordFocusRequester.requestFocus()
            },
            modifier = Modifier.focusRequester(passwordFocusRequester)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            label = "Confirmar Contraseña",
            onValidationChange = {
                isConfirmPasswordValid = it
            },
            imeAction = ImeAction.Done,
            onImeAction = {
                onSignUpClick()
            },
            modifier = Modifier.focusRequester(confirmPasswordFocusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSignUpClick() },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEmailValid && isPasswordValid && isConfirmPasswordValid
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBackToLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
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
                if (dialogMessage.contains("Registro exitoso")) {
                    onSignUpSuccess() // Redirige a MainActivity en lugar de LoginActivity
                }
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
            dialogMessage = "Registro exitoso. Por favor, verifica tu correo electrónico."
            showDialog = true
        }

        is UiState.Error -> {
            dialogMessage = (uiState as UiState.Error).message
            showDialog = true
        }

        is UiState.Idle -> {
            // No hacer nada
        }
    }

    // Solicitar foco al campo de correo electrónico al inicio
    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }
}