package com.jeeps.gamecollector.remaster.ui.login

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    private val TAG = javaClass.simpleName

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _isLoginSuccessful = MutableLiveData<Event<Boolean>>()
    val isLoginSuccessful: LiveData<Event<Boolean>>
        get() = _isLoginSuccessful

    fun processLoginResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            viewModelScope.launch {
                response?.let { user ->
                    try {
                        authenticationRepository.saveNewUser(user)
                        _isLoginSuccessful.postValue(Event(true))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        e.message?.let { message ->
                            Log.e(TAG, message)
                        }
                        _errorMessage.postValue("An unknown error has occurred")
                        _isLoginSuccessful.postValue(Event(false))
                    }
                }
            }
        } else {
            // Handle login failure
            when (response?.error?.errorCode ?: ErrorCodes.UNKNOWN_ERROR) {
                ErrorCodes.NO_NETWORK -> {
                    _errorMessage.postValue("No network connection available")
                }
                else -> {
                    _errorMessage.postValue("An unknown error has occurred")
                }
            }
            response?.error?.message?.let { Log.e(TAG, it) }
            _isLoginSuccessful.postValue(Event(false))
        }
    }

}