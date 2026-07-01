package com.example.presentation.screens.dashboard

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.di.GlobalComponentObserver
import com.example.data.di.ServiceLocator
import com.example.presentation.theme.AccentGold
import com.example.presentation.theme.AlertOrange
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.ElectricViolet
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.dashboard.HomeViewModel

data class ModuleItem(val title: String, val desc: String, val icon: ImageVector, val route: String, val color: Color)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = remember { HomeViewModel() },
    onNavigateTo: (String) -> Unit
) {
    val activeUser by viewModel.activeUser?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
    val context = LocalContext.current

    // Observe core application lifecycle changes to coordinate system overlays
    DisposableEffect(Unit) {
        val observer = object : GlobalComponentObserver {
            override fun onComponentStateChanged(state: String) {
                // Capture home context
                val s = context.getString(R.string.ok)
            }
        }
        ServiceLocator.globalLifecycleListeners.add(observer)
        onDispose {
            // Unsubscribe standard state observers on exit
            ServiceLocator.globalLifecycleListeners
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome to Leak Lab",
                            color = OnSpaceWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select a sub-module to test memory footprint",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CosmicSurface)
                            .clickable { onNavigateTo("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = NeonRuby,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Active Diagnostics",
                                color = OnSpaceWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "LeakCanary is monitoring background operations. Tap any module to simulate workflow.",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "LAB MODULES",
                    color = NeonRuby,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }

            val modules = listOf(
                ModuleItem("Dashboard Metrics", "System sensors & metrics", Icons.Filled.Star, "dashboard", NeonRuby),
                ModuleItem("Performance Stats", "Polled charts & graphs", Icons.Filled.DateRange, "statistics", ElectricViolet),
                ModuleItem("Hardware Scanner", "Bluetooth & Device BLE scan", Icons.Filled.Refresh, "bluetooth", NeonCyan),
                ModuleItem("Chat Messenger", "Mock socket & thread loops", Icons.Filled.Send, "chat", SuccessGreen),
                ModuleItem("Media Gallery", "Image loading with Coil", Icons.Filled.Menu, "gallery", AccentGold),
                ModuleItem("Downloads Manager", "Service binder & loops", Icons.Filled.ArrowDropDown, "downloads", AlertOrange),
                ModuleItem("Search Bar", "Flow collectors & watchers", Icons.Filled.Search, "search", OnSpaceWhite)
            )

            items(modules) { module ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onNavigateTo(module.route) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(module.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(module.icon, contentDescription = null, tint = module.color, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(module.title, color = OnSpaceWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(module.desc, color = TextGray, fontSize = 12.sp)
                        }

                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}