package com.example.presentation.screens.auth

import android.animation.ValueAnimator
import android.view.View
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.di.ServiceLocator
import com.example.presentation.theme.CosmicDeepSpace
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.auth.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = remember { SplashViewModel() },
    onNavigateHome: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    val context = LocalContext.current
    val isFinished by viewModel.isFinished.collectAsState()

    // Custom pulsing animation for the startup logo display
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Synchronize background display rendering loops
    DisposableEffect(Unit) {
        val dummyView = View(context)
        val animator = ValueAnimator.ofFloat(0f, 100f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                dummyView.alpha = it.animatedValue as Float
            }
        }
        animator.start()

        onDispose {
            // Dispose logic
        }
    }

    LaunchedEffect(Unit) {
        val hasUser = ServiceLocator.userRepository != null
        viewModel.startTimeout() {
            //onNavigateLogin()
            onNavigateHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBlack, CosmicDeepSpace, CosmicSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LEAK LAB",
                color = NeonRuby,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(pulseScale)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Performance Diagnostics & Memory Lab",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}