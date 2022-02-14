package com.jeeps.gamecollector.remaster.di

import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.jeeps.gamecollector.remaster.data.api.ApiUser
import com.jeeps.gamecollector.remaster.data.api.interceptors.IgdbInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientsModule {

    private const val BASE_API_URL = "https://us-central1-gamecollectorrev.cloudfunctions.net"

    private const val IGDB_API_URL = "https://api.igdb.com"

    @Singleton
    @Provides
    @Named("BaseApi")
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_API_URL)
        .build()

    @Singleton
    @Provides
    @Named("IgdbApi")
    fun provideIgdbRetrofit(@Named("IdgbHttpClient") client: OkHttpClient): Retrofit = Retrofit.Builder()
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(IGDB_API_URL)
        .client(client)
        .build()


    @Singleton
    @Provides
    @Named("IdgbHttpClient")
    fun provideIgdbHttpClient(apiUser: ApiUser): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(IgdbInterceptor(apiUser))
        .build()

}