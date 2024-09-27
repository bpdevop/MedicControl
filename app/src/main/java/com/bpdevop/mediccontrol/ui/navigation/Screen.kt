package com.bpdevop.mediccontrol.ui.navigation

import androidx.annotation.StringRes
import com.bpdevop.mediccontrol.R

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    data object Patients : Screen("patients", R.string.menu_patients)
    data object AddPatient : Screen("add_patient", R.string.menu_add_patient)
    data object PatientDetail : Screen("patient_detail", R.string.menu_patient_detail) {
        fun withArgs(patientId: String): String = "$route/$patientId"
    }

    data object Vaccination : Screen("vaccination_screen", R.string.menu_vaccination)

    data object EditVaccination : Screen("edit_vaccine_screen", R.string.menu_edit_vaccination) {
        fun withArgs(patientId: String, vaccine: String): String = "$route/$patientId/$vaccine"
    }

    data object Allergy : Screen("allergy_screen", R.string.menu_allergy)

    data object EditAllergy : Screen("edit_allergy_screen", R.string.menu_edit_allergy) {
        fun withArgs(patientId: String, allergy: String): String = "$route/$patientId/$allergy"
    }

    data object Agenda : Screen("agenda", R.string.menu_agenda)
    data object Profile : Screen("profile", R.string.menu_profile)
    // Añade más pantallas si es necesario
}