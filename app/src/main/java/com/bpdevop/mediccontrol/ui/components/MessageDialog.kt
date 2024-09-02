package com.bpdevop.mediccontrol.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun MessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit = onDismiss,
    dismissButtonText: String? = null,
    onDismissAction: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmButtonText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            dismissButtonText?.let {
                TextButton(onClick = { onDismissAction?.invoke() ?: onDismiss() }) {
                    Text(text = it, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        properties = DialogProperties()
    )
}