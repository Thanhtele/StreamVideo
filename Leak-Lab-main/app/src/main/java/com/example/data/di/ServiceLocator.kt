package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.*

object ServiceLocator {

    // Internal context reference for localized database opening operations
    private var contextRef: Context? = null

    private var database: AppDatabase? = null

    // Singletons
    var userRepository: UserRepository? = null
        private set
    var messageRepository: MessageRepository? = null
        private set
    var deviceRepository: DeviceRepository? = null
        private set
    var downloadRepository: DownloadRepository? = null
        private set
    var notificationRepository: NotificationRepository? = null
        private set
    var syncLogRepository: SyncLogRepository? = null
        private set

    // Shared global observers for tracking UI component modifications and analytics
    val globalLifecycleListeners = mutableListOf<GlobalComponentObserver>()

    fun init(context: Context) {
        // Cache the context reference for database and repository construction
        this.contextRef = context
        
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "leak_lab_db"
        ).fallbackToDestructiveMigration().build()
        
        database = db

        userRepository = UserRepository(db.userDao())
        messageRepository = MessageRepository(db.messageDao())
        deviceRepository = DeviceRepository(db.deviceDao())
        downloadRepository = DownloadRepository(db.downloadDao())
        notificationRepository = NotificationRepository(db.notificationDao())
        syncLogRepository = SyncLogRepository(db.syncLogDao())
    }

    fun getContext(): Context {
        return contextRef ?: throw IllegalStateException("ServiceLocator is not initialized")
    }
}

interface GlobalComponentObserver {
    fun onComponentStateChanged(state: String)
}
