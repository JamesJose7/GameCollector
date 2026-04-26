package com.jeeps.gamecollector.remaster.data.repository

import com.firebase.ui.auth.IdpResponse
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.user.User
import com.jeeps.gamecollector.remaster.data.model.AuthenticationDao
import com.jeeps.gamecollector.remaster.utils.extensions.getToken
import com.jeeps.gamecollector.remaster.utils.user.UserUtils
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val authenticationDao: AuthenticationDao
) {

    suspend fun isUserLoggedIn() : Boolean {
        val currentUser = authenticationDao.getCurrentFirebaseUser()
        val token = currentUser?.getToken().orEmpty()
        // Refresh stored token
        authenticationDao.saveUserToken(token)
        return token.isNotEmpty()
    }

    suspend fun saveNewUser(authResponse: IdpResponse) {
        val user = authenticationDao.getCurrentFirebaseUser()

        // Save user details for new users
        if (authResponse.isNewUser) {
            val generatedUserName: String = authResponse.email?.let {
                UserUtils.convertEmailToUsername(it)
            } ?: UserUtils.generateRandomUsername()

            val newUserResponse = authenticationDao.saveNewUser(
                User(user?.uid ?: "", generatedUserName, authResponse.email ?: "")
            )
            when (newUserResponse) {
                is NetworkResponse.Error -> {
                    throw newUserResponse.error!!
                }
                else -> {}
            }
        }

        when (val response = authenticationDao.getUserDetails()) {
            is NetworkResponse.Error -> {
                throw response.error!!
            }
            is NetworkResponse.Success -> {
                val userDetails = response.body.credentials
                authenticationDao.saveUserLocally(userDetails)
            }
        }
    }

    fun getUser(): User? {
        return authenticationDao.getUser()
    }
}