package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.ui.screens.AddPatientScreen
import com.bpdevop.mediccontrol.ui.screens.AgendaScreen
import com.bpdevop.mediccontrol.ui.screens.PatientDetailScreen
import com.bpdevop.mediccontrol.ui.screens.PatientOptionsScreen
import com.bpdevop.mediccontrol.ui.screens.PatientsScreen
import com.bpdevop.mediccontrol.ui.screens.ProfileScreen
import com.bpdevop.mediccontrol.ui.screens.allergy.AllergyScreen
import com.bpdevop.mediccontrol.ui.screens.allergy.EditAllergyScreen
import com.bpdevop.mediccontrol.ui.screens.bloodpressure.BloodPressureScreen
import com.bpdevop.mediccontrol.ui.screens.bloodpressure.EditBloodPressureScreen
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

        composable(Screen.Agenda.route) {
            AgendaScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
    }
}
