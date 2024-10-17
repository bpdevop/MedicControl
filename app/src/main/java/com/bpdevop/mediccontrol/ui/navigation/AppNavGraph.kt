package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.data.model.PatientAppointment
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.ui.screens.AboutScreen
import com.bpdevop.mediccontrol.ui.screens.AddPatientScreen
import com.bpdevop.mediccontrol.ui.screens.AgendaScreen
import com.bpdevop.mediccontrol.ui.screens.InfectiousPatientsExportScreen
import com.bpdevop.mediccontrol.ui.screens.PatientDetailScreen
import com.bpdevop.mediccontrol.ui.screens.PatientOptionsScreen
import com.bpdevop.mediccontrol.ui.screens.PatientsScreen
import com.bpdevop.mediccontrol.ui.screens.ProfileScreen
import com.bpdevop.mediccontrol.ui.screens.SettingsScreen
import com.bpdevop.mediccontrol.ui.screens.allergy.AllergyScreen
import com.bpdevop.mediccontrol.ui.screens.allergy.EditAllergyScreen
import com.bpdevop.mediccontrol.ui.screens.appointment.AppointmentScreen
import com.bpdevop.mediccontrol.ui.screens.appointment.EditAppointmentScreen
import com.bpdevop.mediccontrol.ui.screens.bloodglucose.BloodGlucoseScreen
import com.bpdevop.mediccontrol.ui.screens.bloodglucose.EditBloodGlucoseScreen
import com.bpdevop.mediccontrol.ui.screens.bloodpressure.BloodPressureScreen
import com.bpdevop.mediccontrol.ui.screens.bloodpressure.EditBloodPressureScreen
import com.bpdevop.mediccontrol.ui.screens.examination.EditExaminationScreen
import com.bpdevop.mediccontrol.ui.screens.examination.ExaminationScreen
import com.bpdevop.mediccontrol.ui.screens.laboratory.EditLaboratoryScreen
import com.bpdevop.mediccontrol.ui.screens.laboratory.LaboratoryScreen
import com.bpdevop.mediccontrol.ui.screens.oxygensaturation.EditOxygenSaturationScreen
import com.bpdevop.mediccontrol.ui.screens.oxygensaturation.OxygenSaturationScreen
import com.bpdevop.mediccontrol.ui.screens.prescription.EditPrescriptionScreen
import com.bpdevop.mediccontrol.ui.screens.prescription.PrescriptionScreen
import com.bpdevop.mediccontrol.ui.screens.radiology.EditRadiologyScreen
import com.bpdevop.mediccontrol.ui.screens.radiology.RadiologyScreen
import com.bpdevop.mediccontrol.ui.screens.vaccination.EditVaccineScreen
import com.bpdevop.mediccontrol.ui.screens.vaccination.VaccinationScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Patients,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        // Pantalla de pacientes
        composable(Screen.Patients.route) {
            PatientsScreen(
                onPatientClick = { patientId ->
                    navController.navigate("patient_options/$patientId")
                },
                onAddPatientClick = {
                    navController.navigate(Screen.AddPatient.route)
                },
                onDetailClick = { patientId ->
                    navController.navigate(Screen.PatientDetail.withArgs(patientId))
                }
            )
        }

        //Pantalla para agregar paciente
        composable(Screen.AddPatient.route) {
            AddPatientScreen(
                onPatientAdded = {
                    navController.popBackStack()
                }
            )
        }

        //Detalle del paciente
        composable(
            route = "${Screen.PatientDetail.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType }),

            ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                PatientDetailScreen(
                    patientId = it,
                    onPatientUpdated = {
                        navController.popBackStack()
                    },
                    onPatientDeleted = {
                        navController.popBackStack()
                    }
                )
            }
        }

        //Opciones del paciente
        composable("patient_options/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            PatientOptionsScreen(patientId = patientId, onOptionSelected = { route ->
                navController.navigate(route)
            })
        }

        // Pantalla de Vacunas
        composable(
            route = "${Screen.Vaccination.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                VaccinationScreen(
                    patientId = it, onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditVaccine = { vaccine ->
                        val vaccineJson = Json.encodeToString(vaccine)
                        navController.navigate(Screen.EditVaccination.withArgs(patientId, vaccineJson))
                    }

                )
            }
        }

        //Pantalla de Edición de Vacunas
        composable(
            route = "${Screen.EditVaccination.route}/{patientId}/{vaccine}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("vaccine") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val vaccineJson = backStackEntry.arguments?.getString("vaccine")
            val vaccine = Json.decodeFromString<Vaccine>(vaccineJson!!)

            EditVaccineScreen(
                patientId = patientId,
                vaccine = vaccine,
                onVaccineUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Alergias
        composable(
            route = "${Screen.Allergy.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                AllergyScreen(
                    patientId = it, onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditAllergy = { allergy ->
                        val allergyJson = Json.encodeToString(allergy)
                        navController.navigate(Screen.EditAllergy.withArgs(patientId, allergyJson))
                    }
                )
            }
        }

        // Pantalla de Edición de Alergias
        composable(
            route = "${Screen.EditAllergy.route}/{patientId}/{allergy}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("allergy") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val allergyJson = backStackEntry.arguments?.getString("allergy")
            val allergy = Json.decodeFromString<Allergy>(allergyJson!!)

            EditAllergyScreen(
                patientId = patientId,
                allergy = allergy,
                onAllergyUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla para presión arterial
        composable(
            route = "${Screen.BloodPressure.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                BloodPressureScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditBloodPressure = { bloodPressure ->
                        val bloodPressureJson = Json.encodeToString(bloodPressure)
                        navController.navigate(Screen.EditBloodPressure.withArgs(patientId, bloodPressureJson))
                    }
                )
            }
        }

        // Pantalla de edición de presión arterial
        composable(
            route = "${Screen.EditBloodPressure.route}/{patientId}/{bloodPressure}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("bloodPressure") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val bloodPressureJson = backStackEntry.arguments?.getString("bloodPressure")
            val bloodPressure = Json.decodeFromString<BloodPressure>(bloodPressureJson!!)

            EditBloodPressureScreen(
                patientId = patientId,
                bloodPressure = bloodPressure,
                onBloodPressureUpdated = { navController.popBackStack() }
            )
        }

        //Pantalla de Glicemia
        composable(
            route = "${Screen.BloodGlucose.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                BloodGlucoseScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditBloodGlucose = { bloodGlucose ->
                        val bloodGlucoseJson = Json.encodeToString(bloodGlucose)
                        navController.navigate(Screen.EditBloodGlucose.withArgs(patientId, bloodGlucoseJson))
                    }
                )
            }
        }

        //Pantalla de edición de Glicemia
        composable(
            route = "${Screen.EditBloodGlucose.route}/{patientId}/{bloodGlucose}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("bloodGlucose") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val bloodGlucoseJson = backStackEntry.arguments?.getString("bloodGlucose")
            val bloodGlucose = Json.decodeFromString<BloodGlucose>(bloodGlucoseJson!!)

            EditBloodGlucoseScreen(
                patientId = patientId,
                bloodGlucose = bloodGlucose,
                onBloodGlucoseUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Saturación de Oxígeno
        composable(
            route = "${Screen.OxygenSaturation.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                OxygenSaturationScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditOxygenSaturation = { oxygenSaturation ->
                        val oxygenSaturationJson = Json.encodeToString(oxygenSaturation)
                        navController.navigate(Screen.EditOxygenSaturation.withArgs(patientId, oxygenSaturationJson))
                    }
                )
            }
        }

        // Pantalla de edición de Saturación de Oxígeno
        composable(
            route = "${Screen.EditOxygenSaturation.route}/{patientId}/{oxygenSaturation}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("oxygenSaturation") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val oxygenSaturationJson = backStackEntry.arguments?.getString("oxygenSaturation")
            val oxygenSaturation = Json.decodeFromString<OxygenSaturation>(oxygenSaturationJson!!)

            EditOxygenSaturationScreen(
                patientId = patientId,
                oxygenSaturation = oxygenSaturation,
                onOxygenSaturationUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Examen
        composable(
            route = "${Screen.Examination.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                ExaminationScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditExamination = { examinationId ->
                        navController.navigate(Screen.EditExamination.withArgs(patientId, examinationId))
                    }
                )
            }
        }

        // Pantalla de edición de Examen
        composable(
            route = "${Screen.EditExamination.route}/{patientId}/{examination}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("examination") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val examinationId = backStackEntry.arguments?.getString("examination")!!

            EditExaminationScreen(
                patientId = patientId,
                examinationId = examinationId,
                onExaminationUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Receta
        composable(
            route = "${Screen.Prescription.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                PrescriptionScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditPrescription = { prescriptionId ->
                        navController.navigate(Screen.EditPrescription.withArgs(patientId, prescriptionId))
                    }
                )
            }
        }

        // Pantalla de edición de Receta
        composable(
            route = "${Screen.EditPrescription.route}/{patientId}/{prescriptionId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("prescriptionId") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val prescriptionId = backStackEntry.arguments?.getString("prescriptionId")!!

            EditPrescriptionScreen(
                patientId = patientId,
                prescriptionId = prescriptionId,
                onPrescriptionUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Laboratorio
        composable(
            route = "${Screen.Laboratory.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                LaboratoryScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditLabRecord = { laboratoryId ->
                        navController.navigate(Screen.EditLaboratory.withArgs(patientId, laboratoryId))
                    }
                )
            }
        }

        // Pantalla de edición de Laboratorio
        composable(
            route = "${Screen.EditLaboratory.route}/{patientId}/{laboratoryId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("laboratoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val labRecordId = backStackEntry.arguments?.getString("laboratoryId")!!

            EditLaboratoryScreen(
                patientId = patientId,
                laboratoryId = labRecordId,
                onLaboratoryUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Radiología
        composable(
            route = "${Screen.Radiology.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                RadiologyScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditRadiologyRecord = { radiologyId ->
                        navController.navigate(Screen.EditRadiology.withArgs(patientId, radiologyId))
                    }
                )
            }
        }

        // Pantalla de edición de Radiología
        composable(
            route = "${Screen.EditRadiology.route}/{patientId}/{radiologyId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("radiologyId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val radiologyRecordId = backStackEntry.arguments?.getString("radiologyId")!!

            EditRadiologyScreen(
                patientId = patientId,
                radiologyId = radiologyRecordId,
                onRadiologyUpdated = { navController.popBackStack() }
            )
        }

        // Pantalla de Asignación de Citas
        composable(
            route = "${Screen.Appointment.route}/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                AppointmentScreen(
                    patientId = it,
                    onSaveSuccess = {
                        navController.popBackStack()
                    },
                    onEditAppointment = { appointment ->
                        val appointmentJson = Json.encodeToString(appointment)
                        navController.navigate(Screen.EditAppointment.withArgs(patientId, appointmentJson))
                    }
                )
            }
        }

        // Pantalla de Edición de Citas
        composable(
            route = "${Screen.EditAppointment.route}/{patientId}/{appointment}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("appointment") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")!!
            val appointmentJson = backStackEntry.arguments?.getString("appointment")
            val appointment = Json.decodeFromString<PatientAppointment>(appointmentJson!!)

            EditAppointmentScreen(
                patientId = patientId,
                appointment = appointment,
                onAppointmentUpdated = { navController.popBackStack() }
            )
        }

        composable(Screen.Agenda.route) {
            AgendaScreen(
                onAppointmentClick = { _ ->
                }
            )
        }


        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(Screen.Export.route) {
            InfectiousPatientsExportScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}
