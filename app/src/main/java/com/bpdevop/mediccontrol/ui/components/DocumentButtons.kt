package com.bpdevop.mediccontrol.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.createImageFile
import com.bpdevop.mediccontrol.core.extensions.createVideoFile
import java.io.File

@Composable
fun DocumentButtons(
    context: Context,
    onDocumentUris: (List<Uri>) -> Unit,
) {
    val cameraUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val videoUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val photoFiles = remember { mutableStateListOf<File>() }
    val videoFiles = remember { mutableStateListOf<File>() }
    val documentUris = remember { mutableStateListOf<Uri>() }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri.value.let {
                documentUris.add(it)
                onDocumentUris(documentUris) // Notificar con la lista actualizada
            }
        } else {
            photoFiles.removeLastOrNull()?.delete()
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            videoUri.value.let { documentUris.add(it) }
            onDocumentUris(documentUris)
        } else {
            videoFiles.removeLastOrNull()?.delete()
        }
    }

    // Permiso para la cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Lógica para foto
            val photoFileCreated = context.createImageFile()
            photoFiles.add(photoFileCreated)
            cameraUri.value = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFileCreated)
            cameraLauncher.launch(cameraUri.value)
        } else {
            Toast.makeText(context, context.getString(R.string.global_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    // Lanzador de permiso para video
    val videoPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Lógica para video
            val videoFileCreated = context.createVideoFile()
            videoFiles.add(videoFileCreated)
            videoUri.value = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", videoFileCreated)
            videoLauncher.launch(videoUri.value)
        } else {
            Toast.makeText(context, context.getString(R.string.global_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        documentUris.addAll(uris)
        onDocumentUris(documentUris)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
        }

        IconButton(onClick = { videoPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
            Icon(imageVector = Icons.Default.Videocam, contentDescription = null)
        }

        IconButton(onClick = { documentLauncher.launch("*/*") }) {
            Icon(imageVector = Icons.Default.Folder, contentDescription = null)
        }
    }
}