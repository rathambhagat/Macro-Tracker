package com.bunkmarte.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bunkmarte.ui.screens.DailyScreen
import com.bunkmarte.ui.screens.SummaryScreen

/**
 * Navigation graph for BunkMarte.
 * Two destinations: daily (home) and summary.
 */
@Composable
fun BunkMarteNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "daily"
    ) {
        composable("daily") {
            DailyScreen(navController = navController)
        }
        composable("summary") {
            SummaryScreen(navController = navController)
        }
    }
}
