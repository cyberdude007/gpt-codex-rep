package com.splitpaisa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.splitpaisa.feature.transactions.FilteredTransactionsScreen
import com.splitpaisa.feature.transactions.FilteredTransactionsViewModel
import com.splitpaisa.data.repo.TxFilter

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
            StatsScreen(vm) { filter ->
                val category = filter.categoryId ?: ""
                navController.navigate("${Destinations.Filtered.route}?start=${filter.start}&end=${filter.end}&categoryId=${category}")
            }
        }
        composable(
            route = Destinations.Filtered.route + "?start={start}&end={end}&categoryId={categoryId}",
            arguments = listOf(
                navArgument("start") { type = NavType.LongType },
                navArgument("end") { type = NavType.LongType },
                navArgument("categoryId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val start = backStackEntry.arguments?.getLong("start") ?: 0L
            val end = backStackEntry.arguments?.getLong("end") ?: 0L
            val cat = backStackEntry.arguments?.getString("categoryId").takeIf { !it.isNullOrEmpty() }
            val context = LocalContext.current
            val filter = TxFilter(start, end, cat)
            val vm: FilteredTransactionsViewModel = viewModel(factory = FilteredTransactionsViewModel.factory(context, filter))
            FilteredTransactionsScreen(vm)
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
    object Filtered : Destinations("transactions")
}
