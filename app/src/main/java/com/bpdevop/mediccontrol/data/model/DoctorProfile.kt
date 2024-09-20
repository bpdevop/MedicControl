package com.bpdevop.mediccontrol.data.model

data class DoctorProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val registrationNumber: String = "", // Número de colegiado
    val phoneNumber: String = "", // Número de teléfono
    val photoUrl: String? = null // URL de la foto de perfil
)
