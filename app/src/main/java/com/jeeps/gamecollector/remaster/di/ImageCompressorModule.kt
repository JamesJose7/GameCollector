package com.jeeps.gamecollector.remaster.di

import android.content.Context
import com.jeeps.gamecollector.remaster.utils.ImageCompressor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ImageCompressorModule {

    @Provides
    fun provideImageCompressor(@ApplicationContext context: Context): ImageCompressor {
        return ImageCompressor(context)
    }
}