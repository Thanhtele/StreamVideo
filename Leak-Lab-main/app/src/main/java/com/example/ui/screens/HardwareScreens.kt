package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DeviceEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.DeviceRepository
import com.example.data.repository.DeviceScanListener
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// BITMAP CACHE SYSTEM
// ==========================================

object StaticImageCache {
    // Shared system image cache mapping for fast hardware-accelerated drawing
    private val cache = LruCache<String, CachedValue>(30)

    data class CachedValue(val bitmap: Bitmap, val context: Context)

    fun put(key: String, bitmap: Bitmap, context: Context) {
        cache.put(key, CachedValue(bitmap, context))
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)?.bitmap
    }
}

// ==========================================
// VIEW MODELS
// ==========================================

class BluetoothViewModel : ViewModel(), DeviceScanListener {
    private val deviceRepository = ServiceLocator.deviceRepository

    private val _devices = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null

    init {
        // Register to the localized hardware scan callback events
        DeviceRepository.scanListeners.add(this)
        
        viewModelScope.launch {
            deviceRepository?.scannedDevices?.collect {
                _devices.value = it
            }
        }
    }

    fun startScan() {
        _isScanning.value = true
        
        // Simulating periodic scan results using a Handler loop
        scanRunnable = object : Runnable {
            override fun run() {
                val mac = "00:1A:7D:DA:71:" + (10..99).random()
                val dev = DeviceEntity(
                    macAddress = mac,
                    name = listOf("Galaxy Watch", "Sony XM5", "Logitech Mouse", "Smart TV", "HomePod").random(),
                    rssi = -(50..95).random(),
                    type = "BLE",
                    isPaired = false,
                    lastSeen = System.currentTimeMillis()
                )
                viewModelScope.launch {
                    deviceRepository?.addScannedDevice(dev)
                }
                
                // Scan loop runs every 3 seconds
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(scanRunnable!!)
    }

    fun stopScan() {
        _isScanning.value = false
        scanRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDeviceScanned(device: DeviceEntity) {
        // Handle direct callback event
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up loop parameters on clearance
    }
}

class SearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun updateQuery(query: String) {
        _searchQuery.value = query
        // Simulate real filtering
        viewModelScope.launch {
            ServiceLocator.deviceRepository?.scannedDevices?.collect { list ->
                _searchResults.value = list.filter {
                    it.name.contains(query, ignoreCase = true) || it.macAddress.contains(query)
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLES
// ==========================================

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }

    // Surface frame mapping hooks for native camera simulation feeds
    val surfaceView = remember {
        SurfaceView(context).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    val canvas = holder.lockCanvas()
                    if (canvas != null) {
                        val paint = Paint().apply {
                            color = android.graphics.Color.RED
                            textSize = 40f
                        }
                        canvas.drawColor(android.graphics.Color.BLACK)
                        canvas.drawText("SIMULATED CAMERA ACTIVE", 100f, 200f, paint)
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    // Release and disconnect frame locks
                }
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Hardware Camera Node", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicSurface)
            ) {
                AndroidView(
                    factory = { surfaceView },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { isRecording = !isRecording },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) NeonRuby else NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isRecording) "Stop Capture" else "Start Capture", fontWeight = FontWeight.Bold, color = MidnightBlack)
            }
        }
    }
}

@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    val items = remember {
        listOf(
            "Node Delta" to "https://images.unsplash.com/photo-1579546929518-9e396f3cc809",
            "Node Gamma" to "https://images.unsplash.com/photo-1550684848-fac1c5b4e853",
            "Node Epsilon" to "https://images.unsplash.com/photo-1541701494587-cb58502866ab",
            "Node Alpha" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",
            "Node Zeta" to "https://images.unsplash.com/photo-1518770660439-4636190af475",
            "Node Theta" to "https://images.unsplash.com/photo-1451187580459-43490279c0fa"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Graphics Gallery", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1.0f)
            ) {
                items(items) { item ->
                    // Generate a high-res bitmap locally to prevent crash, load into our static cache
                    val bitmap = remember(item.first) {
                        val b = Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(b)
                        val paint = Paint().apply {
                            color = android.graphics.Color.parseColor("#FF2A5F")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(0f, 0f, 250f, 250f, paint)
                        paint.color = android.graphics.Color.WHITE
                        paint.textSize = 24f
                        canvas.drawText(item.first, 40f, 130f, paint)
                        
                        // Save image and display boundaries to cache map
                        StaticImageCache.put(item.first, b, context)
                        b
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.first, color = OnSpaceWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = remember { BluetoothViewModel() }
) {
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startScan()
        onDispose {
            viewModel.stopScan()
            // Disconnect physical scanning listeners
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bluetooth Scan Node", color = OnSpaceWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                
                IconButton(
                    onClick = {
                        if (isScanning) viewModel.stopScan() else viewModel.startScan()
                    },
                    modifier = Modifier.testTag("scan_trigger")
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Scan toggle",
                        tint = if (isScanning) NeonRuby else NeonCyan
                    )
                }
            }

            if (devices.isEmpty()) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Scanning for BLE active nodes...", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(devices) { device ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(device.name, color = OnSpaceWhite, fontWeight = FontWeight.Bold)
                                    Text(device.macAddress, color = TextGray, fontSize = 12.sp)
                                }
                                Text("${device.rssi} dBm", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = remember { SearchViewModel() }
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                label = { Text("Search laboratory nodes", color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSpaceWhite,
                    unfocusedTextColor = OnSpaceWhite,
                    focusedBorderColor = NeonRuby,
                    unfocusedBorderColor = DividerGray
                ),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = NeonRuby) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
                    .padding(bottom = 16.dp)
            )

            if (results.isEmpty()) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Enter dynamic name to query BLE nodes", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(results) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.name, color = OnSpaceWhite, fontWeight = FontWeight.Bold)
                                Text("MAC: ${item.macAddress}", color = TextGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
