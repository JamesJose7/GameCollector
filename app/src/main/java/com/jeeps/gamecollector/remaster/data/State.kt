package com.jeeps.gamecollector.remaster.data

sealed class State<out T> {
    class Loading<out T> : State<T>()
    data class Success<out T>(val data: T) : State<T>()
    data class Failed<out T>(val message: String, val e: Throwable? = null) : State<T>()
}