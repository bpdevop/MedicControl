package com.bpdevop.mediccontrol.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bpdevop.mediccontrol.ui.navigation.AppNavGraph
import com.bpdevop.mediccontrol.ui.navigation.NavigationItem
import com.bpdevop.mediccontrol.ui.navigation.Screen
import com.bpdevop.mediccontrol.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainViewModel: MainViewModel = hiltViewModel()

    val selectedItem by mainViewModel.selectedItem.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val (showBackArrow, topBarTitle) = rememberTopBarState(currentRoute, selectedItem)

    LaunchedEffect(navBackStackEntry) {
        val currentMenuItem = mainViewModel.getMenuItemByRoute(currentRoute)
        mainViewModel.selectItem(currentMenuItem ?: return@LaunchedEffect)
    }

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
fun rememberTopBarState(currentRoute: String?, selectedItem: NavigationItem?): Pair<Boolean, String> {
    val showBackArrow = when (currentRoute) {
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
        // currentRoute == Screen.ClinicData.route -> stringResource(id = Screen.ClinicData.titleResId)
        // currentRoute == Screen.PrintingConfig.route -> stringResource(id = Screen.PrintingConfig.titleResId)
        // currentRoute == Screen.ExportData.route -> stringResource(id = Screen.ExportData.titleResId)
        // currentRoute == Screen.AppSettings.route -> stringResource(id = Screen.AppSettings.titleResId)
        else -> selectedItem?.let { stringResource(id = it.titleResId) } ?: ""
    }


    return showBackArrow to topBarTitle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    showBackArrow: Boolean,
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
            // Mostrar la flecha de "volver atrás" o el ícono de menú
            if (showBackArrow) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                }
            }
        },
        actions = {
            // Icono de opciones adicionales (tres puntos)
            IconButton(onClick = onMenuToggle) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Más opciones")
            }

            // Menú desplegable de opciones adicionales
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onMenuToggle
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        onMenuToggle() // Cerrar el menú
                        onLogoutClick() // Lógica de cerrar sesión
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