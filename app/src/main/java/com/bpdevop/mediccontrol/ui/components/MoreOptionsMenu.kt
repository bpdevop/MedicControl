package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bpdevop.mediccontrol.R

@Composable
fun MoreOptionsMenu(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPrintClick: (() -> Unit)? = null,   // Opción opcional para imprimir
    onSendClick: (() -> Unit)? = null,    // Opción opcional para enviar
    editText: String = stringResource(R.string.global_message_edit),
    deleteText: String = stringResource(R.string.global_message_delete),
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            fun handleClick(action: () -> Unit) {
                expanded = false
                action()
            }

            DropdownMenuItem(
                text = { Text(editText) },
                onClick = { handleClick(onEditClick) }
            )
            DropdownMenuItem(
                text = { Text(deleteText) },
                onClick = { handleClick(onDeleteClick) }
            )

            onPrintClick?.let {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.global_message_print)) },
                    onClick = { handleClick(it) }
                )
            }

            onSendClick?.let {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.global_message_send)) },
                    onClick = { handleClick(it) }
                )
            }
        }
    }
}