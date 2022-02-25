package com.jeeps.gamecollector.remaster.di.network

import com.jeeps.gamecollector.remaster.data.api.*
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

    @Singleton
    @Provides
    fun providePlatformApiService(@Named("BaseApi") retrofit: Retrofit): ApiPlatform =
        retrofit.create(ApiPlatform::class.java)

    @Singleton
    @Provides
    fun provideGamesApiService(@Named("BaseApi") retrofit: Retrofit): ApiGame =
        retrofit.create(ApiGame::class.java)

    @Singleton
    @Provides
    fun provideStatsApiService(@Named("BaseApi") retrofit: Retrofit): ApiStats =
        retrofit.create(ApiStats::class.java)

    @Singleton
    @Provides
    fun provideIgdbApiService(@Named("IgdbApi") retrofit: Retrofit): ApiIgdb =
        retrofit.create(ApiIgdb::class.java)
}