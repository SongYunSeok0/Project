package com.data.util

import android.content.Context
import android.location.Geocoder
import com.domain.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtil {

    suspend fun getAreaHint(center: Location, context: Context): String =
        withContext(Dispatchers.IO) {
            try {
                val g = Geocoder(context, Locale.KOREA)
                @Suppress("DEPRECATION")
                val list = g.getFromLocation(center.latitude, center.longitude, 1)
                val a = list?.firstOrNull()
                val city = a?.locality ?: a?.adminArea ?: ""
                val gu = a?.subAdminArea ?: ""
                val dong = a?.thoroughfare ?: ""
                listOf(city, gu, dong).filter { it.isNotBlank() }.joinToString(" ")
            } catch (_: Exception) {
                ""
            }
        }
}