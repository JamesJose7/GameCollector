package com.jeeps.gamecollector.remaster.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.user.User
import com.jeeps.gamecollector.remaster.data.model.data.user.UserDetails
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.deprecated.services.igdb.TwitchAuthToken
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiUser {
    @GET("/api/user")
    suspend fun getUser(@Header("Authorization") authorization: String): NetworkResponse<UserDetails, ErrorResponse>

    @POST("/api/signupUserdetails")
    suspend fun signupUserDetails(@Body user: User): NetworkResponse<ResponseBody, ErrorResponse>

    @GET("/api/igdbAuth")
    suspend fun igdbAuth(): NetworkResponse<TwitchAuthToken, ErrorResponse>
}