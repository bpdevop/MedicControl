package com.bpdevop.mediccontrol.core.network

import android.content.Context
import com.bpdevop.mediccontrol.data.api.AuthService
import com.bpdevop.mediccontrol.data.api.responses.TokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService,
) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    suspend fun getAccessToken(): String {
        val token = prefs.getString("access_token", null)
        val expiresAt = prefs.getLong("expires_at", 0)

        // Verifica si el token existe y aún no ha expirado
        return if (token != null && System.currentTimeMillis() < expiresAt) {
            token
        } else {
            // Obtiene un nuevo token y guarda la información de expiración
            val newToken = fetchNewToken()
            saveToken(newToken.accessToken, newToken.expiresIn)
            newToken.accessToken
        }
    }

    private suspend fun fetchNewToken(): TokenResponse = withContext(Dispatchers.IO) {
        authService.getToken(
            clientId = "e81d4b5d-ed5b-4a46-9bb7-47328ad5e497_c3efbc29-8ec2-4dd4-8a7b-2403acd8c4d6",
            clientSecret = "BtELSCfMtD33myJuNfz6xaqt8xU0a8ZE8wuF0JB9HKI="
        )
    }

    private fun saveToken(token: String, expiresIn: Long) {
        // Calcula el tiempo de expiración en milisegundos a partir del tiempo actual
        val expirationTime = System.currentTimeMillis() + expiresIn * 1000 // convertir segundos a ms
        editor.putString("access_token", token)
        editor.putLong("expires_at", expirationTime)
        editor.apply()
    }
}
