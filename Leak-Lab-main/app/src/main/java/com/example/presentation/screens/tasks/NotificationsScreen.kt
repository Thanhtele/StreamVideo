package com.example.presentation.screens.tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.task.NotificationsViewModel

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = remember { NotificationsViewModel() }
) {
    val logs by viewModel.notifications.collectAsState()
    val context = LocalContext.current

    // Subscribe to standard device system broadcast logs
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == "com.example.leaklab.NEW_NOTIFICATION") {
                    val title = intent.getStringExtra("title") ?: "Alert"
                    val body = intent.getStringExtra("body") ?: "Payload"
                    viewModel.createNotification(title, body)
                }
            }
        }
        val filter = IntentFilter("com.example.leaklab.NEW_NOTIFICATION")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            // Detach observers on screen dispose
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Notification Logs", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            Button(
                onClick = {
                    val intent = Intent("com.example.leaklab.NEW_NOTIFICATION").apply {
                        setPackage(context.packageName)
                        putExtra("title", "Lab Notification Logged")
                        putExtra("body", "Diagnostic node check recorded at: ${System.currentTimeMillis()}")
                    }
                    context.sendBroadcast(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp)
                    .testTag("trigger_notification")
            ) {
                Text("Post Diagnostic Alert", fontWeight = FontWeight.Bold)
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text("No diagnostic notifications recorded", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(logs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(log.title, color = OnSpaceWhite, fontWeight = FontWeight.Bold)
                                Text(log.body, color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}