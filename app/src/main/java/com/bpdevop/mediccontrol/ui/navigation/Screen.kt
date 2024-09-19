package com.bpdevop.mediccontrol.ui.navigation

import androidx.annotation.StringRes
import com.bpdevop.mediccontrol.R

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    data object Patients : Screen("patients", R.string.menu_patients)
    data object AddPatient : Screen("add_patient", R.string.menu_add_patient)
    data object PatientDetail : Screen("patient_detail", R.string.menu_patient_detail) {
        fun withArgs(patientId: String): String = "$route/$patientId"
    }
    data object Agenda : Screen("agenda", R.string.menu_agenda)
    data object Profile : Screen("profile", R.string.menu_profile)
    // Añade más pantallas si es necesario
}