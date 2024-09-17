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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bpdevop.mediccontrol.ui.navigation.AppNavGraph
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

    val (showBackArrow, topBarTitle) = rememberTopBarState(currentRoute)

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
                modifier = Modifier.padding(innerPadding),
                startDestination = "patients"
            )
        }
    }
}


@Composable
fun rememberTopBarState(currentRoute: String?): Pair<Boolean, String> {
    // Determina si la flecha de volver atrás debe ser visible
    val showBackArrow = when (currentRoute) {
        "patients", "agenda" -> false  // Pantallas principales, no necesitan flecha de "volver"
        else -> true  // Otras pantallas necesitan flecha de "volver"
    }

    // Determina el título que se debe mostrar en el TopBar
    val topBarTitle = when (currentRoute) {
        "patients" -> "Pacientes"
        "agenda" -> "Agenda"
        "profile" -> "Perfil del Médico"
        "clinicData" -> "Datos de la Clínica"
        "printingConfig" -> "Configuración de Impresión"
        "exportData" -> "Exportaciones"
        "appSettings" -> "Configuración de la App"
        else -> "MedicControl"  // Título por defecto para pantallas desconocidas
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