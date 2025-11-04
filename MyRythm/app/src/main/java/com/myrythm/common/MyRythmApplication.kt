package com.myrythm.common

import android.app.Application
import android.util.Log

class MyRythmApplication  : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppApplication", "MyRythm Application started.")
    }
}