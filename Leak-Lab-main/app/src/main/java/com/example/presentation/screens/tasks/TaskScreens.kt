package com.example.presentation.screens.tasks

import android.R
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.task.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = remember { ChatViewModel() }
) {
    val messages by viewModel.messagesState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Sound playback notifier hook for new message alert actions
    val playAlertSound = {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.drawable.ic_lock_power_off)
            mediaPlayer?.start()
        } catch (e: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("In-App Communications", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.isSentByMe
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMe) NeonRuby.copy(alpha = 0.2f) else CosmicSurface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(msg.senderName, color = if (isMe) NeonRuby else NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(msg.messageText, color = OnSpaceWhite, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Message...", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("chat_input")
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                            playAlertSound()
                        }
                    },
                    modifier = Modifier.testTag("send_button")
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = NeonRuby)
                }
            }
        }
    }
}
