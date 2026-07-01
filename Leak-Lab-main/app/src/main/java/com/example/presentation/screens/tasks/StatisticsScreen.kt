package com.example.presentation.screens.tasks

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.ElectricViolet
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.task.StatisticsViewModel

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
                    Text(
                        "CPU Load Core Simulator", color = NeonRuby, fontWeight = FontWeight.Bold, modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

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

                    Row(
                        verticalAlignment = Alignment.Bottom, modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(MidnightBlack)
                            .padding(12.dp)
                    ) {
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