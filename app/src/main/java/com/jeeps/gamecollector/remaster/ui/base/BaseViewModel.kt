package com.jeeps.gamecollector.remaster.ui.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.utils.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val messageEventChannel = Channel<MessageEvent>()
    val messageEventsChannelFlow = messageEventChannel.receiveAsFlow()

    val TAG = javaClass.simpleName

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    @Deprecated("Use messageEventChannelFlow instead")
    val errorMessage: LiveData<String>
        get() = _errorMessage

    // TODO: Change this to a type safe class instead of strings
    private val _serverMessage = MutableLiveData<Event<String>>()
    @Deprecated("Use messageEventChannelFlow instead")
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
        viewModelScope.launch {
            messageEventChannel.send(MessageEvent.Error(errorType.message))
        }
    }

    fun handleError(error: NetworkResponse.Error<*, *>) {
        val message: String = when (error) {
            is NetworkResponse.ServerError -> ErrorType.SERVER_ERROR.message
            is NetworkResponse.NetworkError -> ErrorType.NETWORK_ERROR.message
            else -> ErrorType.UNKNOWN_ERROR.message
        }
        Log.e(TAG, message)
        _errorMessage.postValue(message)
        viewModelScope.launch {
            messageEventChannel.send(MessageEvent.Error(message))
        }
    }

    fun postServerMessage(message: String) {
        _serverMessage.value = Event(message)
        viewModelScope.launch {
            messageEventChannel.send(MessageEvent.Success(message))
        }
    }

    sealed class MessageEvent {
        data class Error(val message: String) : MessageEvent()
        data class Success(val message: String) : MessageEvent()
    }
}