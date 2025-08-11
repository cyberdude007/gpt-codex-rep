package com.splitpaisa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.splitpaisa.core.ui.PaisaSplitTheme
import com.splitpaisa.data.seed.SeedRepository
import com.splitpaisa.feature.settings.SettingsViewModel
import com.splitpaisa.feature.settings.ThemeMode
import com.splitpaisa.navigation.Destinations
import com.splitpaisa.navigation.PaisaNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SeedRepository(this)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()
            val darkTheme = when (settingsState.theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            PaisaSplitTheme(darkTheme) {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomBar(navController) },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { navController.navigate(Destinations.Add.route) }) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                        }
                    }
                ) { inner ->
                    PaisaNavGraph(
                        navController = navController,
                        repository = repository,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.padding(inner)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(Destinations.Home.route, Icons.Filled.Home, R.string.home),
        BottomNavItem(Destinations.Parties.route, Icons.Filled.Group, R.string.parties),
        BottomNavItem(Destinations.Stats.route, Icons.Filled.BarChart, R.string.stats),
        BottomNavItem(Destinations.Settings.route, Icons.Filled.Settings, R.string.settings)
    )
    NavigationBar {
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                label = { Text(stringResource(item.label)) }
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: Int)