package com.jeeps.gamecollector.remaster.di.network

import com.jeeps.gamecollector.remaster.data.api.ApiUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApisModule {

    @Singleton
    @Provides
    fun provideUserApiService(@Named("BaseApi") retrofit: Retrofit): ApiUser =
        retrofit.create(ApiUser::class.java)
}