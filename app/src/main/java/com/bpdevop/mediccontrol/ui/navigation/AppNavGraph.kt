package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bpdevop.mediccontrol.ui.screens.AddPatientScreen
import com.bpdevop.mediccontrol.ui.screens.AgendaScreen
import com.bpdevop.mediccontrol.ui.screens.PatientDetailScreen
import com.bpdevop.mediccontrol.ui.screens.PatientsScreen
import com.bpdevop.mediccontrol.ui.screens.ProfileScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "patients",
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantalla de pacientes
        composable("patients") {
            PatientsScreen(
                onPatientClick = { patientId ->
                    navController.navigate("patient_detail/$patientId")
                },
                onAddPatientClick = {
                    navController.navigate("add_patient")
                }
            )
        }

        composable("patient_detail/{patientId}") { backStackEntry ->
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

        // Pantalla para agregar un nuevo paciente
        composable("add_patient") {
            AddPatientScreen(
                onPatientAdded = {
                    navController.popBackStack()
                }
            )
        }


        // Pantalla de la agenda del médico
        composable("agenda") {
            AgendaScreen(navController)
        }

        // Pantalla del perfil del médico
        composable("profile") {
            ProfileScreen(navController)
        }

        // Agrega más pantallas según sea necesario
    }
}
