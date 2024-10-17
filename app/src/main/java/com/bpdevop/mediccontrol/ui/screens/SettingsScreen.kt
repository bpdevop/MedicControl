package com.bpdevop.mediccontrol.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.ui.viewmodels.AuthViewModel

@Composable
fun SettingsScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val passwordResetState by authViewModel.passwordResetState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cambiar contraseña
        Text(stringResource(id = R.string.settings_screen_account_section_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(
            icon = Icons.Default.Lock,
            title = stringResource(id = R.string.settings_screen_change_password_title),
            description = stringResource(id = R.string.settings_screen_change_password_description),
            onClick = {
                authViewModel.resetPasswordForCurrentUser()
            }
        )

        LaunchedEffect(passwordResetState) {
            when (passwordResetState) {
                is UiState.Success -> {
                    Toast.makeText(context, (passwordResetState as UiState.Success).data, Toast.LENGTH_SHORT).show()
                    authViewModel.resetPasswordResetState()
                }
                is UiState.Error -> {
                    Toast.makeText(context, (passwordResetState as UiState.Error).message, Toast.LENGTH_SHORT).show()
                    authViewModel.resetPasswordResetState()
                }
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gestión de permisos
        Text(stringResource(id = R.string.settings_screen_privacy_section_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(
            icon = Icons.Default.Security,
            title = stringResource(id = R.string.settings_screen_manage_permissions_title),
            description = stringResource(id = R.string.settings_screen_manage_permissions_description),
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Escribe una reseña
        SettingItem(
            icon = Icons.Default.Star,
            title = stringResource(id = R.string.settings_screen_write_review_title),
            description = stringResource(id = R.string.settings_screen_write_review_description),
            onClick = {
                val uri = Uri.parse("market://details?id=${context.packageName}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
