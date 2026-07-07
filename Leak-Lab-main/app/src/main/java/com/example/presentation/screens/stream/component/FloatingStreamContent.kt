package com.example.presentation.screens.stream.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FloatingStreamContent(
    direction: Int,
    onMove: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    onClose: () -> Unit,
    onSaveConfig: () -> Unit,
) {
    var editMode by remember { mutableStateOf(false) }
    var actionCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(editMode, actionCount) {
        if (editMode) {
            delay(2000.milliseconds)
            editMode = false
            onSaveConfig.invoke()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (editMode) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.Green
                    )
                } else {
                    Modifier
                }
            )
    ) {
        InternalWebView(
            modifier = Modifier.matchParentSize(),
            url = "http://10.0.2.2:8080/",
            direction = direction,
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            editMode = true
                            actionCount++
                        }
                    )
                }
        )

        if (editMode) {
            // Drag toàn bộ window
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            actionCount++
                            change.consume()
                            onMove(
                                dragAmount.x,
                                dragAmount.y
                            )
                        }
                    }
            )

            // Resize handle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            actionCount++
                            change.consume()
                            onResize(
                                dragAmount.x,
                                dragAmount.y
                            )
                        }
                    }
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}