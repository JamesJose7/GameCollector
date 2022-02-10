package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.PlatformsRepository
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
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _platforms = MutableLiveData<List<Platform>>()
    val platforms: LiveData<List<Platform>>
        get() = _platforms.also {
            loadPlatforms()
        }

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

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
                        _isLoading.postValue(true)
                    }
                    is State.Success -> {
                        _isLoading.postValue(false)
                        it.data.let { result ->
                            val sortedPlatforms = result.sortedWith(Comparator.comparing { p: Platform ->
                                p.name.lowercase()
                            })
                            _platforms.postValue(sortedPlatforms)
                        }
                    }
                    is State.Failed -> {
                        _isLoading.postValue(false)
                        _errorMessage.postValue(it.message)

                    }
                }
            }
        }
    }

    private fun checkUserLoginStatus() {
        viewModelScope.launch {
            val isUserLoggedIn = authenticationRepository.isUserLoggedIn()
            _isUserLoggedIn.postValue(isUserLoggedIn)
        }
    }
}