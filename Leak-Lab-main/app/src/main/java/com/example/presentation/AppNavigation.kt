package com.example.presentation

import android.app.AlertDialog
import android.view.View
import android.widget.PopupWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.screens.auth.LoginScreen
import com.example.presentation.screens.auth.RegisterScreen
import com.example.presentation.screens.auth.SplashScreen
import com.example.presentation.screens.dashboard.DashboardScreen
import com.example.presentation.screens.dashboard.HomeScreen
import com.example.presentation.screens.dashboard.ProfileScreen
import com.example.presentation.screens.dashboard.SettingsScreen
import com.example.presentation.screens.dashboard.UserDetailScreen
import com.example.presentation.screens.hardware.BluetoothScreen
import com.example.presentation.screens.hardware.CameraScreen
import com.example.presentation.screens.hardware.GalleryScreen
import com.example.presentation.screens.hardware.SearchScreen
import com.example.presentation.screens.tasks.BackgroundSyncScreen
import com.example.presentation.screens.tasks.ChatScreen
import com.example.presentation.screens.tasks.DownloadsScreen
import com.example.presentation.screens.tasks.NotificationsScreen
import com.example.presentation.screens.tasks.StatisticsScreen
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.TextGray

object NavigationLeaks {
    // Active dialog reference mapping for global overlay management
    var activeDialogRef: AlertDialog? = null

    // Active popup windows tracking registries
    val popupWindows = mutableListOf<PopupWindow>()

    // Companion reference to the master navigation view context
    var activeNavigationView: View? = null
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateHome = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateLogin = {
                    navController.navigate("login")
                },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainLayout(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(onLogout: () -> Unit) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf("home") }
    var activeSubView by rememberSaveable { mutableStateOf<String?>(null) }

    // Registers screen metrics and views with core diagnostics container
    DisposableEffect(Unit) {
        val view = View(context)
        NavigationLeaks.activeNavigationView = view
        onDispose {
            // Clear reference logic
        }
    }

    // Present active session dialog details
    val triggerDynamicAlert = {
        try {
            val builder = AlertDialog.Builder(context)
                .setTitle("Leak Lab Session")
                .setMessage("Deep system diagnostics are active in this workspace.")
                .setPositiveButton("Proceed") { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            NavigationLeaks.activeDialogRef = dialog
            dialog.show()
        } catch (e: Exception) {
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CosmicSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                val tabs = listOf(
                    TabSpec("home", "Dashboard", Icons.Filled.Home),
                    TabSpec("communications", "Chat", Icons.Filled.Send),
                    TabSpec("hardware", "Hardware", Icons.Filled.Refresh),
                    TabSpec("tasks", "Tasks", Icons.Filled.List)
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab.id,
                        onClick = {
                            currentTab = tab.id
                            activeSubView = null
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonRuby,
                            unselectedIconColor = TextGray,
                            selectedTextColor = NeonRuby,
                            unselectedTextColor = TextGray,
                            indicatorColor = NeonRuby.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.id}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MidnightBlack)
        ) {
            // Screen router
            if (activeSubView != null) {
                // Render subviews
                when (activeSubView) {
                    "dashboard" -> DashboardScreen()
                    "statistics" -> StatisticsScreen()
                    "bluetooth" -> BluetoothScreen()
                    "chat" -> ChatScreen()
                    "gallery" -> GalleryScreen()
                    "downloads" -> DownloadsScreen()
                    "search" -> SearchScreen()
                    "profile" -> ProfileScreen(onNavigateBack = { activeSubView = null; onLogout() })
                    "settings" -> SettingsScreen()
                    "user_detail" -> UserDetailScreen()
                    "notifications" -> NotificationsScreen()
                    "sync" -> BackgroundSyncScreen()
                }
            } else {
                // Render main categories
                when (currentTab) {
                    "home" -> HomeScreen(onNavigateTo = { activeSubView = it })
                    "communications" -> {
                        var comSelection by rememberSaveable { mutableStateOf("chat") }
                        Column {
                            TabRow(
                                selectedTabIndex = if (comSelection == "chat") 0 else 1,
                                containerColor = CosmicSurface,
                                contentColor = NeonRuby
                            ) {
                                Tab(selected = comSelection == "chat", onClick = { comSelection = "chat" }) {
                                    Text("Messenger", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                                Tab(selected = comSelection == "notifications", onClick = { comSelection = "notifications" }) {
                                    Text("Notifications", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (comSelection == "chat") ChatScreen() else NotificationsScreen()
                            }
                        }
                    }

                    "hardware" -> {
                        var hwSelection by rememberSaveable { mutableStateOf("bluetooth") }
                        Column {
                            TabRow(
                                selectedTabIndex = when (hwSelection) {
                                    "bluetooth" -> 0
                                    "camera" -> 1
                                    else -> 2
                                },
                                containerColor = CosmicSurface,
                                contentColor = NeonRuby
                            ) {
                                Tab(selected = hwSelection == "bluetooth", onClick = { hwSelection = "bluetooth" }) {
                                    Text("BLE Scan", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                                Tab(selected = hwSelection == "camera", onClick = { hwSelection = "camera" }) {
                                    Text("Camera", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                                Tab(selected = hwSelection == "gallery", onClick = { hwSelection = "gallery" }) {
                                    Text("Gallery", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                when (hwSelection) {
                                    "bluetooth" -> BluetoothScreen()
                                    "camera" -> CameraScreen()
                                    "gallery" -> GalleryScreen()
                                }
                            }
                        }
                    }

                    "tasks" -> {
                        var taskSelection by rememberSaveable { mutableStateOf("downloads") }
                        Column {
                            TabRow(
                                selectedTabIndex = if (taskSelection == "downloads") 0 else 1,
                                containerColor = CosmicSurface,
                                contentColor = NeonRuby
                            ) {
                                Tab(selected = taskSelection == "downloads", onClick = { taskSelection = "downloads" }) {
                                    Text("Downloads", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                                Tab(selected = taskSelection == "sync", onClick = { taskSelection = "sync" }) {
                                    Text("Sync Logs", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (taskSelection == "downloads") DownloadsScreen() else BackgroundSyncScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    // Trigger diagnostics dialogue on initialization
    LaunchedEffect(Unit) {
        triggerDynamicAlert()
    }
}

data class TabSpec(val id: String, val label: String, val icon: ImageVector)
