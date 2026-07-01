package com.example.presentation.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.SuccessGreen
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.task.BackgroundSyncViewModel

@Composable
fun BackgroundSyncScreen(
    viewModel: BackgroundSyncViewModel = remember { BackgroundSyncViewModel() }
) {
    val syncLogs by viewModel.syncLogs.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Background Sync Manager", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            Button(
                onClick = { viewModel.triggerLocalSync() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp)
                    .testTag("trigger_sync")
            ) {
                Text("Queue Diagnostics Work", fontWeight = FontWeight.Bold)
            }

            if (syncLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text("No diagnostics logs found", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(syncLogs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(log.actionName, color = OnSpaceWhite, fontWeight = FontWeight.Bold)
                                    Text(log.status, color = if (log.status == "SUCCESS") SuccessGreen else NeonRuby, fontWeight = FontWeight.Bold)
                                }
                                Text("Duration: ${log.durationMs} ms", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                Text(log.details, color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}