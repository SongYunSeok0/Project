package com.data.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 1127 자동로그인 적용

// Context 확장 - DataStore 초기화
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

@Singleton
class AuthPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.authDataStore

    companion object {
        private val AUTO_LOGIN_KEY = booleanPreferencesKey("auto_login_enabled")
    }

    // 자동로그인 스위치 상태 저장
    suspend fun setAutoLoginEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AUTO_LOGIN_KEY] = enabled
        }
    }

    // 자동로그인 스위치 상태 불러오기
    suspend fun isAutoLoginEnabled(): Boolean {
        return dataStore.data
            .map { prefs -> prefs[AUTO_LOGIN_KEY] ?: false }
            .first()
    }

    // 로그아웃 시 자동로그인 값도 지우고 싶을 때 사용
    // 로그아웃 하면 기존의 자동로그인 해제됨
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
