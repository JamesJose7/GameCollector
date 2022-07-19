package com.jeeps.gamecollector.remaster.ui.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.utils.Event

open class BaseViewModel : ViewModel() {

    val TAG = javaClass.simpleName

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _serverMessage = MutableLiveData<Event<String>>()
    val serverMessage: LiveData<Event<String>>
        get() = _serverMessage

    fun startLoading() {
        _isLoading.postValue(true)
    }

    fun stopLoading() {
        _isLoading.postValue(false)
    }

    fun handleError(errorType: ErrorType, e: Throwable? = null) {
        // TODO: Create additional method that infers error type based on throwable
        e?.message?.let { Log.e(TAG, it) }
        _errorMessage.postValue(errorType.message)
    }

    fun handleError(error: NetworkResponse.Error<*, *>) {
        val message: String = when (error) {
            is NetworkResponse.ServerError -> ErrorType.SERVER_ERROR.message
            is NetworkResponse.NetworkError -> ErrorType.NETWORK_ERROR.message
            else -> ErrorType.UNKNOWN_ERROR.message
        }
        Log.e(TAG, message)
        _errorMessage.postValue(message)
    }

    fun postServerMessage(message: String) {
        _serverMessage.value = Event(message)
    }
}