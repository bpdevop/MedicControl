package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    onValidationChange: (Boolean) -> Unit = {},
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Expresión regular para validar la contraseña
    val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#\$%^&+=!]).{8,}\$")


    fun validatePassword(password: String) {
        when {
            password.isEmpty() -> {
                errorMessage = "La contraseña no puede estar vacía"
                onValidationChange(false)
            }

            !passwordRegex.matches(password) -> {
                errorMessage = "La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, un número y un carácter especial"
                onValidationChange(false)
            }

            else -> {
                errorMessage = null
                onValidationChange(true)
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            validatePassword(it)
        },
        label = { Text(label) },
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                validatePassword(value)
                if (errorMessage == null) onImeAction()
            }
        ),
        trailingIcon = {
            val image = if (isPasswordVisible) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }

            IconButton(onClick = {
                isPasswordVisible = !isPasswordVisible
            }) {
                Icon(imageVector = image, contentDescription = null)
            }
        },
    )
}
