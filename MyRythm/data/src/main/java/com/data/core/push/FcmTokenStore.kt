package com.data.core.push

import android.content.Context
import androidx.core.content.edit

class FcmTokenStore(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit { putString("fcm_token", token) }
    }

    fun getToken(): String? = prefs.getString("fcm_token", null)
}