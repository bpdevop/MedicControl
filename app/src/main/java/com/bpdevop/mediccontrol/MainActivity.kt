package com.bpdevop.mediccontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.ui.theme.MedicControlTheme
import com.bpdevop.mediccontrol.ui.viewmodels.UserSessionViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val userSessionViewModel: UserSessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userSessionViewModel.checkUserAuthentication()
        setContent {
            MedicControlTheme {
                val isUserAuthenticated by userSessionViewModel.isUserAuthenticated.collectAsState()
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (isUserAuthenticated && currentUser?.isEmailVerified == true) {
                    MainScreen()
                } else {
                    VerifyEmailScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Text(text = "main screen")
}

@Composable
fun VerifyEmailScreen() {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val currentUser by userSessionViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Already verified?", style = MaterialTheme.typography.titleLarge)
        Text(text = "If not, please also check the spam folder.", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            currentUser?.reload()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    // Verificar si el correo ya fue verificado
                    userSessionViewModel.checkUserAuthentication()
                }
            }
        }) {
            Text(text = "Check Verification")
        }
    }
}