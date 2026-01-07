package com.data.repository

import android.content.Context
import com.domain.repository.AuthLocalRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class AuthLocalRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthLocalRepository {
    override fun clear() {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit { clear() }
    }
}
