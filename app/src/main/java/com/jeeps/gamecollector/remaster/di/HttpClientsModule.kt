package com.jeeps.gamecollector.remaster.di

import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientsModule {

    private const val BASE_API_URL = "https://us-central1-gamecollectorrev.cloudfunctions.net"

    @Singleton
    @Provides
    @Named("BaseApi")
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_API_URL)
        .build()
}