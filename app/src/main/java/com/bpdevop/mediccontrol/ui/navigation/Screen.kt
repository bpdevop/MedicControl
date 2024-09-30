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

    // Nuevas pantallas para Presión Arterial
    data object BloodPressure : Screen("blood_pressure_screen", R.string.menu_blood_pressure)

    data object EditBloodPressure : Screen("edit_blood_pressure_screen", R.string.menu_edit_blood_pressure) {
        fun withArgs(patientId: String, bloodPressure: String): String = "$route/$patientId/$bloodPressure"
    }

    // Nueva Pantalla para Glicemia
    data object BloodGlucose : Screen("blood_glucose_screen", R.string.menu_blood_glucose)

    data object EditBloodGlucose : Screen("edit_blood_glucose_screen", R.string.menu_edit_blood_glucose) {
        fun withArgs(patientId: String, bloodGlucose: String): String = "$route/$patientId/$bloodGlucose"
    }

    // Pantallas para Saturación de Oxígeno
    data object OxygenSaturation : Screen("oxygen_saturation_screen", R.string.menu_oxygen_saturation)

    data object EditOxygenSaturation : Screen("edit_oxygen_saturation_screen", R.string.menu_edit_oxygen_saturation) {
        fun withArgs(patientId: String, oxygenSaturation: String): String = "$route/$patientId/$oxygenSaturation"
    }

    // Pantalla para nuevo Examen
    data object Examination : Screen("examination_screen", R.string.menu_examination)

    // Pantalla para editar Examen
    data object EditExamination : Screen("edit_examination_screen", R.string.menu_edit_examination) {
        fun withArgs(patientId: String, examinationId: String): String = "$route/$patientId/$examinationId"
    }

    data object Agenda : Screen("agenda", R.string.menu_agenda)
    data object Profile : Screen("profile", R.string.menu_profile)
    // Añade más pantallas si es necesario
}