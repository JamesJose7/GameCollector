package com.jeeps.gamecollector.remaster.ui.login

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

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
                        // TODO: Handle more specific exceptions
                        handleError(ErrorType.UNKNOWN_ERROR, e)
                        _isLoginSuccessful.postValue(Event(false))
                    }
                }
            }
        } else {
            // Handle login failure
            when (response?.error?.errorCode ?: ErrorCodes.UNKNOWN_ERROR) {
                ErrorCodes.NO_NETWORK -> {
                    handleError(ErrorType.NETWORK_ERROR, response?.error)
                }
                else -> {
                    handleError(ErrorType.UNKNOWN_ERROR, response?.error)
                }
            }
            _isLoginSuccessful.postValue(Event(false))
        }
    }

}