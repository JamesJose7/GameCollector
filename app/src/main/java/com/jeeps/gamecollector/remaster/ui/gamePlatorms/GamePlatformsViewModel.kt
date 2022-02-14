package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.PlatformsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class GamePlatformsViewModel @Inject constructor(
    private val platformsRepository: PlatformsRepository,
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    private val ignoredExceptions = listOf(
        FirebaseFirestoreException.Code.PERMISSION_DENIED
    )

    private val _platforms = MutableLiveData<List<Platform>>()
    val platforms: LiveData<List<Platform>>
        get() = _platforms.also {
            loadPlatforms()
        }

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean>
        get() = _isUserLoggedIn.also {
            checkUserLoginStatus()
        }

    private fun loadPlatforms() {
        val user = authenticationRepository.getUser() ?: return

        viewModelScope.launch {
            platformsRepository.getPlatforms(user.username).collect {
                when (it) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        stopLoading()
                        it.data.let { result ->
                            val sortedPlatforms = result.sortedWith(Comparator.comparing { p: Platform ->
                                p.name.lowercase()
                            })
                            _platforms.postValue(sortedPlatforms)
                        }
                    }
                    is State.Failed -> {
                        stopLoading()
                        handleError(it.e)
                    }
                }
            }
        }
    }

    private fun handleError(e: Throwable?) {
        if (e is FirebaseFirestoreException) {
            if (e.code !in ignoredExceptions) {
                handleError(ErrorType.SERVER_ERROR, e)
            }
        } else {
            handleError(ErrorType.UNKNOWN_ERROR, e)
        }
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            val isUserLoggedIn = authenticationRepository.isUserLoggedIn()
            _isUserLoggedIn.postValue(isUserLoggedIn)
        }
    }
}