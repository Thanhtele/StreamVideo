package com.example.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.DividerGray
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.dashboard.SettingsViewModel

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