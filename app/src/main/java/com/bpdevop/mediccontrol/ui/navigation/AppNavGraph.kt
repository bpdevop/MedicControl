package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.ui.screens.AddPatientScreen
import com.bpdevop.mediccontrol.ui.screens.AgendaScreen
import com.bpdevop.mediccontrol.ui.screens.vaccination.EditVaccineScreen
import com.bpdevop.mediccontrol.ui.screens.PatientDetailScreen
import com.bpdevop.mediccontrol.ui.screens.PatientOptionsScreen
import com.bpdevop.mediccontrol.ui.screens.PatientsScreen
import com.bpdevop.mediccontrol.ui.screens.ProfileScreen
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

        //Opciones del paciente
        composable("patient_options/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            PatientOptionsScreen(patientId = patientId, onOptionSelected = { route ->
                navController.navigate(route)
            })
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

        //Pantalla para agregar paciente
        composable(Screen.AddPatient.route) {
            AddPatientScreen(
                onPatientAdded = {
                    navController.popBackStack()
                }
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
