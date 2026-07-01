package com.example.presentation.screens.tasks

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.DividerGray
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.task.DownloadsViewModel
import com.example.service.DownloadService

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = remember { DownloadsViewModel() }
) {
    val downloads by viewModel.downloadsState.collectAsState()
    val context = LocalContext.current

    // Connect to the background sync and download foreground service
    var isBound by remember { mutableStateOf(false) }
    var downloadService: DownloadService? by remember { mutableStateOf(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? DownloadService.LocalBinder
                binder?.clientActivity = context as? Activity
                downloadService = binder?.getService()
                isBound = true

                // Add to static list inside service
                DownloadService.boundServiceConnections.add(
                    DownloadService.ServiceConnectionRef(name?.className ?: "unknown", context)
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
                downloadService = null
            }
        }
    }

    LaunchedEffect(Unit) {
        val intent = Intent(context, DownloadService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Disconnect binding lifecycle parameters on screen release
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Asset Transfer Hub", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            Button(
                onClick = {
                    viewModel.startNewDownload("Binary_Node_${(10..99).random()}.bin", "https://leaklab.io/assets/bundle")
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp)
                    .testTag("trigger_download")
            ) {
                Text("Start Background Sync Stream", fontWeight = FontWeight.Bold)
            }

            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text("No background sync streams running", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(downloads) { dl ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(dl.fileName, color = OnSpaceWhite, fontWeight = FontWeight.Bold)
                                    Text(dl.status, color = if (dl.status == "COMPLETED") SuccessGreen else NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { dl.progress / 100f },
                                    color = NeonRuby,
                                    trackColor = DividerGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Transfer: ${dl.progress}%", color = TextGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}