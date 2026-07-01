package com.example.ui.screens

import android.content.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DownloadEntity
import com.example.data.database.MessageEntity
import com.example.data.database.NotificationEntity
import com.example.data.database.SyncLogEntity
import com.example.data.di.ServiceLocator
import com.example.data.repository.*
import com.example.service.DownloadService
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// VIEW MODELS
// ==========================================

class ChatViewModel : ViewModel() {
    private val messageRepository = ServiceLocator.messageRepository

    private val _messagesState = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messagesState = _messagesState.asStateFlow()

    init {
        // Register direct callback events to message triggers
        MessageRepository.messageCallbacks.add { msg ->
            viewModelScope.launch {
                messageRepository?.sendMessage("Bot Response", "Acknowledged: ${msg.messageText}", false)
            }
        }

        viewModelScope.launch {
            messageRepository?.messages?.collect {
                _messagesState.value = it
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            messageRepository?.sendMessage("User", text, true)
        }
    }
}

class DownloadsViewModel : ViewModel(), DownloadProgressListener {
    private val downloadRepository = ServiceLocator.downloadRepository

    private val _downloadsState = MutableStateFlow<List<DownloadEntity>>(emptyList())
    val downloadsState = _downloadsState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadRepository?.downloads?.collect {
                _downloadsState.value = it
            }
        }
    }

    fun startNewDownload(fileName: String, url: String) {
        viewModelScope.launch {
            // Store listener reference for status tracking callbacks
            DownloadRepository.progressListeners[fileName] = this@DownloadsViewModel
            downloadRepository?.startDownload(fileName, url)
        }
    }

    override fun onProgressUpdated(id: String, progress: Int, status: String) {
        // Notify progress
    }
}

class NotificationsViewModel : ViewModel() {
    private val notificationRepository = ServiceLocator.notificationRepository

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            notificationRepository?.allNotifications?.collect {
                _notifications.value = it
            }
        }
    }

    fun createNotification(title: String, body: String) {
        viewModelScope.launch {
            notificationRepository?.addNotification(title, body, "DIAGNOSTICS")
        }
    }
}

class BackgroundSyncViewModel : ViewModel() {
    private val syncRepository = ServiceLocator.syncLogRepository

    private val _syncLogs = MutableStateFlow<List<SyncLogEntity>>(emptyList())
    val syncLogs = _syncLogs.asStateFlow()

    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    init {
        viewModelScope.launch {
            syncRepository?.syncLogs?.collect {
                _syncLogs.value = it
            }
        }

        // Spin up background processor thread to manage offline synchronization logs
        handlerThread = HandlerThread("DiagnosticsSyncThread")
        handlerThread?.start()
        backgroundHandler = Handler(handlerThread!!.looper)
    }

    fun triggerLocalSync() {
        backgroundHandler?.post {
            // Simulated processing inside background thread
            viewModelScope.launch {
                syncRepository?.addSyncLog(
                    actionName = "Manual Local Sync",
                    status = "SUCCESS",
                    durationMs = 450L,
                    details = "Processed manual local memory diagnostics check."
                )
            }
        }
    }
}

// ==========================================
// COMPOSABLES
// ==========================================

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
            val mediaPlayer = MediaPlayer.create(context, android.R.drawable.ic_lock_power_off)
            mediaPlayer?.start()
        } catch (e: Exception) {}
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                    modifier = Modifier.weight(1.0f).testTag("chat_input")
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
                binder?.clientActivity = context as? android.app.Activity
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
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 16.dp).testTag("trigger_download")
            ) {
                Text("Start Background Sync Stream", fontWeight = FontWeight.Bold)
            }

            if (downloads.isEmpty()) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No background sync streams running", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(downloads) { dl ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
        context.registerReceiver(receiver, filter)
        
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
                        putExtra("title", "Lab Notification Logged")
                        putExtra("body", "Diagnostic node check recorded at: ${System.currentTimeMillis()}")
                    }
                    context.sendBroadcast(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 16.dp).testTag("trigger_notification")
            ) {
                Text("Post Diagnostic Alert", fontWeight = FontWeight.Bold)
            }

            if (logs.isEmpty()) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No diagnostic notifications recorded", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(logs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 16.dp).testTag("trigger_sync")
            ) {
                Text("Queue Diagnostics Work", fontWeight = FontWeight.Bold)
            }

            if (syncLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1.0f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No diagnostics logs found", color = TextGray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1.0f)) {
                    items(syncLogs) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
