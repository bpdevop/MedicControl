package com.bpdevop.mediccontrol.ui.navigation

import androidx.annotation.StringRes
import com.bpdevop.mediccontrol.R

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    data object Patients : Screen("patients", R.string.menu_patients)
    data object AddPatient : Screen("add_patient", R.string.menu_add_patient)
    data object PatientDetail : Screen("patient_detail", R.string.menu_patient_detail) {
        fun withArgs(patientId: String): String = "$route/$patientId"
    }

    data object MedicalHistory : Screen("medical_history_screen", R.string.menu_medical_history)

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

    // Pantalla para nueva receta
    data object Prescription : Screen("prescription_screen", R.string.menu_prescription)

    // Pantalla para editar receta
    data object EditPrescription : Screen("edit_prescription_screen", R.string.menu_edit_prescription) {
        fun withArgs(patientId: String, prescriptionId: String): String = "$route/$patientId/$prescriptionId"
    }

    //Pantalla para laboratorios
    data object Laboratory : Screen("laboratory_screen", R.string.menu_laboratory)

    //Pantalla para editar laboratorios
    data object EditLaboratory : Screen("edit_laboratory_screen", R.string.menu_edit_laboratory) {
        fun withArgs(patientId: String, laboratoryId: String): String = "$route/$patientId/$laboratoryId"
    }

    // Pantalla para nuevo registro de radiología
    data object Radiology : Screen("radiology_screen", R.string.menu_radiology)

    // Pantalla para editar registro de radiología
    data object EditRadiology : Screen("edit_radiology_screen", R.string.menu_edit_radiology) {
        fun withArgs(patientId: String, radiologyId: String): String = "$route/$patientId/$radiologyId"
    }

    // Pantalla para gestionar las citas de un paciente
    data object Appointment : Screen("appointment_screen", R.string.menu_appointment)

    // Pantalla para editar una cita específica de un paciente
    data object EditAppointment : Screen("edit_appointment_screen", R.string.menu_edit_appointment) {
        fun withArgs(patientId: String, appointmentId: String): String = "$route/$patientId/$appointmentId"
    }

    data object Agenda : Screen("agenda", R.string.menu_agenda)
    data object Profile : Screen("profile", R.string.menu_profile)
    data object Export : Screen("export", R.string.menu_export)

    data object Settings : Screen("settings", R.string.menu_settings)
    data object About : Screen("about", R.string.menu_about)
}