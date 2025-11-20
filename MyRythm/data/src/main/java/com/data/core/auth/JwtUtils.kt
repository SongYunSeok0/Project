package com.data.core.auth

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun extractUserId(access: String?): String? {
        if (access.isNullOrBlank()) return null
        val parts = access.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
        val json = JSONObject(payload)
        // 서버 구현 중 있는 키 우선순위로 시도
        return when {
            json.has("uuid") -> json.getString("uuid")
            json.has("user_id") -> json.get("user_id").toString()
            json.has("sub") -> json.getString("sub")
            else -> null
        }
    }
}
