package com.example.androidreminderapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidreminderapp.ui.auth.AuthSessionState
import com.example.androidreminderapp.ui.auth.AuthViewModel
import com.example.androidreminderapp.ui.auth.LoginScreen
import com.example.androidreminderapp.ui.home.HomeScreen
import com.example.androidreminderapp.ui.home.HomeViewModel
import com.example.androidreminderapp.ui.navigation.NotifyBottomBar
import com.example.androidreminderapp.ui.navigation.ProfileScreen
import com.example.androidreminderapp.ui.navigation.ReminderScreen
import com.example.androidreminderapp.ui.navigation.ReportsScreen
import com.example.androidreminderapp.ui.navigation.SettingsScreen
import com.example.androidreminderapp.ui.navigation.ShellFeatureScreen
import com.example.androidreminderapp.ui.navigation.SleepScreen
import com.example.androidreminderapp.ui.navigation.SoundsScreen
import com.example.androidreminderapp.ui.navigation.TabsScreen
import com.example.androidreminderapp.ui.reminder.AddEditReminderScreen
import com.example.androidreminderapp.ui.theme.AndroidReminderAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        setContent {
            AndroidReminderAppTheme {
                val viewModel: AuthViewModel = hiltViewModel()
                val sessionState by viewModel.sessionState.collectAsState()
                val navController = rememberNavController()
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (!granted) {
                        android.util.Log.w(
                            "MainActivity",
                            "Notification permission was denied; reminders will not appear in the notification shade."
                        )
                    }
                }

                LaunchedEffect(sessionState) {
                    if (
                        sessionState is AuthSessionState.Authenticated &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                LaunchedEffect(sessionState) {
                    when (sessionState) {
                        AuthSessionState.Authenticated -> {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        AuthSessionState.Unauthenticated -> {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        AuthSessionState.Loading -> {
                            // Stay on loading / splash
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (sessionState) {
                        AuthSessionState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            val startDest = if (sessionState is AuthSessionState.Authenticated) "home" else "login"
                            val currentRoute by navController.currentBackStackEntryAsState()
                            val route = currentRoute?.destination?.route
                            val topLevelRoutes = setOf(
                                "home", "reminder", "tabs", "settings", "profile",
                                "sleep", "sounds", "reports", "themes"
                            )

                            Scaffold(
                                bottomBar = {
                                    if (route != null && route in topLevelRoutes) {
                                        NotifyBottomBar(
                                            selectedRoute = route.orEmpty(),
                                            onNavigate = { destination ->
                                                navController.navigate(destination) {
                                                    popUpTo("home") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            ) { contentPadding ->
                                NavHost(
                                    navController = navController,
                                    startDestination = startDest,
                                    modifier = Modifier.padding(contentPadding)
                                ) {
                                    composable("login") {
                                        LoginScreen(
                                            onLoginSuccess = {
                                                navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            }
                                        )
                                    }

                                    composable("home") {
                                        HomeScreen(
                                            viewModel = hiltViewModel<HomeViewModel>(),
                                            onAddReminder = {
                                                navController.navigate("add_reminder")
                                            },
                                            onEditReminder = { reminderId ->
                                                navController.navigate("add_reminder/$reminderId")
                                            },
                                            onOpenReminders = { navController.navigate("reminder") },
                                            onOpenProfile = { navController.navigate("profile") },
                                            onOpenSettings = { navController.navigate("settings") }
                                        )
                                    }

                                    composable("reminder") {
                                        ReminderScreen(
                                            viewModel = hiltViewModel<HomeViewModel>(),
                                            onAddReminder = { navController.navigate("add_reminder") },
                                            onEditReminder = { reminderId ->
                                                navController.navigate("add_reminder/$reminderId")
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }

                                    composable("tabs") {
                                        TabsScreen(onNavigate = { destination ->
                                            navController.navigate(destination)
                                        })
                                    }

                                    composable("settings") {
                                        SettingsScreen(onBack = { navController.popBackStack() })
                                    }
                                    composable("profile") {
                                        ProfileScreen(
                                            displayInfo = viewModel.userDisplayInfo,
                                            onLogout = { viewModel.signOut() },
                                            onNavigate = { destination ->
                                                navController.navigate(destination)
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                    composable("sleep") { SleepScreen() }
                                    composable("sounds") { SoundsScreen() }
                                    composable("reports") {
                                        ReportsScreen(viewModel = hiltViewModel<HomeViewModel>())
                                    }
                                    composable("themes") {
                                        ShellFeatureScreen(
                                            title = "Themes",
                                            description = "Notify's purple night theme is active.",
                                            actionLabel = "Keep current theme",
                                            onAction = {}
                                        )
                                    }
                                    composable("edit_profile") {
                                        ShellFeatureScreen(
                                            title = "Edit Profile",
                                            description = "Your account details are managed by your sign-in provider.",
                                            actionLabel = "Done",
                                            onAction = { navController.popBackStack() }
                                        )
                                    }

                                    composable("add_reminder") {
                                        AddEditReminderScreen(
                                            reminderId = null,
                                            viewModel = hiltViewModel(),
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }

                                    composable(
                                        route = "add_reminder/{reminderId}",
                                        arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
                                    ) { backStackEntry ->
                                        val reminderId = backStackEntry.arguments?.getLong("reminderId")
                                        AddEditReminderScreen(
                                            reminderId = reminderId,
                                            viewModel = hiltViewModel(),
                                            onNavigateBack = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
