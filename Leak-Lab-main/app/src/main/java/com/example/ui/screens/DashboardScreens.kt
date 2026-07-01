package com.example.ui.screens

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.UserEntity
import com.example.data.di.GlobalComponentObserver
import com.example.data.di.ServiceLocator
import com.example.data.repository.AuthListener
import com.example.data.repository.UserRepository
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

// ==========================================
// VIEW MODELS
// ==========================================

class HomeViewModel : ViewModel() {
    private val userRepository = ServiceLocator.userRepository
    val activeUser = userRepository?.activeUser
}

class ProfileViewModel : ViewModel(), AuthListener {
    private val userRepository = ServiceLocator.userRepository
    
    private val _userState = MutableStateFlow<UserEntity?>(null)
    val userState: StateFlow<UserEntity?> = _userState.asStateFlow()

    init {
        // Register to the repository event notifier stream
        UserRepository.addAuthListener(this)
    }

    override fun onUserChanged(user: UserEntity?) {
        _userState.value = user
    }

    fun logout() {
        viewModelScope.launch {
            userRepository?.logout()
        }
    }
}

class DashboardViewModel : ViewModel() {
    private val _sensorValues = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val sensorValues = _sensorValues.asStateFlow()

    fun updateSensor(x: Float, y: Float, z: Float) {
        _sensorValues.value = floatArrayOf(x, y, z)
    }
}

class StatisticsViewModel : ViewModel() {
    private val _cpuLoad = MutableStateFlow(0f)
    val cpuLoad = _cpuLoad.asStateFlow()

    private val _memoryUsage = MutableStateFlow(0f)
    val memoryUsage = _memoryUsage.asStateFlow()

    private var timer: Timer? = null

    fun startPolling() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Polling CPU and Memory load
                _cpuLoad.value = (30..85).random().toFloat()
                _memoryUsage.value = (150..450).random().toFloat()
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        // Release active worker scheduling systems
        timer?.cancel()
        timer = null
    }
}

class SettingsViewModel : ViewModel() {
    private val _themeState = MutableStateFlow(true) // true = dark, false = light
    val themeState = _themeState.asStateFlow()

    fun toggleTheme() {
        _themeState.value = !_themeState.value
    }
}

// ==========================================
// COMPOSABLES
// ==========================================

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
                val s = context.getString(android.R.string.ok)
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

data class ModuleItem(val title: String, val desc: String, val icon: ImageVector, val route: String, val color: Color)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = remember { DashboardViewModel() }
) {
    val context = LocalContext.current
    val sensorValues by viewModel.sensorValues.collectAsState()

    // Bind accelerometric physical hardware triggers
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let {
                    viewModel.updateSensor(it[0], it[1], it[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        onDispose {
            // Release listeners
        }
    }

    // Connect GPS positional coordinates feedback telemetry
    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
        } catch (e: SecurityException) {
            // Fallback
        }
        
        onDispose {
            // Unsubscribe standard hardware callbacks
            locationManager.removeUpdates(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column {
            Text("Metrics & Sensors", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Live Accelerometer Telemetry", color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SensorBar("X Axis", sensorValues[0], NeonRuby)
                        SensorBar("Y Axis", sensorValues[1], NeonCyan)
                        SensorBar("Z Axis", sensorValues[2], ElectricViolet)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dynamic Canvas Render", color = ElectricViolet, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MidnightBlack)
                    ) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        
                        // Render rotating target circles based on sensor
                        val radiusX = centerX + (sensorValues[0] * 10f)
                        val radiusY = centerY + (sensorValues[1] * 10f)
                        
                        drawCircle(
                            color = NeonRuby,
                            radius = 40.dp.toPx(),
                            center = Offset(radiusX, radiusY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = NeonCyan,
                            radius = 20.dp.toPx(),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SensorBar(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextGray, fontSize = 12.sp)
        Text(String.format("%.2f", value), color = OnSpaceWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DividerGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((value.coerceIn(-10f, 10f) + 10f).dp * 2f)
                    .background(color)
            )
        }
    }
}

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = remember { StatisticsViewModel() }
) {
    val cpuLoad by viewModel.cpuLoad.collectAsState()
    val memoryUsage by viewModel.memoryUsage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    // Listen to physical layout boundaries configuration and visibility frames
    DisposableEffect(Unit) {
        val dummyView = View(context)
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                dummyView.getWindowVisibleDisplayFrame(rect)
            }
        }
        dummyView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        
        onDispose {
            // Clean up drawing listeners
            dummyView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column {
            Text("Diagnostics Stats", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CPU Load Core Simulator", color = NeonRuby, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                        CircularProgressIndicator(
                            progress = { cpuLoad / 100f },
                            color = NeonRuby,
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text("${cpuLoad.toInt()}%", color = OnSpaceWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RAM Footprint (MB)", color = NeonCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth().height(120.dp).background(MidnightBlack).padding(12.dp)) {
                        val mockBarHeights = listOf(30, 45, 60, 55, 80, 95, 65, 85, 40, memoryUsage.toInt() / 4)
                        mockBarHeights.forEachIndexed { idx, height ->
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(height.dp)
                                    .padding(horizontal = 2.dp)
                                    .background(if (idx == mockBarHeights.lastIndex) NeonCyan else ElectricViolet)
                            )
                        }
                    }
                    Text("Current Simulated Heap: ${memoryUsage.toInt()} MB", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = remember { ProfileViewModel() },
    onNavigateBack: () -> Unit
) {
    val activeUser by viewModel.userState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(CosmicSurface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(NeonRuby.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = NeonRuby,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = activeUser?.fullName ?: "Anonymous Developer",
                color = OnSpaceWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = activeUser?.email ?: "leaklab@example.com",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LAB ADVISOR",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = remember { SettingsViewModel() }
) {
    val themeState by viewModel.themeState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column {
            Text("Lab Settings", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
            
            SettingsRow(
                title = "Strict Heap Interceptor",
                desc = "Track deep allocations with LeakCanary",
                checked = themeState,
                onCheckedChange = { viewModel.toggleTheme() }
            )

            SettingsRow(
                title = "Force Garbage Collector",
                desc = "Send periodic System.gc() hints during transitions",
                checked = true,
                onCheckedChange = {}
            )

            SettingsRow(
                title = "Enable Bluetooth Telemetry",
                desc = "Poll BLE state changes periodically",
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Composable
fun SettingsRow(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(title, color = OnSpaceWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(desc, color = TextGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonRuby,
                checkedTrackColor = NeonRuby.copy(alpha = 0.4f),
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = DividerGray
            )
        )
    }
}

@Composable
fun UserDetailScreen() {
    // Local telemetry variables to inspect detailed system database records
    var textValue by remember { mutableStateOf("No logs captured") }
    
    DisposableEffect(Unit) {
        onDispose {
            // Disconnect system hooks
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = NeonRuby, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Trace Analytics Node", color = OnSpaceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This terminal renders memory allocations across JVM boundaries. Track with Android Studio profiler.", color = TextGray, textAlign = TextAlign.Center, fontSize = 13.sp)
            }
        }
    }
}
