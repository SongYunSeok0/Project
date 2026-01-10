@file:Suppress("DEPRECATION")

package com.data.core.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedPrefsTokenStore(
    context: Context
) : TokenStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /**
     * 🔐 EncryptedSharedPreferences
     *
     * - 환경 변경 / 디바이스 변경 / 서명 변경 시
     *   기존 암호화 데이터 복호화 실패(AEADBadTagException) 발생 가능
     * - 이 경우 기존 prefs를 삭제하고 새로 생성
     */
    private val prefs = try {
        EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // 🔥 깨진 암호화 데이터 제거
        context.deleteSharedPreferences(FILE)

        EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Volatile
    private var cached = AuthTokens(
        prefs.getString(KEY_ACCESS, null),
        prefs.getString(KEY_REFRESH, null)
    )

    private val _tokens = MutableStateFlow(cached)
    override val tokens: StateFlow<AuthTokens> = _tokens.asStateFlow()

    override suspend fun set(access: String?, refresh: String?, persist: Boolean) {
        prefs.edit {
            if (persist) {
                if (access != null) putString(KEY_ACCESS, access) else remove(KEY_ACCESS)
                if (refresh != null) putString(KEY_REFRESH, refresh) else remove(KEY_REFRESH)
            } else {
                // 자동로그인 OFF면 디스크 토큰은 반드시 제거
                remove(KEY_ACCESS)
                remove(KEY_REFRESH)
            }
        }
        update(access, refresh)
    }

    override suspend fun setAccess(access: String?) {
        prefs.edit {
            if (access != null) putString(KEY_ACCESS, access) else remove(KEY_ACCESS)
        }
        update(access, cached.refresh)
    }

    override suspend fun setRefresh(refresh: String?) {
        prefs.edit {
            if (refresh != null) putString(KEY_REFRESH, refresh) else remove(KEY_REFRESH)
        }
        update(cached.access, refresh)
    }

    override suspend fun clear() {
        prefs.edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
        update(null, null)
    }

    override fun current(): AuthTokens = cached

    private fun update(access: String?, refresh: String?) {
        val now = AuthTokens(access, refresh)
        cached = now
        _tokens.value = now
    }

    private companion object {
        const val FILE = "auth_tokens.secure_prefs"
        const val KEY_ACCESS = "k_access"
        const val KEY_REFRESH = "k_refresh"
    }
}
