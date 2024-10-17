package com.bpdevop.mediccontrol.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun CommonDialog(
    title: String? = null,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = {
            title?.let {
                Text(text = it)
            }
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        properties = DialogProperties()
    )
}
