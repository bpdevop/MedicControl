package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bpdevop.mediccontrol.ui.screens.AddPatientScreen
import com.bpdevop.mediccontrol.ui.screens.AgendaScreen
import com.bpdevop.mediccontrol.ui.screens.PatientDetailScreen
import com.bpdevop.mediccontrol.ui.screens.PatientOptionsScreen
import com.bpdevop.mediccontrol.ui.screens.PatientsScreen
import com.bpdevop.mediccontrol.ui.screens.ProfileScreen
import com.bpdevop.mediccontrol.ui.screens.VaccinationScreen

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

        composable("patient_options/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            PatientOptionsScreen(patientId = patientId, onOptionSelected = { route ->
                navController.navigate(route)
            })
        }

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
        composable("vaccination_screen/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            patientId?.let {
                VaccinationScreen(
                    patientId = it, onSaveSuccess = { navController.popBackStack() })
            }
        }

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
