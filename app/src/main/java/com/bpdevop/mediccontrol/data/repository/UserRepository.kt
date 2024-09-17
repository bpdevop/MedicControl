package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun getUserData(): UiState<User> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return UiState.Error("Usuario no autenticado")

            val documentSnapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val user = documentSnapshot.toObject(User::class.java)
                ?: return UiState.Error("Usuario no encontrado")

            UiState.Success(user)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error al obtener los datos del usuario")
        }
    }
}