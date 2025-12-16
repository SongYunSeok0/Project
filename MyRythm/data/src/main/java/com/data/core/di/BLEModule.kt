package com.data.core.di

import com.data.device.BLEManager
import com.domain.BLEConnector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BLEModule {

    @Binds
    abstract fun bindBLEConnector(
        bleManager: BLEManager
    ): BLEConnector
}
