package com.myrythm.common

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyRythmApplication  : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRythm Application started.")
    }
}