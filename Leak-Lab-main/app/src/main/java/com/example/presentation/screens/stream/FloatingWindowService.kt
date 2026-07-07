package com.example.presentation.screens.stream

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.R
import com.example.presentation.screens.stream.component.FloatingStreamContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FloatingWindowService : LifecycleService(), SavedStateRegistryOwner {

    companion object {
        const val MIN_WIDTH = 700
        const val MIN_HEIGHT = 400
        const val DELTA_X = 0
        const val DELTA_Y = 0

        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_HIDE = "ACTION_HIDE"
    }

    object Direction {
        const val LEFT = (Gravity.START or Gravity.TOP)
        const val RIGHT = Gravity.END or Gravity.TOP
        const val FRONT = Gravity.START or Gravity.TOP
        const val REAR = Gravity.END or Gravity.TOP
    }

    val Context.floatingDataStore by preferencesDataStore(name = "floating_window")

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var floatingLayoutParams: WindowManager.LayoutParams
    private var curDirection = Direction.LEFT

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    inner class LocalBinder : Binder() {
        fun getService(): FloatingWindowService {
            return this@FloatingWindowService
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun showFloatingWindow(direction: Int = Direction.RIGHT) {
        if (!::composeView.isInitialized || !::floatingLayoutParams.isInitialized) return

        curDirection = direction
        floatingLayoutParams.gravity = curDirection

        windowManager.updateViewLayout(
            composeView,
            floatingLayoutParams
        )
    }

    fun hideFloatingWindow() {
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }

    fun updateStreamUrl(url: String) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        startForegroundServiceNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        lifecycleScope.launch {
            createFloatingWindow()
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW -> {
                showFloatingWindow()
            }

            ACTION_HIDE -> {
                hideFloatingWindow()
            }
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createFloatingWindow() {
        floatingLayoutParams = createLayoutParams()
        composeView = createComposeView()

        windowManager.addView(
            composeView,
            floatingLayoutParams
        )
    }

    private fun createComposeView() = ComposeView(this).apply {
        setViewTreeLifecycleOwner(this@FloatingWindowService)
        setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)

        setContent {
            MaterialTheme {
                FloatingStreamContent(
                    direction = curDirection,
                    onMove = { deltaX, deltaY ->
                        floatingLayoutParams.x += deltaX.toInt() * if (curDirection == Direction.LEFT) 1 else -1
                        floatingLayoutParams.y += deltaY.toInt()

                        windowManager.updateViewLayout(
                            composeView,
                            layoutParams
                        )
                    },
                    onResize = { deltaWidth, deltaHeight ->
                        floatingLayoutParams.width = (floatingLayoutParams.width + deltaWidth.toInt()).coerceAtLeast(MIN_WIDTH)
                        floatingLayoutParams.height = (floatingLayoutParams.height + deltaHeight.toInt()).coerceAtLeast(MIN_HEIGHT)

                        windowManager.updateViewLayout(
                            composeView,
                            layoutParams
                        )
                    },
                    onClose = {
                        showFloatingWindow(if (curDirection == Direction.LEFT) Direction.RIGHT else Direction.LEFT)
                        //hideFloatingWindow()
                    },
                    onSaveConfig = {
                        lifecycleScope.launch {
                            applicationContext.saveFloatingWindowParams(
                                x = floatingLayoutParams.x,
                                y = floatingLayoutParams.y,
                                width = floatingLayoutParams.width,
                                height = floatingLayoutParams.height
                            )
                        }
                    }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createLayoutParams(): WindowManager.LayoutParams {
        val savedParams = applicationContext.getFloatingWindowParams().first()
        return WindowManager.LayoutParams(
            savedParams.width,
            savedParams.height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedParams.x
            y = savedParams.y
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "floating_window_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Window",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(
            this,
            channelId
        )
            .setContentTitle("Camera Stream")
            .setContentText("Floating camera is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(
            1,
            notification
        )
    }

    suspend fun Context.saveFloatingWindowParams(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        floatingDataStore.edit { preferences ->
            preferences[FloatingWindowKeys.X] = x
            preferences[FloatingWindowKeys.Y] = y
            preferences[FloatingWindowKeys.WIDTH] = width
            preferences[FloatingWindowKeys.HEIGHT] = height
        }
    }

    fun Context.getFloatingWindowParams(): Flow<FloatingWindowParams> {
        return floatingDataStore.data.map { preferences ->
            FloatingWindowParams(
                x = preferences[FloatingWindowKeys.X] ?: DELTA_X,
                y = preferences[FloatingWindowKeys.Y] ?: DELTA_Y,
                width = preferences[FloatingWindowKeys.WIDTH] ?: MIN_WIDTH,
                height = preferences[FloatingWindowKeys.HEIGHT] ?: MIN_HEIGHT
            )
        }
    }

    override fun onDestroy() {
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        super.onDestroy()
    }
}