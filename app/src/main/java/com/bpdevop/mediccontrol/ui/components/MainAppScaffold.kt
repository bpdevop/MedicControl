package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.navigateToLoginActivity
import com.bpdevop.mediccontrol.ui.navigation.AppNavGraph
import com.bpdevop.mediccontrol.ui.navigation.NavigationItem
import com.bpdevop.mediccontrol.ui.navigation.Screen
import com.bpdevop.mediccontrol.ui.viewmodels.MainViewModel
import com.bpdevop.mediccontrol.ui.viewmodels.UserSessionViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainViewModel: MainViewModel = hiltViewModel()
    val sessionViewModel: UserSessionViewModel = hiltViewModel()

    val selectedItem by mainViewModel.selectedItem.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val (showBackArrow, topBarTitle) = rememberTopBarState(currentRoute, selectedItem)

    LaunchedEffect(navBackStackEntry) {
        val currentMenuItem = mainViewModel.getMenuItemByRoute(currentRoute)
        mainViewModel.selectItem(currentMenuItem ?: return@LaunchedEffect)
    }

    HandleLogoutDialog(
        showLogoutDialog = showLogoutDialog,
        onLogout = {
            sessionViewModel.signOut()
            context.navigateToLoginActivity()
            showLogoutDialog = false
        },
        onDismissLogoutDialog = { showLogoutDialog = false }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(
                selectedItem = selectedItem,
                onItemClicked = { item ->
                    scope.launch { navigateToItem(navController, item.route, drawerState) }
                },
                userName = "Usuario"
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    showBackArrow = showBackArrow,
                    topBarTitle = topBarTitle,
                    onBackClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    showMenu = showMenu,
                    onMenuToggle = { showMenu = !showMenu },
                    onLogoutClick = { showLogoutDialog = true }
                )
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun rememberTopBarState(currentRoute: String?, selectedItem: NavigationItem?): Pair<Boolean?, String> {
    val showBackArrow = when (currentRoute) {
        null -> null
        Screen.Patients.route,
        Screen.Agenda.route,
        Screen.Profile.route,
        -> false

        else -> true
    }

    val topBarTitle = when {
        currentRoute == Screen.Patients.route -> stringResource(id = Screen.Patients.titleResId)
        currentRoute == Screen.AddPatient.route -> stringResource(id = Screen.AddPatient.titleResId)
        currentRoute?.startsWith(Screen.PatientDetail.route) == true -> stringResource(id = Screen.PatientDetail.titleResId)
        currentRoute == Screen.Agenda.route -> stringResource(id = Screen.Agenda.titleResId)
        currentRoute == Screen.Profile.route -> stringResource(id = Screen.Profile.titleResId)
        currentRoute?.startsWith(Screen.Vaccination.route) == true -> stringResource(id = Screen.Vaccination.titleResId)
        currentRoute?.startsWith(Screen.EditVaccination.route) == true -> stringResource(id = Screen.EditVaccination.titleResId)
        currentRoute?.startsWith(Screen.Allergy.route) == true -> stringResource(id = Screen.Allergy.titleResId)
        currentRoute?.startsWith(Screen.EditAllergy.route) == true -> stringResource(id = Screen.EditAllergy.titleResId)
        currentRoute?.startsWith(Screen.BloodPressure.route) == true -> stringResource(id = Screen.BloodPressure.titleResId)
        currentRoute?.startsWith(Screen.EditBloodPressure.route) == true -> stringResource(id = Screen.EditBloodPressure.titleResId)
        currentRoute?.startsWith(Screen.BloodGlucose.route) == true -> stringResource(id = Screen.BloodGlucose.titleResId)
        currentRoute?.startsWith(Screen.EditBloodGlucose.route) == true -> stringResource(id = Screen.EditBloodGlucose.titleResId)
        currentRoute?.startsWith(Screen.OxygenSaturation.route) == true -> stringResource(id = Screen.OxygenSaturation.titleResId)
        currentRoute?.startsWith(Screen.EditOxygenSaturation.route) == true -> stringResource(id = Screen.EditOxygenSaturation.titleResId)
        currentRoute?.startsWith(Screen.Examination.route) == true -> stringResource(id = Screen.Examination.titleResId)
        currentRoute?.startsWith(Screen.EditExamination.route) == true -> stringResource(id = Screen.EditExamination.titleResId)
        currentRoute?.startsWith(Screen.Prescription.route) == true -> stringResource(id = Screen.Prescription.titleResId)
        currentRoute?.startsWith(Screen.EditPrescription.route) == true -> stringResource(id = Screen.EditPrescription.titleResId)
        else -> selectedItem?.let { stringResource(id = it.titleResId) } ?: ""
    }

    return showBackArrow to topBarTitle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    showBackArrow: Boolean?,
    topBarTitle: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    // Barra superior utilizando Material Design 3
    TopAppBar(
        title = { Text(text = topBarTitle) },
        navigationIcon = {
            val (icon, description, onClickAction) = when (showBackArrow) {
                true -> Triple(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBackClick)
                false, null -> Triple(Icons.Default.Menu, "Menu", onMenuClick)
            }

            IconButton(onClick = onClickAction) {
                Icon(icon, contentDescription = description)
            }
        },
        actions = {
            IconButton(onClick = onMenuToggle) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onMenuToggle
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.action_logout)) },
                    onClick = {
                        onMenuToggle()
                        onLogoutClick()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun HandleLogoutDialog(showLogoutDialog: Boolean, onLogout: () -> Unit, onDismissLogoutDialog: () -> Unit) {
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = onDismissLogoutDialog,
            title = { Text(stringResource(id = R.string.action_logout)) },
            text = { Text(stringResource(id = R.string.global_message_logout)) },
            confirmButton = {
                TextButton(onClick = onLogout) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissLogoutDialog) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

private suspend fun navigateToItem(navController: NavHostController, route: String, drawerState: DrawerState) {
    drawerState.close()
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}