package com.jeeps.gamecollector.remaster.data.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.user.User
import com.jeeps.gamecollector.remaster.data.model.data.user.UserDetails
import com.jeeps.gamecollector.remaster.data.api.ApiUser
import com.jeeps.gamecollector.remaster.utils.PreferencesWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
class AuthenticationDao @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val apiUser: ApiUser
) {

    private val dispatcher = Dispatchers.IO

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun getUserToken(user: FirebaseUser?): String {
        val token = suspendCoroutine<String> { continuation ->
            if (user != null) {
                user.getIdToken(true)
                    .addOnCompleteListener { task ->
                        val token = if (task.isSuccessful) {
                            task.result.token ?: ""
                        } else {
                            ""
                        }
                        continuation.resume(token)
                    }
                    .addOnFailureListener {
                        continuation.resume("")
                    }
            } else {
                continuation.resume("")
            }
        }
        return token
    }

    fun saveUserToken(token: String) {
        PreferencesWrapper.save(CURRENT_USER_TOKEN, token)
    }

    suspend fun saveNewUser(user: User): NetworkResponse<ResponseBody, ErrorResponse> {
        return withContext(dispatcher) {
            apiUser.signupUserDetails(user)
        }
    }

    fun saveUserDetailsLocally(username: String, uid: String, token: String) {
        PreferencesWrapper.save(CURRENT_USER_USERNAME, username)
        PreferencesWrapper.save(CURRENT_USER_UID, uid)
        PreferencesWrapper.save(CURRENT_USER_TOKEN, token)
    }

    fun saveUserLocally(user: User) {
        PreferencesWrapper.save(CURRENT_USER, user)
    }

    fun getUser(): User? {
        return PreferencesWrapper.read(CURRENT_USER, User::class.java)
    }

    suspend fun getUserDetails(token: String): NetworkResponse<UserDetails, ErrorResponse> {
        return withContext(dispatcher) {
            apiUser.getUser("Bearer $token")
        }
    }

    companion object {
        private const val CURRENT_USER = "CURRENT_USER"
        private const val CURRENT_USER_TOKEN = "CURRENT_USER_TOKEN"
        private const val CURRENT_USER_USERNAME = "CURRENT_USER_USERNAME"
        private const val CURRENT_USER_UID = "CURRENT_USER_UID"
    }
}