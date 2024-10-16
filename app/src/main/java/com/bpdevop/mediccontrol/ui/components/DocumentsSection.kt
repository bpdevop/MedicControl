package com.bpdevop.mediccontrol.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.bpdevop.mediccontrol.R

@Composable
fun DocumentsSection(
    initialDocuments: List<String>,             // Lista de documentos iniciales (URLs)
    newDocumentUris: List<Uri>,                 // Lista de documentos nuevos (URIs locales)
    onRemoveDocument: (Uri) -> Unit,            // Callback para eliminar documentos nuevos
    onRemoveExistingDocument: (String) -> Unit,  // Callback para eliminar documentos existentes
) {
    // Mostrar documentos subidos inicialmente
    if (initialDocuments.isNotEmpty()) {
        Text(text = stringResource(R.string.new_examination_uploaded_files))
        initialDocuments.forEach { filePath ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filePath,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { onRemoveExistingDocument(filePath) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }

    // Mostrar nuevos documentos seleccionados
    if (newDocumentUris.isNotEmpty()) {
        Text(text = stringResource(R.string.new_examination_new_files))
        newDocumentUris.forEach { uri ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uri.lastPathSegment ?: "",
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { onRemoveDocument(uri) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}
