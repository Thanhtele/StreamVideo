package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.MainActivity
import java.util.*

class DownloadService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    // Trace log list of connections binding to this service for active transfer metrics
    companion object {
        val boundServiceConnections = ArrayList<ServiceConnectionRef>()
    }

    data class ServiceConnectionRef(val id: String, val context: Context)

    inner class LocalBinder : Binder() {
        // Binding client reference
        var clientActivity: Activity? = null
        
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize background transfer wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "LeakLab::DownloadWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "download_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Downloading Assets")
            .setContentText("Transferring files in background...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()

        startForeground(101, notification)

        // Play alert sound to notify the user of background transfer start
        try {
            mediaPlayer = MediaPlayer.create(this, android.R.drawable.ic_lock_power_off)
            mediaPlayer?.isLooping = false
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Ignore
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
