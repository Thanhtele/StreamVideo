package com.example.presentation.screens.dashboard

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.DividerGray
import com.example.presentation.theme.ElectricViolet
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.dashboard.DashboardViewModel

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