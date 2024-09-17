package com.bpdevop.mediccontrol.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bpdevop.mediccontrol.ui.screens.ForgotPasswordScreen
import com.bpdevop.mediccontrol.ui.screens.LoginScreen
import com.bpdevop.mediccontrol.ui.screens.SignUpScreen
import com.bpdevop.mediccontrol.ui.theme.MedicControlTheme
import com.bpdevop.mediccontrol.ui.viewmodels.UserSessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val viewModel: UserSessionViewModel by viewModels()
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { isLoading }

        viewModel.checkUserAuthentication()

        setContent {
            MedicControlTheme {
                val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

                if (isUserAuthenticated) {
                    LaunchedEffect(Unit) {
                        navigateToMainActivity()
                    }
                } else {
                    LoginContent()
                    isLoading = false
                }
            }
        }
    }

    @Composable
    private fun LoginContent() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

        when (currentScreen) {
            is Screen.Login -> LoginScreen(
                onSignUpClick = {
                    currentScreen = Screen.SignUp
                },
                onLoginSuccess = {
                    navigateToMainActivity()
                },
                onForgotPasswordClick = {
                    currentScreen = Screen.ForgotPassword // Navega a la pantalla de recuperación de contraseña
                }
            )

            is Screen.SignUp -> SignUpScreen(
                onSignUpSuccess = {
                    navigateToMainActivity() // Redirigir a MainActivity después del registro
                },
                onBackToLoginClick = {
                    currentScreen = Screen.Login
                }
            )

            is Screen.ForgotPassword -> ForgotPasswordScreen(
                onPasswordReset = {
                    currentScreen = Screen.Login
                },
                onBackToLoginClick = {
                    currentScreen = Screen.Login
                }
            )
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

sealed class Screen {
    data object Login : Screen()
    data object SignUp : Screen()
    data object ForgotPassword : Screen()
}