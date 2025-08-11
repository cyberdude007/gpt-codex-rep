package com.splitpaisa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.splitpaisa.data.seed.SeedRepository
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
    repository: SeedRepository,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Destinations.Home.route, modifier = modifier) {
        composable(Destinations.Home.route) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
            HomeScreen(vm, settingsViewModel)
        }
        composable(Destinations.Parties.route) {
            val vm: PartiesViewModel = viewModel(factory = PartiesViewModel.factory(repository))
            PartiesScreen(vm)
        }
        composable(Destinations.Stats.route) {
            val vm: StatsViewModel = viewModel(factory = StatsViewModel.factory(repository))
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
