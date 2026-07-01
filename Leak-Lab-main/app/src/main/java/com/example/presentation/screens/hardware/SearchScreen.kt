package com.example.presentation.screens.hardware


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.presentation.theme.DividerGray
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.hardware.SearchViewModel

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
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
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