package com.bpdevop.mediccontrol.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.createImageFile
import java.io.File

@Composable
fun ImagePickerComponent(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    onImagePicked: (Uri?, File?) -> Unit,
    onImageRemoved: (File?) -> Unit,
    isEditing: Boolean = true,
    placeholder: Int = R.drawable.ic_person_placeholder,
) {
    val context = LocalContext.current
    var showImageOptions by remember { mutableStateOf(false) }
    var photoFile: File? = null
    var cameraUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImagePicked(cameraUri, photoFile)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it, null) }
    }

    val cameraPermissionDeniedMessage = stringResource(R.string.global_permission_denied)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoFile = context.createImageFile()
            cameraUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile!!)
            cameraLauncher.launch(cameraUri)
        } else {
            Toast.makeText(context, cameraPermissionDeniedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUri ?: placeholder
            ),
            contentDescription = stringResource(R.string.global_image_picker_content_description),
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(enabled = isEditing) { showImageOptions = true }
        )

        DropdownMenu(
            expanded = showImageOptions,
            onDismissRequest = { showImageOptions = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.global_image_picker_take_photo)) },
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        photoFile = context.createImageFile()
                        cameraUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFile!!)
                        cameraLauncher.launch(cameraUri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showImageOptions = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.global_image_picker_choose_from_gallery)) },
                onClick = {
                    galleryLauncher.launch("image/*")
                    showImageOptions = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.global_image_picker_remove_photo)) },
                onClick = {
                    onImageRemoved(photoFile)
                    showImageOptions = false
                }
            )
        }
    }
}