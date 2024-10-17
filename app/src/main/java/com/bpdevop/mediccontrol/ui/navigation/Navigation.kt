package com.bpdevop.mediccontrol.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import com.bpdevop.mediccontrol.R

// Definición de los ítems de navegación como clases selladas
sealed class NavigationItem(val route: String, val titleResId: Int, val icon: ImageVector) {
    data object Patients : NavigationItem("patients", R.string.menu_patients, Icons.Default.People)
    data object Agenda : NavigationItem("agenda", R.string.menu_agenda, Icons.Default.Event)
    data object Profile : NavigationItem("profile", R.string.menu_profile, Icons.Default.Person)
    data object ClinicData : NavigationItem("clinic_data", R.string.menu_clinic_data, Icons.Default.LocalHospital)
    data object Settings : NavigationItem("settings", R.string.menu_settings, Icons.Default.Settings)
    data object About : NavigationItem("about", R.string.menu_about, Icons.Default.Info)
    data object Export : NavigationItem("export", R.string.menu_export, Icons.Default.Upload)
}


// Lista de ítems principales del menú
val mainMenuItems = listOf(
    NavigationItem.Patients,
    NavigationItem.Agenda,
    NavigationItem.Profile,
    //NavigationItem.ClinicData
    NavigationItem.Export
)

// Lista de ítems secundarios del menú
val secondaryMenuItems = listOf(
    NavigationItem.Settings,
    NavigationItem.About
)