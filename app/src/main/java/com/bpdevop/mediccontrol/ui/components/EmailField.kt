package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Email",
    onValidationChange: (Boolean) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Validación del correo electrónico
    fun validateEmail(email: String) {
        val trimmedEmail = email.trim()

        when {
            trimmedEmail.isEmpty() -> {
                errorMessage = "El correo electrónico no puede estar vacío"
                onValidationChange(false)
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                errorMessage = "Correo electrónico no válido"
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
            validateEmail(it)
        },
        label = { Text(label) },
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                validateEmail(value)
                if (errorMessage == null) onImeAction()
            }
        )
    )
}
