package com.bpdevop.mediccontrol.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bpdevop.mediccontrol.ui.components.MainAppScaffold
import com.bpdevop.mediccontrol.ui.screens.VerifyEmailScreen
import com.bpdevop.mediccontrol.ui.theme.MedicControlTheme
import com.bpdevop.mediccontrol.ui.viewmodels.UserSessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: UserSessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.checkEmailVerification()

        setContent {
            MedicControlTheme {
                MainAppScaffold()
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: UserSessionViewModel) {
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()

    if (!isEmailVerified) {
        VerifyEmailScreen()
    }
}