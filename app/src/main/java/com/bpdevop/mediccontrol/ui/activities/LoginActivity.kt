package com.bpdevop.mediccontrol.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bpdevop.mediccontrol.ui.screens.ForgotPasswordScreen
import com.bpdevop.mediccontrol.ui.screens.LoginScreen
import com.bpdevop.mediccontrol.ui.screens.SignUpScreen
import com.bpdevop.mediccontrol.ui.theme.MedicControlTheme
import com.bpdevop.mediccontrol.ui.viewmodels.UserSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationState.collect { navigationState ->
                    when (navigationState) {

                        is UserSessionViewModel.NavigationState.LoginScreen -> {
                            isLoading = false
                            setContent {
                                MedicControlTheme {
                                    LoginContent()
                                }
                            }
                        }

                        is UserSessionViewModel.NavigationState.MainScreen -> {
                            isLoading = false
                            navigateToMainActivity()
                        }

                        else -> Unit
                    }
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
                    currentScreen = Screen.ForgotPassword
                }
            )

            is Screen.SignUp -> SignUpScreen(
                onSignUpSuccess = {
                    navigateToMainActivity()
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


}

fun Context.navigateToMainActivity() {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    (this as? Activity)?.finish()
}

private sealed class Screen {
    data object Login : Screen()
    data object SignUp : Screen()
    data object ForgotPassword : Screen()
}