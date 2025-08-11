package com.splitpaisa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import com.splitpaisa.feature.add.AddScreen
import com.splitpaisa.feature.add.AddViewModel
import com.splitpaisa.feature.home.HomeScreen
import com.splitpaisa.feature.home.HomeViewModel
import com.splitpaisa.feature.parties.PartiesScreen
import com.splitpaisa.feature.parties.PartiesViewModel
import com.splitpaisa.feature.settings.SettingsScreen
import com.splitpaisa.feature.settings.SettingsViewModel
import com.splitpaisa.feature.stats.StatsScreen
import com.splitpaisa.feature.stats.StatsViewModel

@Composable
fun PaisaNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Destinations.Home.route, modifier = modifier) {
        composable(Destinations.Home.route) {
            val context = LocalContext.current
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))
            HomeScreen(vm, settingsViewModel)
        }
        composable(Destinations.Parties.route) {
            val context = LocalContext.current
            val vm: PartiesViewModel = viewModel(factory = PartiesViewModel.factory(context))
            PartiesScreen(vm)
        }
        composable(Destinations.Stats.route) {
            val context = LocalContext.current
            val vm: StatsViewModel = viewModel(factory = StatsViewModel.factory(context))
            StatsScreen(vm)
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(settingsViewModel)
        }
        composable(Destinations.Add.route) {
            val vm: AddViewModel = viewModel()
            AddScreen(vm) { navController.popBackStack() }
        }
    }
}

sealed class Destinations(val route: String) {
    object Home : Destinations("home")
    object Parties : Destinations("parties")
    object Stats : Destinations("stats")
    object Settings : Destinations("settings")
    object Add : Destinations("add")
}
