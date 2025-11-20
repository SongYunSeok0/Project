package com.data.core.di

import com.data.core.auth.TokenStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CoreEntryPoint {
    fun tokenStore(): TokenStore
}
