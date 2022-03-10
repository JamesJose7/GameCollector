package com.jeeps.gamecollector.remaster.data.api.interceptors

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.BuildConfig
import com.jeeps.gamecollector.remaster.data.api.ApiUser
import com.jeeps.gamecollector.remaster.utils.PreferencesWrapper
import com.jeeps.gamecollector.remaster.utils.constants.PreferencesKeys
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class IgdbInterceptor @Inject constructor(
    private val apiUser: ApiUser
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        getTwitchAuthToken()
        val token = PreferencesWrapper
            .read(PreferencesKeys.TWITCH_AUTH_TOKEN, String::class.java) ?: ""

        val original = chain.request()
        val request = original.newBuilder()
            .header("Client-ID", CLIENT_ID)
            .header("Authorization", token)
            .header("Content-Type", "text/plain")
            .method(original.method, original.body)
            .build()

        return chain.proceed(request)
    }

    private fun getTwitchAuthToken() = runBlocking {
        launch {
            if (PreferencesWrapper
                    .read(PreferencesKeys.TWITCH_AUTH_TOKEN, String::class.java)
                    .isNullOrEmpty()) {

                val token = when (val igdbAuth = apiUser.igdbAuth()) {
                    is NetworkResponse.Success -> {
                        val accessToken = igdbAuth.body.access_token
                        "Bearer $accessToken"
                    }
                    is NetworkResponse.Error -> null
                }
                token?.let {
                    PreferencesWrapper.save(PreferencesKeys.TWITCH_AUTH_TOKEN, token)
                }
            }
        }
    }

    companion object {
        private const val CLIENT_ID = BuildConfig.IGDB_CLIENT_ID
    }
}