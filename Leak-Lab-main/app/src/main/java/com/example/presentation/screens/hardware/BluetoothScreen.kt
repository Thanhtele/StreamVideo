package com.example.presentation.screens.hardware

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.hardware.BluetoothViewModel

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
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
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