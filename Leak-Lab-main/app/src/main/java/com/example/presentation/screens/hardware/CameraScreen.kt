package com.example.presentation.screens.hardware

import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }

    // Surface frame mapping hooks for native camera simulation feeds
    val surfaceView = remember {
        SurfaceView(context).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    val canvas = holder.lockCanvas()
                    if (canvas != null) {
                        val paint = Paint().apply {
                            color = Color.RED
                            textSize = 40f
                        }
                        canvas.drawColor(Color.BLACK)
                        canvas.drawText("SIMULATED CAMERA ACTIVE", 100f, 200f, paint)
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    // Release and disconnect frame locks
                }
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Hardware Camera Node", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicSurface)
            ) {
                AndroidView(
                    factory = { surfaceView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isRecording = !isRecording },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) NeonRuby else NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isRecording) "Stop Capture" else "Start Capture", fontWeight = FontWeight.Bold, color = MidnightBlack)
            }
        }
    }
}