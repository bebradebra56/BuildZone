package com.buildzone.zonebu.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.buildzone.zonebu.presentation.screen.*

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val PROJECTS = "projects"
    const val ADD_PROJECT = "add_project"
    const val ROOMS = "rooms/{projectId}"
    const val ADD_ROOM = "add_room/{projectId}"
    const val ROOM_PLAN = "room_plan/{roomId}"
    const val ADD_MEASUREMENT = "add_measurement/{roomId}/{xInt}/{yInt}"
    const val EDIT_MEASUREMENT = "edit_measurement/{measurementId}"
    const val HEAT_MAP = "heat_map/{roomId}"
    const val PROBLEM_ZONES = "problem_zones/{roomId}"
    const val ZONE_DETAILS = "zone_details/{roomId}/{zoneType}"
    const val MEASUREMENTS_LIST = "measurements_list/{roomId}"
    const val HISTORY = "history"
    const val TIMELINE = "timeline/{roomId}"
    const val REPORTS = "reports"
    const val EXPORT = "export/{projectId}"
    const val COMPARISON = "comparison/{roomId}"
    const val TASKS = "tasks"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val ACTIVITY_HISTORY = "activity_history"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

enum class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    DASHBOARD(Routes.DASHBOARD, Icons.Filled.Home, "Dashboard"),
    PROJECTS(Routes.PROJECTS, Icons.Filled.AccountTree, "Projects"),
    REPORTS(Routes.REPORTS, Icons.Filled.BarChart, "Reports"),
    TASKS(Routes.TASKS, Icons.Filled.CheckBox, "Tasks"),
    SETTINGS(Routes.SETTINGS, Icons.Filled.Settings, "Settings")
}

private val noBottomBarRoutes = setOf(Routes.SPLASH, Routes.WELCOME, Routes.ONBOARDING)

@Composable
fun AppNavGraph(darkTheme: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && currentRoute !in noBottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentRoute?.startsWith(item.route.substringBefore("/{")) == true
                            || currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            enterTransition = { fadeIn() + slideInHorizontally { it / 5 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 5 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 5 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 5 } }
        ) {
            composable(Routes.SPLASH) { SplashScreen(navController) }
            composable(Routes.WELCOME) { WelcomeScreen(navController) }
            composable(Routes.ONBOARDING) { OnboardingScreen(navController) }
            composable(Routes.DASHBOARD) { DashboardScreen(navController) }
            composable(Routes.PROJECTS) { ProjectsScreen(navController) }
            composable(Routes.ADD_PROJECT) { AddProjectScreen(navController) }

            composable(
                Routes.ROOMS,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                val projectId = it.arguments!!.getLong("projectId")
                RoomsScreen(navController, projectId)
            }

            composable(
                Routes.ADD_ROOM,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                val projectId = it.arguments!!.getLong("projectId")
                AddRoomScreen(navController, projectId)
            }

            composable(
                Routes.ROOM_PLAN,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                RoomPlanScreen(navController, roomId)
            }

            composable(
                Routes.ADD_MEASUREMENT,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.LongType },
                    navArgument("xInt") { type = NavType.IntType },
                    navArgument("yInt") { type = NavType.IntType }
                )
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                val xRatio = it.arguments!!.getInt("xInt") / 1000f
                val yRatio = it.arguments!!.getInt("yInt") / 1000f
                AddMeasurementScreen(navController, roomId, xRatio, yRatio)
            }

            composable(
                Routes.EDIT_MEASUREMENT,
                arguments = listOf(navArgument("measurementId") { type = NavType.LongType })
            ) {
                val measurementId = it.arguments!!.getLong("measurementId")
                EditMeasurementScreen(navController, measurementId)
            }

            composable(
                Routes.HEAT_MAP,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                HeatMapScreen(navController, roomId)
            }

            composable(
                Routes.PROBLEM_ZONES,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                ProblemZonesScreen(navController, roomId)
            }

            composable(
                Routes.ZONE_DETAILS,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.LongType },
                    navArgument("zoneType") { type = NavType.StringType }
                )
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                val zoneType = it.arguments!!.getString("zoneType") ?: ""
                ZoneDetailsScreen(navController, roomId, zoneType)
            }

            composable(
                Routes.MEASUREMENTS_LIST,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                MeasurementsScreen(navController, roomId)
            }

            composable(Routes.HISTORY) { HistoryScreen(navController) }

            composable(
                Routes.TIMELINE,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                TimelineScreen(navController, roomId)
            }

            composable(Routes.REPORTS) { ReportsScreen(navController) }

            composable(
                Routes.EXPORT,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) {
                val projectId = it.arguments!!.getLong("projectId")
                ExportScreen(navController, projectId)
            }

            composable(
                Routes.COMPARISON,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType })
            ) {
                val roomId = it.arguments!!.getLong("roomId")
                ComparisonScreen(navController, roomId)
            }

            composable(Routes.TASKS) { TasksScreen(navController) }
            composable(Routes.ADD_TASK) { AddTaskScreen(navController) }

            composable(
                Routes.EDIT_TASK,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) {
                val taskId = it.arguments!!.getLong("taskId")
                EditTaskScreen(navController, taskId)
            }

            composable(Routes.ACTIVITY_HISTORY) { ActivityHistoryScreen(navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController) }
            composable(Routes.SETTINGS) { SettingsScreen(navController) }
        }
    }
}
