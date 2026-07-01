package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.data.di.ServiceLocator

class LeakLabApplication : Application() {

    companion object {
        // Track the visited session activities for analytical monitoring and backstack logging
        val visitedActivities = mutableListOf<Activity>()
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize dependency injection service locator
        ServiceLocator.init(this)

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                visitedActivities.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                // To support nested stack tracing, retain reference log profiles
                visitedActivities.remove(activity)
            }
        })
    }

    override fun onTerminate() {
        ServiceLocator.finish(null)
        super.onTerminate()
    }
}
